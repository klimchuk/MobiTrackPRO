package henson.midp.Model;

import java.util.*;
import java.io.*;
import henson.midp.*;

public class Track extends Vector
{
  public String name="Default";
  // Общая длина трека, km
  public double length=0.0;

  public Track()
  {
  }

  public double getLength()
  {
    double coeff=MIDlet1.rmsManager.getUnitCoeff(false);
    return Math.ceil(length*coeff*100.0)/100.0;
  }

  public void clear()
  {
    this.removeAllElements();
    length=0.0;
  }

  public String getPltName()
  {
      StringBuffer sb=new StringBuffer();
      sb.append("track");
      if(size()>0)
      {
          Point p = (Point)this.elementAt(0);
          // Дата
          sb.append(" "+p.DateTime[3]+"/"+p.DateTime[4]+"/"+Util.withLeadZero(p.DateTime[5]));
          // Время
          sb.append(" "+p.DateTime[0]+":"+Util.withLeadZero(p.DateTime[1]));
      }
      return sb.toString();
  }
  
  public String createPltData()
  {
    StringBuffer sb=new StringBuffer();
    for(int i=0; i<size(); i++)
    {
      Point p = (Point)this.elementAt(i);
      sb.append(p.Latitude);
      sb.append(",");
      sb.append(p.Longitude);
      sb.append(",");
      if(i==0)
        sb.append("1");
      else
        sb.append("0");
      sb.append(",");
      sb.append(p.Altitude);
      sb.append(",");
      sb.append(p.OleDateFromTm());
      sb.append('\r');
      sb.append('\n');
    }
    return sb.toString();
  }

  public boolean createKMLData(boolean route, OutputStream os)
  {
      try
      {
        // Рисуем линию трека
        int pointIndex=0;
        int segment=0;
        while(pointIndex<size())
        {
            os.write("<Folder>\r\n".getBytes());
            //
            if(route)
            {
                os.write("<name>Route</name>\r\n".getBytes());
            }
            else
            {
                Point p = (Point)this.elementAt(pointIndex);            
                if(segment==0)
                    os.write(("<name>Track "+p.getDateTime(Point.TYPE_DATETIME)+"</name>\r\n").getBytes());
                else
                    os.write(("<name>"+segment+"> Track "+p.getDateTime(Point.TYPE_DATETIME)+"</name>\r\n").getBytes());
            }
            os.write("<Placemark>\r\n".getBytes());
            os.write("<name>Line</name>\r\n".getBytes());
            os.write("<Style>\r\n".getBytes());
            os.write("<LineStyle>\r\n".getBytes());
            if(route)
                os.write("<color>a000ff00</color>\r\n".getBytes());
            else
            {
                int color=(segment%5);
                switch(color)
                {
                    case 0:
                        os.write("<color>a0ff0000</color>\r\n".getBytes());
                        break;
                    case 1:
                        os.write("<color>a000ff00</color>\r\n".getBytes());
                        break;
                    case 2:
                        os.write("<color>a00000ff</color>\r\n".getBytes());
                        break;
                    case 3:
                        os.write("<color>a000ffff</color>\r\n".getBytes());
                        break;
                    case 4:
                        os.write("<color>a0ffffff</color>\r\n".getBytes());
                        break;
                }
            }        
            os.write("<width>3</width>\r\n".getBytes());
            os.write("</LineStyle>\r\n".getBytes());
            os.write("</Style>\r\n".getBytes());
            //
            os.write("<LineString>\r\n".getBytes());
            os.write("<coordinates>\r\n".getBytes());
            //
            int startIndex=pointIndex;
            while(pointIndex<size())
            {
              Point p = (Point)this.elementAt(pointIndex);
              if(!route && pointIndex>startIndex && MIDlet1.rmsManager.breakTrackDelay>0)
              {
                  Point prev = (Point)this.elementAt(pointIndex-1);
                  if(Math.abs(p.checkDifference(prev))>MIDlet1.rmsManager.breakTrackDelay)
                      break;
              }
              p.segment=segment;
              os.write((p.Longitude+","+p.Latitude+","+(p.Altitude/Util.M2FT)+" ").getBytes());
              pointIndex++;          
            }
            //        
            os.write("\r\n</coordinates>\r\n".getBytes());
            os.write("</LineString>\r\n".getBytes());
            os.write("</Placemark>\r\n".getBytes());
            // Points
            os.write("<Folder>\r\n".getBytes());
            os.write("<name>Points</name>\r\n".getBytes());
            // Рисуем точки трека
            for(int i=0; i<size(); i++)
            {
                Point p = (Point)this.elementAt(i);
                //
                if(p.segment==segment)
                {
                    os.write("<Placemark>\r\n".getBytes());
                    os.write(p.getKML(route, "", "").getBytes());
                    os.write("</Placemark>\r\n".getBytes());
                }
            }
            // End Points
            os.write("</Folder>\r\n".getBytes());
            // End Track
            os.write("</Folder>\r\n".getBytes());
            segment++;
        }      
    }
      catch(IOException ex)
      {
        return false;
      }
      //
      return true;
  }
/*
  public String createKMLData(boolean route)
  {
    StringBuffer sb=new StringBuffer();
    // Рисуем линию трека
    int pointIndex=0;
    int segment=0;
    while(pointIndex<size())
    {
        sb.append("<Folder>\r\n");
        //
        if(route)
        {
            sb.append("<name>Route</name>\r\n");
        }
        else
        {
            Point p = (Point)this.elementAt(pointIndex);            
            if(segment==0)
                sb.append("<name>Track "+p.getDateTime(Point.TYPE_DATETIME)+"</name>\r\n");
            else
                sb.append("<name>"+segment+"> Track "+p.getDateTime(Point.TYPE_DATETIME)+"</name>\r\n");
        }
        sb.append("<Placemark>\r\n");
        sb.append("<name>Line</name>\r\n");
        sb.append("<Style>\r\n");
        sb.append("<LineStyle>\r\n");
        if(route)
            sb.append("<color>a000ff00</color>\r\n");
        else
        {
            int color=(segment%5);
            switch(color)
            {
                case 0:
                    sb.append("<color>a0ff0000</color>\r\n");
                    break;
                case 1:
                    sb.append("<color>a000ff00</color>\r\n");
                    break;
                case 2:
                    sb.append("<color>a00000ff</color>\r\n");
                    break;
                case 3:
                    sb.append("<color>a000ffff</color>\r\n");
                    break;
                case 4:
                    sb.append("<color>a0ffffff</color>\r\n");
                    break;
            }
        }        
        sb.append("<width>3</width>\r\n");
        sb.append("</LineStyle>\r\n");
        sb.append("</Style>\r\n");
        //
        sb.append("<LineString>\r\n");
        sb.append("<coordinates>\r\n");
        //
        int startIndex=pointIndex;
        while(pointIndex<size())
        {
          Point p = (Point)this.elementAt(pointIndex);
          if(!route && pointIndex>startIndex && MIDlet1.rmsManager.breakTrackDelay>0)
          {
              Point prev = (Point)this.elementAt(pointIndex-1);
              if(Math.abs(p.checkDifference(prev))>MIDlet1.rmsManager.breakTrackDelay)
                  break;
          }
          p.segment=segment;
          sb.append(p.Longitude+","+p.Latitude+","+(p.Altitude/Util.M2FT)+" ");
          pointIndex++;          
        }
        //        
        sb.append("\r\n</coordinates>\r\n");
        sb.append("</LineString>\r\n");
        sb.append("</Placemark>\r\n");
        // Points
        sb.append("<Folder>\r\n");
        sb.append("<name>Points</name>\r\n");
        // Рисуем точки трека
        for(int i=0; i<size(); i++)
        {
            Point p = (Point)this.elementAt(i);
            //
            if(p.segment==segment)
            {
                sb.append("<Placemark>\r\n");
                sb.append(p.getKML(route, "", ""));
                sb.append("</Placemark>\r\n");
            }
        }
        // End Points
        sb.append("</Folder>\r\n");
        // End Track
        sb.append("</Folder>\r\n");
        segment++;
    }
    //
    return sb.toString();
  }

  public double length()
  {
    double len=0.0;
    for(int i=1; i<size(); i++)
    {
      Point newPoint=(Point)this.elementAt(i);
      Point oldPoint=(Point)this.elementAt(i-1);
      //
      len+=Util.distance(newPoint.Latitude, newPoint.Longitude, oldPoint.Latitude, oldPoint.Longitude);
    }
    return Math.ceil(len*100.0)/100.0;
  }
*/

  public void append(double Latitude, double Longitude, double Altitude, double Speed, String time, String date)
  {
    double dist=0.0;
    if(size()>0)
    {
      Point p=(Point)this.elementAt(size()-1);
      dist=Util.distance(Latitude, Longitude, p.Latitude, p.Longitude);
      if(dist<0.05)
        return;
    }
    //
    Point p=new Point(Latitude, Longitude, Altitude);
    p.Speed=Speed;
    Vector v=Util.parseString(time, ":", false);
    // Час
    p.DateTime[0]=Byte.parseByte((String)v.elementAt(0));
    // Минуты
    p.DateTime[1]=Byte.parseByte((String)v.elementAt(1));
    // Секунды
    p.DateTime[2]=Byte.parseByte((String)v.elementAt(2));
    //
    v=Util.parseString(date, "/", false);
    // День
    p.DateTime[3]=Byte.parseByte((String)v.elementAt(0));
    // Месяц
    p.DateTime[4]=Byte.parseByte((String)v.elementAt(1));
    // Год
    p.DateTime[5]=Byte.parseByte((String)v.elementAt(2));
    //
    addElement(p);
    length+=dist;
  }

  public void load(DataInputStream dis) throws IOException
  {
    name=dis.readUTF();
    length=dis.readDouble();
    int size=dis.readInt();
    for(int i=0; i<size; i++)
    {
      Point p=new Point(0.0, 0.0, 0.0);
      p.Latitude=dis.readDouble();
      p.Longitude=dis.readDouble();
      p.Altitude=(double)dis.readFloat();
      p.Speed=(double)dis.readFloat();
      for(int j=0; j<p.DateTime.length; j++)
        p.DateTime[j]=dis.readByte();
      this.addElement(p);
    }
  }

  public void save(DataOutputStream dos) throws IOException
  {
    dos.writeUTF(name);
    dos.writeDouble(length);
    dos.writeInt(size());
    for(int i=0; i<size(); i++)
    {
      Point p=(Point)this.elementAt(i);
      dos.writeDouble(p.Latitude);
      dos.writeDouble(p.Longitude);
      // Используем Float для уменьшения объема данных
      dos.writeFloat((float)p.Altitude);
      dos.writeFloat((float)p.Speed);
      //
      for(int j=0; j<p.DateTime.length; j++)
        dos.writeByte(p.DateTime[j]);
    }
  }
}
