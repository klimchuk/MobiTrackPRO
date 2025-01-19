package henson.midp;

import java.io.*;

public class Obex
{
  public static final byte OBEX_CONNECT          = (byte)0x80;
  public static final byte OBEX_DISCONNECT       = (byte)0x81;
  public static final byte OBEX_VERSION          = 0x10;
  public static final byte OBEX_CONNECT_FLAGS    = 0x00;
  public static final byte OBEX_SUCCESS          = (byte)0xA0;
  public static final byte OBEX_CONTINUE         = (byte)0x90;
  public static final byte OBEX_PUT              = 0x02;
  public static final byte OBEX_PUT_FINAL        = (byte)0x82;
  public static final byte OBEX_NAME             = 0x01;
  public static final byte OBEX_LENGTH           = (byte)0xC3;
  public static final byte OBEX_BODY             = 0x48;
  public static final byte OBEX_END_OF_BODY      = 0x49;
  public static final int BLOCK_SIZE             = 0x400;
  private ByteArrayInputStream bais=null;

  public Obex()
  {
  }

  public byte[] connect()
  {
    return new byte[]{ OBEX_CONNECT, 0x00, 0x07, OBEX_VERSION, 0x00, 0x20, 0x00};
  }

  public byte[] disconnect()
  {
    return new byte[]{ OBEX_DISCONNECT, 0x00, 0x03 };
  }

  public void setData(byte[] data)
  {
    if(data==null)
      bais=null;
    else
      bais=new ByteArrayInputStream(data);
  }

  public ByteArrayInputStream getData()
  {
    return bais;
  }

  public byte[] getPutChunk()
  {
    if(bais==null)
      return null;
    int size=bais.available();
    boolean end=(size<=BLOCK_SIZE);
    try
    {
      ByteArrayOutputStream bbbb = new ByteArrayOutputStream();
      if(end)
        bbbb.write(OBEX_PUT_FINAL);
      else
      {
        bbbb.write(OBEX_PUT);
        size=BLOCK_SIZE;
      }
      //
      int n=size+6;
      bbbb.write((byte)((n&0xff00)>>8));
      bbbb.write((byte)(n&0xff));
      //
      if(end)
        bbbb.write(OBEX_END_OF_BODY);
      else
        bbbb.write(OBEX_BODY);
      n=size+3;
      bbbb.write((byte)((n&0xff00)>>8));
      bbbb.write((byte)((size+3)&0xff));
      //
      byte[] data = new byte[size];
      bais.read(data);
      bbbb.write(data);
      //
      if(end)
        bais=null;
      //
      bbbb.close();
      return bbbb.toByteArray();
    }
    catch (IOException ex)
    {
      return null;
    }
  }

  public byte[] put(String filename)
  {
    int size=bais.available();
    int sizeFull=size;
    boolean end=(size<=BLOCK_SIZE);
    try
    {
      ByteArrayOutputStream bbbb = new ByteArrayOutputStream();
      if(end)
        bbbb.write(OBEX_PUT_FINAL);
      else
      {
        bbbb.write(OBEX_PUT);
        size=BLOCK_SIZE;
      }
      //
      int filenamelen=(filename.length()+1)*2;
      int n=size+filenamelen+3+11;
      bbbb.write((byte)((n&0xff00)>>8));
      bbbb.write((byte)(n&0xff));
      //
      bbbb.write(OBEX_NAME);
      bbbb.write(0x00);
      bbbb.write((byte)(filenamelen+3));
      // Имя в UNICODE
      byte[] name = new byte[filenamelen];
      for(int i=0; i<filename.length()+1; i++)
      {
        name[i * 2] = 0;
        if(i==filename.length())
          name[i * 2+1] = 0;
        else
          name[i * 2 + 1] = (byte) filename.charAt(i);
      };
      bbbb.write(name);
      //
      bbbb.write(OBEX_LENGTH);
      // Размер
      bbbb.write((byte)((sizeFull&0xff000000)>>24));
      bbbb.write((byte)((sizeFull&0xff0000)>>16));
      bbbb.write((byte)((sizeFull&0xff00)>>8));
      bbbb.write((byte)(sizeFull&0xff));
      //
      if(end)
        bbbb.write(OBEX_END_OF_BODY);
      else
        bbbb.write(OBEX_BODY);
      n=size+3;
      bbbb.write((byte)((n&0xff00)>>8));
      bbbb.write((byte)((size+3)&0xff));
      //
      byte[] data = new byte[size];
      bais.read(data);
      bbbb.write(data);
      //
      if(end)
        bais=null;
      //
      bbbb.close();
      return bbbb.toByteArray();
    }
    catch (IOException ex)
    {
      return null;
    }
  }
}
