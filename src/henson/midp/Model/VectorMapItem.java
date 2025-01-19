package henson.midp.Model;

import java.io.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class VectorMapItem
{
  public final static byte REGION_UNKNOWN=0;
  public final static byte REGION_POI=1;
  public final static byte REGION_CITY=2;
  public final static byte REGION_POLYLINE=3;
  public final static byte REGION_POLYGON=4;
  //
  public byte region=REGION_UNKNOWN;
  public short type=0;
  public String name="";
  public double[] data=null;
  // Временные данные
  public int[] x=null;
  public int[] y=null;

  public VectorMapItem()
  {
  }

  public void load(DataInputStream dis) throws IOException
  {
    region=dis.readByte();
    type=dis.readShort();
    name=dis.readUTF();
    int size=dis.readInt();
    //
    data=new double[size];
    x=new int[size/2];
    y=new int[size/2];
    //
    for(int i=0; i<size; i++)
      data[i]=dis.readDouble();
  }
}
