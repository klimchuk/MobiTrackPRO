package henson.midp.Model;

import java.util.*;

import henson.midp.*;

public class Point
{
  public final static int TYPE_DATETIME=0;
  public final static int TYPE_DATE=1;
  public final static int TYPE_TIME=2;
    
  public String Name="";
  // Тип
  // 0 - Default (No icon)
  // 1 - Dining palette-2.png   224,0
  // 2 - Lodging    palette-2.png   128,128
  // 3 - ATM    palette-2.png   64,0
  // 4 - Bar    palette-2.png   96,128
  // 5 - Coffee palette-2.png   192,0
  // 6 - Mall   palette-2.png   160,192
  // 7 - Movie  palette-2.png   192,128
  // 8 - Store  palette-3.png   64,128
  // 9 - Pharmacy   palette-2.png   32,192
  // 10 - Gas   palette-2.png   160,128
  // 11 - Sport palette-2.png   32,0
  // 12 - Park  palette-2.png   128,192
  // 13 - Hospital palette-3.png    192,64
  // 14 - School    palette-2.png   64,192
  // 15 - Churche   palette-2.png   96,192
  // 16 - Airport   palette-2.png   0,0
  // 17 - Service   palette-4.png   224,192  
  // 18 - Building  palette-3.png   160,160
  public byte Category=0;
  // Широта
  public double Latitude=0.0;
  // Долгота
  public double Longitude=0.0;
  // Высота в футах
  public double Altitude=0.0;
  // Скорость в узлах
  public double Speed=0.0;
  // Расстояние до конца трека
  public double Distance=0;
  // Новый
  public boolean appendWaypoint=false;
  // Сегмент трека
  public int segment = -1;
  //
  public byte[] DateTime=new byte[6];

  public Point(double lat, double lon, double alt)
  {
    Latitude=lat;
    Longitude=lon;
    Altitude=alt;
  }

  public double OleDateFromTm()
  {
    int[] rgMonthDays = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365 };
    int wHour=DateTime[0];
    int wMinute=DateTime[1];
    int wSecond=DateTime[2];
    int wDay=DateTime[3];
    int wMonth=DateTime[4];
    int wYear=DateTime[5]+2000;
    if(DateTime[5]>70)
        wYear=DateTime[5]+1900;
      // Validate year and month (ignore day of week and milliseconds)
      if (wYear > 9999 || wMonth < 1 || wMonth > 12)
            return 0.0;

      //  Check for leap year and set the number of days in the month
      boolean bLeapYear = ((wYear & 3) == 0) &&
            ((wYear % 100) != 0 || (wYear % 400) == 0);

      int nDaysInMonth =
            rgMonthDays[wMonth] - rgMonthDays[wMonth-1] +
            ((bLeapYear && wDay == 29 && wMonth == 2) ? 1 : 0);

      // Finish validating the date
      if (wDay < 1 || wDay > nDaysInMonth ||
            wHour > 23 || wMinute > 59 ||
            wSecond > 59)
      {
            return 0.0;
      }

      // Cache the date in days and time in fractional days
      long nDate;
      double dblTime;

      //It is a valid date; make Jan 1, 1AD be 1
      nDate = wYear*365L + wYear/4 - wYear/100 + wYear/400 + rgMonthDays[wMonth-1] + wDay;

      //  If leap year and it's before March, subtract 1:
      if (wMonth <= 2 && bLeapYear)
            --nDate;

      //  Offset so that 12/30/1899 is 0
      nDate -= 693959L;

      dblTime = (((long)wHour * 3600L) +  // hrs in seconds
            ((long)wMinute * 60L) +  // mins in seconds
            ((long)wSecond)) / 86400.;

      return (double) nDate + ((nDate >= 0) ? dblTime : -dblTime);
  }

  public String getLatitude()
  {
    boolean south=(Latitude<0.0);
    int lathour=(int)Math.abs(Latitude);
    double latmin=(Math.abs(Latitude)-lathour)*60.0;
    String shour=""+lathour;
    while(shour.length()<2)
      shour="0"+shour;
    double smin=Math.ceil(latmin*10000.0)/10000.0;
    return shour+"-"+(smin<10.0?"0"+smin:""+smin)+(south?"S":"N");
  }

  public String getLongitude()
  {
    boolean west=(Longitude<0.0);
    int lonhour = (int)Math.abs(Longitude);
    double lonmin = (Math.abs(Longitude) - lonhour) * 60.0;
    String shour=""+lonhour;
    while(shour.length()<3)
      shour="0"+shour;
    double smin=Math.ceil(lonmin*10000.0)/10000.0;
    return shour+"-"+(smin<10.0?"0"+smin:""+smin)+(west?"W":"E");
  }
  
  public long getTime()
  {
      Calendar cal=Calendar.getInstance();
      try
      {
          // Время
          cal.set(Calendar.HOUR_OF_DAY, DateTime[0]);
          cal.set(Calendar.MINUTE, DateTime[1]);
          cal.set(Calendar.SECOND, DateTime[2]);
          // Дата
          cal.set(Calendar.DAY_OF_MONTH, DateTime[3]);
          cal.set(Calendar.MONTH, DateTime[4]-1);
          if(DateTime[5]>70)
            cal.set(Calendar.YEAR, 1900+DateTime[5]);
          else
            cal.set(Calendar.YEAR, 2000+DateTime[5]);
          cal.set(Calendar.MILLISECOND, 0);
      }
      catch(Exception ex)
      {
          return 0L;
      }      
      return cal.getTime().getTime();
  }
  
  public String getDateTime(int type)
  {
      StringBuffer sb=new StringBuffer();
      Calendar cal=Calendar.getInstance();
      try
      {
          // Время
          cal.set(Calendar.HOUR_OF_DAY, DateTime[0]);
          cal.set(Calendar.MINUTE, DateTime[1]);
          cal.set(Calendar.SECOND, DateTime[2]);
          // Дата
          cal.set(Calendar.DAY_OF_MONTH, DateTime[3]);
          cal.set(Calendar.MONTH, DateTime[4]-1);
          if(DateTime[5]>70)
            cal.set(Calendar.YEAR, 1900+DateTime[5]);
          else
            cal.set(Calendar.YEAR, 2000+DateTime[5]);
          //
          Date d=cal.getTime();
          long locald=d.getTime()+(long)MIDlet1.rmsManager.utcOffset*60000L;
          cal.setTime(new Date(locald));
          // Дата
          if(type==TYPE_DATETIME || type==TYPE_DATE)
            sb.append(MIDlet1.rmsManager.getDate(cal, true)+" ");
          // Время
          if(type==TYPE_DATETIME || type==TYPE_TIME)
            sb.append(MIDlet1.rmsManager.getTime(cal, true));
      }
      catch(Exception ex)
      {
          cal=null;
      }      
      //
      return sb.toString();
  }
  
  public String getKML(boolean route, String beforeDescription, String beforeName)
  {
      StringBuffer sb=new StringBuffer();
      //
      if(route)
      {
            Vector v=Util.getDistanceFromKM(Distance);
            sb.append("<description>"+beforeDescription+"Distance: "+(Double)v.elementAt(1)+" "+(String)v.elementAt(0)+"</description>\r\n");
            sb.append("<name>"+beforeName+Name+"</name>\r\n");
            sb.append("<styleUrl>#routePoints</styleUrl>\r\n");          
      }
      else
      {
        double speed=Math.ceil(Speed*Util.KT2KM*MIDlet1.rmsManager.getUnitCoeff(false)*10.0)/10.0;
        double altitude=Math.ceil(Altitude*10.0)/10.0;
        if(MIDlet1.rmsManager.typeUnit==Util.UNIT_METRIC)
            altitude=Math.ceil(Altitude*10.0/Util.M2FT)/10.0;
        //
        sb.append("<description>"+getDateTime(Point.TYPE_DATE)+beforeDescription+" Speed: "+speed+" "+MIDlet1.rmsManager.getSpeedName()+" Altitude: "+altitude+" "+MIDlet1.rmsManager.getDistanceName(false)+"</description>\r\n");
        if(segment==0)
            sb.append("<name>"+beforeName+getDateTime(Point.TYPE_TIME)+"</name>\r\n");            
        else
            sb.append("<name>"+beforeName+segment+"> "+getDateTime(Point.TYPE_TIME)+"</name>\r\n");
        sb.append("<styleUrl>#trackPoints</styleUrl>\r\n");
      }
      //
      sb.append("<Point>\r\n");
      sb.append("<coordinates>\r\n");
      //
      sb.append(Longitude+","+Latitude+","+(Altitude/Util.M2FT)+" ");
      //
      sb.append("\r\n</coordinates>\r\n");
      sb.append("</Point>\r\n");
      //
      return sb.toString();
  }
  
  public long checkDifference(Point p)
  {
      return (DateTime[0]-p.DateTime[0])*3600+
              (DateTime[1]-p.DateTime[1])*60+
              (DateTime[2]-p.DateTime[2]);
  }

  public double getAltitude()
  {
    double coeff=1;
    if(MIDlet1.rmsManager.typeUnit==Util.UNIT_METRIC)
    {
      // m -> ft
      coeff=1.0/Util.M2FT;
    }
    return Math.ceil(Altitude*coeff*10.0)/10.0;
  }
}
