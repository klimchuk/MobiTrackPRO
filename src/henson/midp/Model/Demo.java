package henson.midp.Model;

import henson.midp.*;
import henson.midp.View.*;
import java.io.*;
import java.util.*;

public class Demo extends TimerTask
{
  InputStream is=null;
  public String path="";

  public Demo(String path)
  {
      this.path=path;
  }

  private boolean Open()
  {
      if(path==null || path.length()==0)
      {
        try
        {
          is = getClass().getResourceAsStream("/demo.ubx");
        }
        catch(Exception e)
        {
          MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't find demo track", null));
          is=null;
        }
      }
      else
      {
        byte[] data=MIDlet1.rmsManager.loadFile(path);
        if(data!=null)
            is=new ByteArrayInputStream(data);
      }
      return (is!=null);
  }

  public void run()
  {
    if(is==null)
    {
      if(!Open())
      {
        cancel();
        return;
      }
    }
    //
    // Берем одну строку и отправляем на обработку
    int ch;
    ByteArrayOutputStream baos=new ByteArrayOutputStream();
    try
    {
      while (true)
      {
          ch = is.read();
        if(ch==-1 || ch==(int)'\n' || ch==(int)'\r')
          break;
        baos.write(ch);
      }
      baos.flush();
      byte[] b = baos.toByteArray();
      if (b != null && b.length > 0)
      {
          String s=new String(b);
          MIDlet1.gpsManager.AnalyzeNMEA(s);
      }
    }
    catch (IOException ex)
    {
      cancel();
      is=null;
    }
  }
}
