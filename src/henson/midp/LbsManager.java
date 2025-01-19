/*
 * LbsManager.java
 *
 * Created on 7 Февраль 2006 г., 2:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package henson.midp;

import java.util.*;
import javax.microedition.lcdui.*;
import javax.microedition.location.*;

import henson.midp.Model.*;
import henson.midp.View.*;

/**
 *
 * @author Administrator
 */
public class LbsManager implements LocationListener, ILocationManager, Runnable
{
    private LocationProvider lp=null;
    
    private boolean valid=false;
    private double latitude=0.0;
    private double longitude=0.0;
    private double altitude=0.0;
    private double speed=0.0;
    private double course=0.0;
    private long timeStamp=0L;

    private double precesionHDOP=0.0;
    private double precesionVDOP=0.0;
    private double precesionPDOP=0.0;
    
    private String extraInfo;

    private int positionFix=0;
    private int satelliteMode=0;
    
    // NetGPS/KMLGPS
    long lastSent=0L;
    // Track
    long lastSaved=0L;
    
    private Sattelite[] satt=new Sattelite[12];
    private Vector satellites=new Vector();
   
    private Thread t=null;
    
    /** Creates a new instance of LbsManager */
    public LbsManager() 
    {
        for(int i=0; i<satt.length; i++)
          satt[i]=null;
    }
    
    public boolean Open()
    {
        Criteria cr=new Criteria();
        cr.setHorizontalAccuracy(500);
        try
        {
            lp=LocationProvider.getInstance(cr);
            lp.setLocationListener(this, -1, -1, -1);
            //t=new Thread(this);
            //t.start();
        }
        catch(Exception le)
        {
            MIDlet1.screenManager.pushScreen(new InfoForm("Open", le.getMessage(), null));
            lp=null;
        }        
        return (lp!=null);
    }
    
    public boolean Close()
    {
        if(t!=null)
            t=null;
        //
        if(lp!=null)
        {
            lp.setLocationListener((LocationListener)null, -1, -1, -1);
            lp=null;
        }
        //
        return true;
    }
    
    private void AnalyzeNMEA(String nmea)
    {        
        if(nmea==null)
            return;
        MIDlet1.rmsManager.appendNMEA(nmea);
        //
        Vector sentences=Util.parseString(nmea,"\n",true);
        if(sentences==null)
            return;
        for(int n=0; n<sentences.size(); n++)
        {
            String s=(String)sentences.elementAt(n);
            int pos=s.indexOf("*");
            s=new String(s.substring(0, pos));
            try
            {
                  Vector v=Util.parseString(s, ",", false);
                  if (s.startsWith("$GPGSA"))
                  {
                    // Fix Data
                    satelliteMode=Integer.parseInt((String)v.elementAt(2));
                    setValid(satelliteMode==2 || satelliteMode==3);
                    if (valid && v.size()>17)
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
                                String x = (String) v.elementAt(j + 3);
                                if(x.length()>0 && satt[i].number==Integer.parseInt(x))
                                    satt[i].active=true;
                            }
                            satellites.addElement(satt[i]);
                        }
                      }
                    }
                  }            
                  else
                  if (s.startsWith("$GPGSV"))
                  {
                        String x = (String) v.elementAt(2);
                        int number = Integer.parseInt(x);
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
                MIDlet1.screenManager.pushScreen(new InfoForm("NMEA", e.getMessage()+"\n"+s, null));
            }
        }
    }
    
    public void locationUpdated(LocationProvider provider, Location location)
    {
        setValid(location.isValid());
        if(valid)
        {
            QualifiedCoordinates qc=location.getQualifiedCoordinates();
            latitude=qc.getLatitude();
            longitude=qc.getLongitude();
            altitude=qc.getAltitude();        
            speed=location.getSpeed();
            course=location.getCourse();
            timeStamp=location.getTimestamp();
            //precesionHDOP=qc.getHorizontalAccuracy();
            //precesionVDOP=qc.getVerticalAccuracy();        
            extraInfo=location.getExtraInfo("text/plain");
            //
            String nmea=location.getExtraInfo("application/X-jsr179-location-nmea");
            Vector vvx=Util.parseString(nmea, "\n", true);
            if(vvx!=null && vvx.size()>0)
            {
                for(int k=0; k<vvx.size(); k++) 
                {
                    String str=(String)vvx.elementAt(k);
                    if(str.startsWith("$GP"))
                        AnalyzeNMEA(str);
                }
            }
            sendToNetGPS(nmea);
        }
    }
    
    public void providerStateChanged(LocationProvider provider, int newState)
    {
        switch(newState)
        {
            case LocationProvider.AVAILABLE:
                setValid(true);
                break;
            case LocationProvider.OUT_OF_SERVICE:
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                setValid(false);
                break;
        }
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
        if(valid)
            return latitude;
        else
            return MIDlet1.rmsManager.defaultLatitude;
    }

    public String getLatitudeString()
    {
        double d=getLatitude();
        if(d<0.0)
            return Coordinates.convert(-getLatitude(), Coordinates.DD_MM)+"S";
        else
            return Coordinates.convert(getLatitude(), Coordinates.DD_MM)+"N";
    }
    
    public double getLongitude()
    {
        if(valid)
            return longitude;
        else
            return MIDlet1.rmsManager.defaultLongitude;
    }

    public String getLongitudeString()
    {
        double d=getLongitude();
        if(d<0.0)
            return Coordinates.convert(-d, Coordinates.DD_MM)+"W";
        else
            return Coordinates.convert(d, Coordinates.DD_MM)+"E";
    }
    
    public double getAltitude(boolean feet)
    {
        double coeff=1;
        if(MIDlet1.rmsManager.typeUnit==Util.UNIT_ENGLISH ||
                MIDlet1.rmsManager.typeUnit==Util.UNIT_NAUTICAL ||
                feet) 
        {
            // m -> ft
            coeff=Util.M2FT;
        }
        return Math.ceil(altitude*coeff*10.0)/10.0;
    }
        
    public double getSpeed(boolean local)
    {
        if(Double.isNaN(speed))
            return 0.0;
        //
        if(local)
        {
            // Конвертируем метры в секунду в текущие единицы скорости
            double coeff=MIDlet1.rmsManager.getUnitCoeff(false);
            return Math.ceil(speed*Util.MS2KMH*coeff*10.0)/10.0;
        }
        else
            // Конвертируем метры в секунды в узлы
            return Math.ceil(speed*Util.MS2KMH*10.0/Util.KT2KM)/10.0;
    }
    
    public double getCourse()
    {
        return Math.ceil(course*10.0)/10.0;
    }
    
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
        sb.append(course+" degs");
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
          if(newValid)
            Util.playSound(Util.SOUND_VALID);
          else
            Util.playSound(Util.SOUND_INVALID);
        }
        valid=newValid;
    }

    public double getPrecesionHDOP()
    {
        return Math.ceil(precesionHDOP*10.0)/10.0;
    }

    public double getPrecesionVDOP()
    {
        return precesionVDOP;
    }
    
    public double getPrecesionPDOP()
    {
        return precesionPDOP;
    }   

  private Calendar proceedDateTime(boolean local)
  {
      if(timeStamp==0L)
          return null;
      //
      Calendar cal=Calendar.getInstance();
      try
      {
          cal.setTime(new Date(timeStamp));
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
        if(!MIDlet1.rmsManager.trip.append(msTime, getSpeed(false), lat, lon) &&
            MIDlet1.rmsManager.points.size()>0)
            return;
      }
    }
    // По умолчанию точка маршрута
    Point point=MIDlet1.rmsManager.routePoint;
    // Проверяем расстояние и переключаемся если надо
    if(point!=null)
    {
        double val=Util.distance(lat, lon, point.Latitude, point.Longitude);
        if(val*1000.0<(MIDlet1.rmsManager.typeUnit==Util.UNIT_METRIC?MIDlet1.rmsManager.pointMarginNext:MIDlet1.rmsManager.pointMarginNext/Util.M2FT))
        {
            // Переключаемся на другую точку если маршрут            
            for(int i=0; i<MIDlet1.rmsManager.route.size(); i++)
            {
                Point p=(Point)MIDlet1.rmsManager.route.elementAt(i);
                if(p==point)
                {
                    if(i+1<MIDlet1.rmsManager.route.size())
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
                    break;
                }
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
    if (precesionHDOP<=4.0 && current - lastSent > MIDlet1.rmsManager.intervalNetGPS * 1000L)
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
  
  public void run()
  {
      while(t!=null && lp!=null)
      {
          try
          {
             Location l=lp.getLocation(10);
             locationUpdated(lp, l);
          }
          catch(Exception le)
          {
            setValid(false);
          }
      }
  }
}
