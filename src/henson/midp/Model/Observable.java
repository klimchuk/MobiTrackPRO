package henson.midp.Model;

import henson.midp.*;
import henson.midp.View.*;
import java.io.*;
import java.util.*;

public class Observable
{
  public String name="?";
  public boolean active=true;
  public boolean visible=true;
  public boolean showTrack=false;
  public String path="http://www.unteh.com/cgi-bin/netgps.pl";
  public String login="";
  public String password="";
  public int interval=60;
  //
  public long lastAction=0L;
  //
  private String latitude="";
  private String longitude="";
  private double defaultLatitude=0.0;
  private double defaultLongitude=0.0;
  //
  public Track track=new Track();
  //
  public Observable()
  {
  }

  public void load(DataInputStream dis) throws IOException
  {
    name=dis.readUTF();
    active=dis.readBoolean();
    visible=dis.readBoolean();
    showTrack=dis.readBoolean();
    path=dis.readUTF();
    login=dis.readUTF();
    password=dis.readUTF();
    interval=dis.readInt();
    //
    //track=new Track();
    //track.load(dis);
  }

  public void save(DataOutputStream dos) throws IOException
  {
    dos.writeUTF(name);
    dos.writeBoolean(active);
    dos.writeBoolean(visible);
    dos.writeBoolean(showTrack);
    dos.writeUTF(path);
    dos.writeUTF(login);
    dos.writeUTF(password);
    dos.writeInt(interval);
    //
    //track.save(dos);
  }

  public synchronized double getLatitude()
  {
    try
    {
      if (latitude.length() == 11)
      {
        String s1 = latitude.substring(0, 2);
        String s2 = latitude.substring(3, 10);
        String s3 = latitude.substring(10, 11);
        double value = Double.parseDouble(s1) + Double.parseDouble(s2) / 60.0;
        if (s3.startsWith("S"))
          return -value;
        else
          return value;
      }
    }
    catch (Exception e)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("Latitude",
          e.getMessage() + "\n" + latitude, null));
    }
    return defaultLatitude;
  }

  public synchronized double getLongitude()
  {
    try
    {
      if (longitude.length() == 12)
      {
        String s1 = longitude.substring(0, 3);
        String s2 = longitude.substring(4, 11);
        String s3 = longitude.substring(11, 12);
        double value = Double.parseDouble(s1) + Double.parseDouble(s2) / 60.0;
        if (s3.startsWith("W"))
          return -value;
        else
          return value;
      }
    }
    catch (Exception e)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("Longitude",
          e.getMessage() + "\n" + longitude, null));
    }
    return defaultLongitude;
  }

  public synchronized void Parse(String nmea)
  {
    if(!nmea.startsWith("GPSOK"))
      return;
    nmea=new String(nmea.substring(5));
    // Удаляем контрольную сумму
    int pos=nmea.indexOf("*");
    if(!nmea.startsWith("$GP") || pos==-1)
      return;
    //
    nmea=new String(nmea.substring(0, pos));
    //
    Vector v=Util.parseString(nmea, ",", false);
    //
    try
    {
      if (nmea.startsWith("$GPGGA"))
      {
        // Fix Data
        int positionFix=Integer.parseInt((String) v.elementAt(6));
        if (positionFix>0)
        {
          String s = (String) v.elementAt(2) + (String) v.elementAt(3);
          latitude = s.substring(0, 2) + "-" + s.substring(2);
          s = (String) v.elementAt(4) + (String) v.elementAt(5);
          longitude = s.substring(0, 3) + "-" + s.substring(3);
          //
          //track.append(getLatitude(), getLongitude(), 0.0, "10:10:10", "01/01/05");
        }
      }
      else
      if (nmea.startsWith("$GPRMC"))
      {
        // Fix Data
        String s = (String) v.elementAt(2);
        if (s.startsWith("A"))
        {
          s = (String) v.elementAt(3) + (String) v.elementAt(4);
          latitude = s.substring(0, 2) + "-" + s.substring(2);
          s = (String) v.elementAt(5) + (String) v.elementAt(6);
          longitude = s.substring(0, 3) + "-" + s.substring(3);
          //
          //track.append(getLatitude(), getLongitude(), 0.0, "10:10:10", "01/01/05");
        }
      }
      else
      if (nmea.startsWith("$GPGLL"))
      {
        // Position
        String s = (String) v.elementAt(6);
        if (s.startsWith("A"))
        {
          s = (String) v.elementAt(1) + (String) v.elementAt(2);
          latitude = s.substring(0, 2) + "-" + s.substring(2);
          s = (String) v.elementAt(3) + (String) v.elementAt(4);
          longitude = s.substring(0, 3) + "-" + s.substring(3);
          //
          //track.append(getLatitude(), getLongitude(), 0.0, "10:10:10", "01/01/05");
        }
      }
    }
    catch(Exception e)
    {
      // Игнорируем ошибки!
    }
  }

  public void Tick()
  {
    if(!active || login.length()==0 || password.length()==0)
      return;
    long n=System.currentTimeMillis();
    if(n-interval*1000L>lastAction)
    {
      lastAction=n;
      MIDlet1.netManager.getObservable(this);
    }
  }

  public int setInterval(String s)
  {
    if(s==null || s.length()==0)
      interval=60;
    else
    {
      try
      {
        interval = Integer.parseInt(s);
        if(interval<5)
          interval=5;
        else
        if(interval>3600)
          interval=3600;
      }
      catch (NumberFormatException ex)
      {
        interval=60;
      }
    }
    return interval;
  }
}
