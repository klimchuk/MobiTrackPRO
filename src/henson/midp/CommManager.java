package henson.midp;

import henson.midp.View.*;
import javax.microedition.io.*;
import java.io.*;
import java.util.*;

public class CommManager implements Runnable
{
  public final static byte STATE_INITIAL=0x01;
  public final static byte STATE_CONNECTED=0x02;
  public byte State = STATE_INITIAL;
  //
  CommConnection conn=null;
  // Поток входных данных
  Thread t=null;
  boolean running;
  //
  InputStream is=null;
  ByteArrayOutputStream bytearrayoutputstream = null;

  public CommManager()
  {
  }

  public static Vector getPortsArray()
  {
    String s=System.getProperty("microedition.commports");
    if(s!=null && s.length()>0)
      return Util.parseString(s, ",", false);
    return null;
  }

  public boolean OpenConnection(String connectionString)
  {
    try
    {
      conn = (CommConnection) Connector.open(connectionString, Connector.READ, true);
      //
      running = true;
      t = new Thread(this);
      t.start();
      //
      State = STATE_CONNECTED;
    }
    catch (IOException ex)
    {
      conn=null;
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "CommManager::OpenConnection "+ex.getMessage(), null));
    }
    //
    return (conn!=null);
  }

  public boolean CloseConnection()
  {
    State = STATE_INITIAL;
    boolean result=false;
    if(conn!=null)
    {
      try
      {
        if(t!=null)
        {
          running = false;
          // Ждем завершения
          t.join();
        }
        if(is!=null)
        {
          is.close();
          is=null;
        }
        //
        conn.close();
        result=true;
      }
      catch (Exception ex)
      {
        MIDlet1.screenManager.replaceScreen(new InfoForm("Error", "Error while closing COM:"+ex.getMessage(), null));
      }
      conn=null;
    }
    return result;
  }

  public void run()
  {
    while(running)
    {
      if(conn==null)
        break;
      //
      try
      {
        if (is == null)
          is = conn.openInputStream();
        if (is == null)
          break;
        //
        if(bytearrayoutputstream==null)
          bytearrayoutputstream=new ByteArrayOutputStream();
        else
          bytearrayoutputstream.reset();
        //
        int ch;
        while ( (ch = is.read()) != '\n')
        {
          bytearrayoutputstream.write(ch);
          MIDlet1.screenManager.pushScreen(new InfoForm("Data", ""+ch, null));
        }
        bytearrayoutputstream.flush();
         byte[] b = bytearrayoutputstream.toByteArray();
        if (b != null && b.length > 0)
          MIDlet1.gpsManager.AnalyzeNMEA(new String(b));
      }
      catch(InterruptedIOException ioex)
      {
        // timeout?
      }
      catch(Exception ex)
      {
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", "CommManager::ReceiveData Connection is lost! Please reconnect your device.", null));
        //
        try
        {
          if(is!=null)
          {
            is.close();
            is=null;
          }
          if(conn!=null)
          {
            conn.close();
            conn=null;
          }
          running=false;
          State=STATE_INITIAL;
        }
        catch (IOException ex1)
        {
        }
      }
    }
    t=null;
  }
}
