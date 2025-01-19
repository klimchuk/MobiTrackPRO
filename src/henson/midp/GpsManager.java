package henson.midp;

import henson.midp.Model.*;
import henson.midp.View.*;
import java.util.*;
import javax.microedition.lcdui.*;

public class GpsManager implements ILocationManager
{
  // NetGPS/KMLGPS
  long lastSent=0L;
  double lastLatitude=-90.0;
  double lastLongitude=-180.0;
  // Track
  long lastSaved=0L;
  //
  private boolean valid=false;
  private String latitude="00-00";
  private String longitude="000-00";
  private String time="000000.00";
  public String course="";
  public String speed="";
  private double precesionHDOP=0.0;
  private double precesionVDOP=0.0;
  private double precesionPDOP=0.0;
  private String altitude="";
  public String sattelites="";
  //public String visible="";

  private int positionFix=0;
  private int satelliteMode=0;

  //public String dgpsTime="";
  //public String dgpsID="";

  private String date="";
  public long offset=0L;

  public String lastCommand="";

  private Sattelite[] satt=new Sattelite[12];
  private Vector satellites=new Vector();

  //private double defaultLatitude=0.0;
  //private double defaultLongitude=0.0;

  public Observable currentObservable=null;

  // Last change of state
  public long lastChangeState = 0L;  
 
  public GpsManager()
  {
    for(int i=0; i<satt.length; i++)
      satt[i]=null;
  }
  
  public boolean Open() 
  {
      return true;
  }
  
  public boolean Close() 
  {
      return true;
  }
  
  private Calendar proceedDateTime(boolean local)
  {
      if(time.length()<6 || date.length()<6)
          return null;
      //
      Calendar cal=Calendar.getInstance();
      try
      {
          // Время
          cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0,2)));
          cal.set(Calendar.MINUTE, Integer.parseInt(time.substring(2,4)));
          double seconds=Double.parseDouble(time.substring(4));
          int sint=(int)seconds;
          int msoffset=(int)((seconds-(double)sint)*1000.0);
          cal.set(Calendar.SECOND, sint);
          if(msoffset>0.0)
              cal.set(Calendar.MILLISECOND, msoffset);
          // Дата
          cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.substring(0,2)));
          cal.set(Calendar.MONTH, Integer.parseInt(date.substring(2,4))-1);
          int year=Integer.parseInt(date.substring(4,6));
          if(year>70)
            cal.set(Calendar.YEAR, 1900+year);
          else
            cal.set(Calendar.YEAR, 2000+year);
          //
          if(local)
          {
              Date d=cal.getTime();
              long locald=d.getTime()+(long)MIDlet1.rmsManager.utcOffset*60000L;
              cal.setTime(new Date(locald));
          }
      }
      catch(Exception ex)
      {
          cal=null;
      }
      return cal;
  }

  public String getDate(boolean useLocalFormat)
  {
      Calendar cal=proceedDateTime(useLocalFormat);
      if(cal==null)
          return "";
      return MIDlet1.rmsManager.getDate(cal, useLocalFormat);
  }

  public String getTime(boolean useLocalFormat)
  {
      Calendar cal=proceedDateTime(useLocalFormat);
      if(cal==null)
          return "";
      return MIDlet1.rmsManager.getTime(cal, useLocalFormat);
  }

    public double getLatitude()
  {
    try
    {
        int len=latitude.length();
        if(len>6)
        {
            String s1 = latitude.substring(0, 2);
            String s2 = latitude.substring(3, len-1);
            String s3 = latitude.substring(len-1, len);
            double value = Double.parseDouble(s1) + Double.parseDouble(s2) / 60.0;
            if (s3.startsWith("S"))
              return -value;
            else
              return value;
        }
    }
    catch (Exception e)
    {
      //MIDlet1.screenManager.pushScreen(new InfoForm("Latitude",
        //  e.getMessage() + "\n" + latitude, null));
    }
    return MIDlet1.rmsManager.defaultLatitude;
  }
    
    public String getLatitudeString()
    {
        return latitude;
    }

  public double getLongitude()
  {
    try
    {
        int len=longitude.length();
        if(len>7)
        {
            String s1 = longitude.substring(0, 3);
            String s2 = longitude.substring(4, len-1);
            String s3 = longitude.substring(len-1, len);
            double value = Double.parseDouble(s1) + Double.parseDouble(s2) / 60.0;
            if (s3.startsWith("W"))
              return -value;
            else
              return value;            
        }
    }
    catch (Exception e)
    {
      //MIDlet1.screenManager.pushScreen(new InfoForm("Longitude",
        //  e.getMessage() + "\n" + longitude, null));
    }
    return MIDlet1.rmsManager.defaultLongitude;
  }
  
    public String getLongitudeString()
    {
        return longitude;
    }
  
/*
  public double getLatitude()
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

  public double getLongitude()
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
*/
  public double getSpeed(boolean local)
  {
    if(speed.length()==0)
      return 0.0;
    //
    double val=Double.parseDouble(speed);
    if(local==true)
    {
        double coeff=MIDlet1.rmsManager.getUnitCoeff(false);
        return Math.ceil(val*Util.KT2KM*coeff*10.0)/10.0;
    }
    else
        return val;
  }

  public double getAltitude(boolean feet)
  {
    if(altitude.length()==0)
      return 0.0;
    //
    double val=Double.parseDouble(altitude);
    double coeff=1;
    if(MIDlet1.rmsManager.typeUnit==Util.UNIT_ENGLISH ||
            MIDlet1.rmsManager.typeUnit==Util.UNIT_NAUTICAL ||
            feet)
    {
      // m -> ft
      coeff=Util.M2FT;
    }
    return Math.ceil(val*coeff*10.0)/10.0;
  }

  public double getCourse()
  {
    if(course.length()==0)
      return 0.0;
    //
    double val=Double.parseDouble(course);
    return Math.ceil(val*10.0)/10.0;
  }

  private void sendToNetGPS(String nmea)
  {
    double lat=getLatitude();
    double lon=getLongitude();
    MIDlet1.rmsManager.defaultLatitude = lat;
    MIDlet1.rmsManager.defaultLongitude = lon;
    //
    if(MIDlet1.rmsManager.useTrip)
    {      
      Calendar cal=proceedDateTime(false);
      if(cal!=null)
      {
        long msTime=cal.getTime().getTime();
        if(!MIDlet1.rmsManager.trip.append(msTime, getSpeed(false), lat, lon, getAltitude(false)) &&
            MIDlet1.rmsManager.points.size()>0)
            return;
      }
    }
    // По умолчанию точка маршрута
    Point point=MIDlet1.rmsManager.getRoutePoint();
    // Проверяем расстояние и переключаемся если надо
    if(point!=null)
    {
        double val=Util.distance(lat, lon, point.Latitude, point.Longitude);
        if(val*1000.0<(MIDlet1.rmsManager.typeUnit==Util.UNIT_METRIC?MIDlet1.rmsManager.pointMarginNext:MIDlet1.rmsManager.pointMarginNext/Util.M2FT))
        {
            // Переключаемся на другую точку если маршрут
            Point p=(Point)MIDlet1.rmsManager.route.elementAt(MIDlet1.rmsManager.routePointIndex);
            if(MIDlet1.rmsManager.routePointIndex+1<MIDlet1.rmsManager.route.size())
            {
                Util.playSound(Util.SOUND_NEXTPOINT);
                MIDlet1.screenManager.vibra(1000);                        
                //
                MIDlet1.rmsManager.routePointIndex++;
                Point newp=(Point)MIDlet1.rmsManager.route.elementAt(MIDlet1.rmsManager.routePointIndex);
                Alert al=new Alert("Route", "Next point is "+newp.Name, null, AlertType.INFO);
                al.setTimeout(5000);                        
                MIDlet1.screenManager.setCurrent(al);
            }
            else
            {
                Util.playSound(Util.SOUND_ENDROUTE);
                MIDlet1.screenManager.vibra(2000);
                //
                MIDlet1.rmsManager.routePointIndex=-1;
                Alert al=new Alert("Route", "Congratulation! You've reached end of route "+p.Name, null, AlertType.INFO);
                al.setTimeout(7000);
                MIDlet1.screenManager.setCurrent(al);
            }
        }
        else
        if(val*1000.0<(MIDlet1.rmsManager.typeUnit==Util.UNIT_METRIC?MIDlet1.rmsManager.pointMarginSound:MIDlet1.rmsManager.pointMarginSound/Util.M2FT))
        {
            // Приближаемся к точке
            Util.playSound(Util.SOUND_INCOMING);
        }
    }
    // KMLGPS и NetGPS
    long current=System.currentTimeMillis();
    if (precesionHDOP<=4.0 && 
            current - lastSent > MIDlet1.rmsManager.intervalNetGPS * 1000L &&
            Util.distance(lat, lon, lastLatitude, lastLongitude)*
            (MIDlet1.rmsManager.typeUnit==Util.UNIT_METRIC?1000.0:1000.0*Util.M2FT)>MIDlet1.rmsManager.distanceNetGPS)
    {
      lastSent = current;
      if(MIDlet1.rmsManager.useNetGPS)
      {
          // Если есть kml в строке, то связь с Google Earth иначе NetGPS
          if(MIDlet1.rmsManager.addressNetGPS.indexOf("kml")==-1)
          {
            // NetGPS
            MIDlet1.netManager.url = MIDlet1.rmsManager.addressNetGPS;
            MIDlet1.netManager.query = "?un=" + MIDlet1.rmsManager.login + "&pw=" +
                MIDlet1.rmsManager.password + "&cds=" + nmea;
            // GET query
            MIDlet1.netManager.send(true, 0, null);           
          }
          else
          {
            // Google Earth
            Point p=new Point(lat, lon, getAltitude(true));
            p.Speed=getSpeed(false);
            Vector v=Util.parseString(getTime(false), ":", false);
            // Час
            p.DateTime[0]=Byte.parseByte((String)v.elementAt(0));
            // Минуты
            p.DateTime[1]=Byte.parseByte((String)v.elementAt(1));
            // Секунды
            p.DateTime[2]=Byte.parseByte((String)v.elementAt(2));
            //
            v=Util.parseString(getDate(false), "/", false);
            // День
            p.DateTime[3]=Byte.parseByte((String)v.elementAt(0));
            // Месяц
            p.DateTime[4]=Byte.parseByte((String)v.elementAt(1));
            // Год
            p.DateTime[5]=Byte.parseByte((String)v.elementAt(2));
            //
            MIDlet1.netManager.url = MIDlet1.rmsManager.addressNetGPS;
            String kmlData=p.getKML(false, " Course: "+getCourse(), MIDlet1.rmsManager.login+" - ");
            kmlData=kmlData.replace('\n',' ');
            kmlData=kmlData.replace('\r',' ');
            MIDlet1.netManager.query = "un=" + MIDlet1.rmsManager.login + "&pw=" +
                MIDlet1.rmsManager.password + "&cds=" + Util.URLencode(kmlData);
            // GET query
            MIDlet1.netManager.send(false, NetManager.POST_SEND_KML, null);
          }
      }
    }
    // Проверка на время и контроль HDOP
    if (precesionHDOP<=4.0 && current - lastSaved > MIDlet1.rmsManager.intervalTrack * 1000L)
    {      
      // Только для скорости > 1.0 knot
      if(getSpeed(false)>1.0)
      {
          boolean appendPoint=true;
          // Проверка на минимальное расстояние между точками трека
          if(MIDlet1.rmsManager.currentTrack.size()>1)
          {
              Point p1=(Point)MIDlet1.rmsManager.currentTrack.elementAt(MIDlet1.rmsManager.currentTrack.size()-1);
              // Расстояние в километрах
              double val=Util.distance(lat, lon, p1.Latitude, p1.Longitude);
              if(val*(MIDlet1.rmsManager.typeUnit==Util.UNIT_METRIC?1000.0:1000.0*Util.M2FT)<MIDlet1.rmsManager.distanceTrack)
                  appendPoint=false;
          }
          // Проверка на угол поворота
          if(MIDlet1.rmsManager.checkTurns && MIDlet1.rmsManager.currentTrack.size()>1)
          {
              //Проверяем угол между текущем положением и последними двумя сохраненными точками
              Point p1=(Point)MIDlet1.rmsManager.currentTrack.elementAt(MIDlet1.rmsManager.currentTrack.size()-1);
              Point p2=(Point)MIDlet1.rmsManager.currentTrack.elementAt(MIDlet1.rmsManager.currentTrack.size()-2);
              double angle=Math.toDegrees(Util.heading(p2.Latitude, p2.Longitude, p1.Latitude, p1.Longitude));
              // Отклонение на 15 градусов считается поворотом
              if(Math.abs(getCourse()-angle)<15.0)
                  appendPoint=false;
          }
          // Проверка на ускорение если не сработало по углу поворота
          /*
          if(appendPoint && MIDlet1.rmsManager.currentTrack.size()>0)
          {
              Point p1=(Point)MIDlet1.rmsManager.currentTrack.elementAt(MIDlet1.rmsManager.currentTrack.size()-1);
              // Время от последней точки в ms
              double difftime=proceedDateTime(false).getTime().getTime()-p1.getTime();
              // Расстояние от последней точки km
              double dist=Util.distance(this.getLatitude(), this.getLongitude(), p1.Latitude, p1.Longitude);
              // Скорость в узлах (m/s / Util.KT2KM)
              double ktspeed=(dist*1.0e6)/(difftime*Util.KT2KM);
              // Ускорение в m/s^2
              double acceleration=(Math.abs(ktspeed-p1.Speed)*Util.KT2KM*1000.0)/difftime;
              System.out.println("Acceleration: "+acceleration);
              // Если ускорение >4.0 m/s^2 значит ошибка измерений
              if(acceleration>4.0)
                  appendPoint=false;
          }*/
          //
          if(appendPoint)
          {
              lastSaved = current;
              MIDlet1.rmsManager.appendTrackPoint(lat, 
                                                    lon, 
                                                    getAltitude(true),
                                                    getSpeed(false),
                                                    getTime(false), 
                                                    getDate(false));
          }            
      }
    }
  }

  public void AnalyzeNMEA(String nmea)
  {
    MIDlet1.rmsManager.appendNMEA(nmea);
    // Удаляем контрольную сумму
    int pos=nmea.indexOf("*");
    if(!nmea.startsWith("$GP") || pos==-1)
      return;
    //
    /*
    StringBuffer sb=new StringBuffer();
    for(int i=0; i<nmea.length(); i++)
    {
      if (nmea.charAt(i) > 32)
        sb.append(nmea.charAt(i));
    }
    String sourceNmea=sb.toString();
*/
    String sourceNmea=new String(nmea.trim());
    nmea=new String(nmea.substring(0, pos));
    //
    Vector v=Util.parseString(nmea, ",", false);
    //
    try
    {
      lastCommand=(String)v.elementAt(0);
      if (nmea.startsWith("$GPGGA"))
      {
        // Fix Data
        positionFix=Integer.parseInt((String) v.elementAt(6));
        setValid(positionFix==1 || positionFix==2 || positionFix==3);
        time = new String((String) v.elementAt(1));
        if (valid)
        {
          String s = (String) v.elementAt(2) + (String) v.elementAt(3);
          latitude = s.substring(0, 2) + "-" + s.substring(2);
          s = (String) v.elementAt(4) + (String) v.elementAt(5);
          longitude = s.substring(0, 3) + "-" + s.substring(3);
          sattelites = (String) v.elementAt(7);
          precesionHDOP = Double.parseDouble((String) v.elementAt(8));
          altitude = (String) v.elementAt(9);
          if(MIDlet1.rmsManager.geoidCorrection!=1)
          {
              String correction = (String) v.elementAt(11);
              if(correction.length()>0)
              {
                  if(MIDlet1.rmsManager.geoidCorrection==0)
                      // Negative
                      altitude = ""+(Double.parseDouble(altitude) - Double.parseDouble(correction));
                  else
                  if(MIDlet1.rmsManager.geoidCorrection==2)
                      // Positive
                      altitude = ""+(Double.parseDouble(altitude) + Double.parseDouble(correction));
              }
          }
          //
          //this.addElement(new String(nmea));
          //dgpsTime = (String) v.elementAt(13);
          //dgpsID = (String) v.elementAt(14);
          //
          //sendToNetGPS(sourceNmea);
        }
      }
      else
      if (nmea.startsWith("$GPRMC"))
      {
        // Fix Data
        String s = (String) v.elementAt(2);
        setValid(s.startsWith("A"));
        time = new String((String) v.elementAt(1));
        if (valid)
        {
          s = (String) v.elementAt(3) + (String) v.elementAt(4);
          latitude = s.substring(0, 2) + "-" + s.substring(2);
          s = (String) v.elementAt(5) + (String) v.elementAt(6);
          longitude = s.substring(0, 3) + "-" + s.substring(3);
          s = (String) v.elementAt(9);
          date = new String(s);
          //
          speed=(String) v.elementAt(7);
          course=(String) v.elementAt(8);
          //
          sendToNetGPS(sourceNmea);
        }
      }
      else
      if (nmea.startsWith("$GPGLL"))
      {
        // Position
        String s = (String) v.elementAt(6);
        setValid(s.startsWith("A"));
        time = new String((String) v.elementAt(5));
        if (valid)
        {
          s = (String) v.elementAt(1) + (String) v.elementAt(2);
          latitude = s.substring(0, 2) + "-" + s.substring(2);
          s = (String) v.elementAt(3) + (String) v.elementAt(4);
          longitude = s.substring(0, 3) + "-" + s.substring(3);
          //
          //this.addElement(new String(nmea));
        }
      }
      else
      if (nmea.startsWith("$GPVTG")) 
      {        
        speed = (String) v.elementAt(5);
        // Курс для низких скоростей игнорируем т.к. есть неточность
        if(getSpeed(false)>1.0)
        {
            // Course over ground
            course = (String) v.elementAt(1);
        }
      }
      else
      if (nmea.startsWith("$GPZDA"))
      {
        // Course over ground
        String s = (String) v.elementAt(1);
        time = new String(s);
        String d = (String) v.elementAt(2);
        String m = (String) v.elementAt(3);
        String y = (String) v.elementAt(4);
        date = d+m+y.substring(2);
        //
        s = (String) v.elementAt(5);
        if (s.length() > 0)
            offset = Long.parseLong(s)*60L;
        s = (String) v.elementAt(6);
        if (s.length() > 0)
          offset += Long.parseLong(s);
      }
      else
      if (nmea.startsWith("$GPGSA"))
      {
        // Fix Data
        satelliteMode=Integer.parseInt((String)v.elementAt(2));
        setValid(satelliteMode==2 || satelliteMode==3);
        if (v.size()>17)
        {
          precesionPDOP=Double.parseDouble((String)v.elementAt(15));
          precesionHDOP=Double.parseDouble((String)v.elementAt(16));
          precesionVDOP=Double.parseDouble((String)v.elementAt(17));
          //
          satellites.removeAllElements();
          for(int i=0; i<satt.length; i++)
          {
            if(satt[i]!=null)
            {
                satt[i].active=false;
                // Ищем спутник в списке активных
                for(int j=0; j<12; j++)
                {
                    String s = (String) v.elementAt(j + 3);
                    if(s.length()>0 && satt[i].number==Integer.parseInt(s))
                        satt[i].active=true;
                }
                satellites.addElement(satt[i]);
            }
          }
        }
      }
      else
      if (nmea.startsWith("$GPGSV"))
      {
        String s = (String) v.elementAt(2);
        int number = Integer.parseInt(s);
        //visible = (String) v.elementAt(3);
        /*
        s = (String) v.elementAt(3);
        int count = Integer.parseInt(s);
        for(int i=count; i<satt.length; i++)
          satt[i]=null;*/
        //
        for (int i = 0; i < 4; i++)
        {
          if(v.size()<8+i*4)
            break;
          //
          Sattelite sat = new Sattelite();
          String PRN = (String) v.elementAt(4 * i + 4);
          if(PRN.length()==0)
          {
            satt[ (number - 1) * 4 + i] = null;
            continue;
          }
          sat.number = Integer.parseInt(PRN);
          String Height = (String) v.elementAt(4 * i + 5);
          if(Height.length()==0)
          {
            satt[ (number - 1) * 4 + i] = null;
            continue;
          }
          sat.height = Integer.parseInt(Height);
          String Azimuth = (String) v.elementAt(4 * i + 6);
          if(Azimuth.length()==0)
          {
            satt[ (number - 1) * 4 + i] = null;
            continue;
          }
          sat.azimuth = Integer.parseInt(Azimuth);
          String Power = (String) v.elementAt(4 * i + 7);
          if(Power.length()==0)
            sat.power=0;
          else
            sat.power = Integer.parseInt(Power);
          satt[ (number - 1) * 4 + i] = sat;
        }
      }
    }
    catch(Exception e)
    {
      // Игнорируем ошибки!
      //MIDlet1.screenManager.pushScreen(new InfoForm("NMEA", e.getMessage()+"\n"+nmea, null));
    }
    //
    //latitude="49-12.3300N";
    //longitude="016-37.6000E";
    //
    if(!valid)
      MIDlet1.rmsManager.trip.sleep();
    /*
    else
    if(nmea.startsWith("$GPGSA"))
    {
      for(int i=0; i<satt.length; i++)
      {
        if(3+i<v.size())
        {
          String s = (String) v.elementAt(3 + i);
          if (s != null & s.length() > 0)
            satt[i] = new String(s);
          else
            satt[i] = "";
        }
      }
    }*/
  }
/*
  public String getLast()
  {
    if(size()==0)
      return null;
    return (String)this.elementAt(size()-1);
  }*/

  public String getPositionMessage()
  {
      StringBuffer sb=new StringBuffer();
      //
      sb.append("MobiTrackPRO\n");
      sb.append(getDate(true)+"\n");
      sb.append(getTime(true)+"\n");
      sb.append(latitude+"\n");
      sb.append(longitude+"\n");
      sb.append(getSpeed(true)+" "+MIDlet1.rmsManager.getSpeedName()+"\n");
      sb.append(getCourse()+" degs");
      //
      return sb.toString();
  }

  public boolean getValid() 
  {
      return valid;
  }
  
  public void setValid(boolean newValid)
  {
    if(newValid!=valid)
    {
      long time=System.currentTimeMillis();
      if(newValid)
      {
        // Last change of state
        if(time-lastChangeState<5000L)
            return;
        Util.playSound(Util.SOUND_VALID);
      }
      else
      {
        lastChangeState=time;
        Util.playSound(Util.SOUND_INVALID);
        precesionHDOP=precesionPDOP=precesionVDOP=50.0;
      }
      valid=newValid;
    }
  }
  
  public double getPrecesionHDOP() 
  {
      return precesionHDOP;
  }
  
  public double getPrecesionVDOP() 
  {
      return precesionVDOP;
  }
  
  public double getPrecesionPDOP() 
  {
      return precesionPDOP;
  }
  
  public Vector getSattelites()
  {
    return satellites;
  }

  public int getPositionFix()
  {
    return positionFix;
  }
  
  public int getSatelliteMode()
  {
    return satelliteMode;
  }
}

