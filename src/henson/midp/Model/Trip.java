package henson.midp.Model;

import java.io.*;
import henson.midp.*;

public class Trip
{
  private static long HOUR_MS = 3600000L;
  private static long MIN_MS = 60000L;
  // ����� �����, 1/1000th s
  public long timeTotal;
  // ����� � ��������, 1/1000th s
  public long timeMoving;
  // ������������ ��������, knots
  public double speedMax;
  // �������� �� ���������� ����, knots
  public double speedLast;
  // ����� ���������� �� �����������, km
  public double distanceTripOdometer;
  // �������, km
  public double distanceOdometer;
  // ����� �� ���������� ����, 1/1000th s
  public long timeLast;
  // ��������� ������
  public double latitudeLast;
  // ��������� �������
  public double longitudeLast;
  // ������� ���������, m/s^2
  public double accelerationCurrent;
  // ������������ ���������, m/s^2
  public double accelerationMax;
  // ����������� ���������, m/s^2
  public double accelerationMin;
  // ������� ������, m
  public double altitudeCurrent;
  // ������������ ������, m
  public double altitudeMax;
  // ����������� ������, m
  public double altitudeMin;
  // ���� �� ���������� ���� ���� �������� ������
  private boolean active;

  public Trip()
  {
    ResetTrip();
    ResetOdometer();
  }

  public void ResetTrip()
  {
    timeTotal=0;
    timeMoving=0;
    speedMax=0;
    speedLast=0;
    distanceTripOdometer=0;
    accelerationCurrent=accelerationMax=accelerationMin=0.0;
    altitudeCurrent=0.0;
    altitudeMax=-9999;
    altitudeMin=9999;
    active=false;
  }

  public void ResetOdometer()
  {
    distanceOdometer=0;
  }

  public void ResetAcceleration()
  {
    accelerationCurrent=accelerationMax=accelerationMin=0.0;
  }

  public void ResetAltitude()
  {
    altitudeCurrent=0.0;
    altitudeMax=-9999;
    altitudeMin=9999;
  }  

  public double getMaxSpeed()
  {
    double coeff=MIDlet1.rmsManager.getUnitCoeff(false);
    return Math.ceil(speedMax*Util.KT2KM*coeff*10.0)/10.0;
  }

  public double getAvgSpeed()
  {
    if(timeMoving==0.0)
      return 0.0;
    // ������� �������� � km/h
    double val=distanceTripOdometer*HOUR_MS/timeMoving;
    double coeff=MIDlet1.rmsManager.getUnitCoeff(false);
    return Math.ceil(val*coeff*10.0)/10.0;
  }

  public double getTotalAvgSpeed()
  {
    if(timeTotal==0.0)
      return 0.0;
    // ������� �������� � km/h
    double val=distanceTripOdometer*HOUR_MS/timeTotal;
    double coeff=MIDlet1.rmsManager.getUnitCoeff(false);
    return Math.ceil(val*coeff*10.0)/10.0;
  }

  public double getTripOdometerDistance()
  {
    double coeff=MIDlet1.rmsManager.getUnitCoeff(false);
    return Math.ceil(distanceTripOdometer * coeff * 100.0) / 100.0;
  }

  public double getOdometerDistance()
  {
    double coeff=MIDlet1.rmsManager.getUnitCoeff(false);
    return Math.ceil(distanceOdometer * coeff * 10.0) / 10.0;
  }

  public String getMovingTime()
  {
    long hour=timeMoving/HOUR_MS;
    long min=(timeMoving-hour*HOUR_MS)/MIN_MS;
    long sec=(timeMoving-hour*HOUR_MS-min*MIN_MS)/1000L;
    return hour+":"+Util.withLeadZero(min)+":"+Util.withLeadZero(sec);
  }

  public String getTotalTime()
  {
    long hour=timeTotal/HOUR_MS;
    long min=(timeTotal-hour*HOUR_MS)/MIN_MS;
    long sec=(timeTotal-hour*HOUR_MS-min*MIN_MS)/1000L;
    return hour+":"+Util.withLeadZero(min)+":"+Util.withLeadZero(sec);
  }

  public void sleep()
  {
    active=false;
  }

  public boolean append(long time, double speed, double latitude, double longitude, double altitude)
  {
    if(active)
    {
      long steptime=time-timeLast;
      // We count speed if >1.0 knot 
      if(speed>1.0)
      {
        if(speed>speedMax)
            speedMax=speed;
        //
        double step=Util.distance(latitudeLast, longitudeLast, latitude, longitude);
        // ��������� ������ �� �������������
        // ������� ��������
        double _avgspeed=(distanceTripOdometer+step)*HOUR_MS/(timeMoving+steptime);
        // �� ������ ���� ������ ������������
        double _maxspeed=speedMax*Util.KT2KM;
        //
        if(_avgspeed>_maxspeed)
            // ��������� ������(!)
            return false;
        //
        timeMoving+=steptime;
        distanceTripOdometer+=step;
        distanceOdometer+=step;
        // �� ��������� �������� ����� 0 �.�. �������� ����� ������
        if(speed>0 && speedLast>1.0 && steptime>500L)
        {
            double acc=((speed-speedLast)*Util.KT2KM*1.0e6)/((double)steptime*3600.0);
            if(acc>-6.0 && acc<6.0)
            {
                accelerationCurrent=acc;
                if(accelerationCurrent<accelerationMin)
                    accelerationMin=accelerationCurrent;
                if(accelerationCurrent>accelerationMax)
                    accelerationMax=accelerationCurrent;
            }
        }
      }
      else
      {
        if(speedLast<=1.0)
            accelerationCurrent=0.0;
      }
      timeTotal+=steptime;
    }
    timeLast=time;
    speedLast=speed;
    latitudeLast=latitude;
    longitudeLast=longitude;
    //
    altitudeCurrent = altitude;    
    if(altitudeCurrent<altitudeMin)
        altitudeMin=altitudeCurrent;
    if(altitudeCurrent>altitudeMax)
        altitudeMax=altitudeCurrent;
    // ����������
    active=true;
    //
    return true;
  }

  public void load(DataInputStream dis) throws IOException
  {
    timeTotal=dis.readLong();
    timeMoving=dis.readLong();
    speedMax=dis.readDouble();
    distanceTripOdometer=dis.readDouble();
    distanceOdometer=dis.readDouble();
    accelerationMax=dis.readDouble();
    accelerationMin=dis.readDouble();
    altitudeMax=dis.readDouble();
    altitudeMin=dis.readDouble();
    active=false;
  }

  public void save(DataOutputStream dos) throws IOException
  {
    dos.writeLong(timeTotal);
    dos.writeLong(timeMoving);
    dos.writeDouble(speedMax);
    dos.writeDouble(distanceTripOdometer);
    dos.writeDouble(distanceOdometer);
    dos.writeDouble(accelerationMax);
    dos.writeDouble(accelerationMin);
    dos.writeDouble(altitudeMax);
    dos.writeDouble(altitudeMin);
  }

  public double getAcceleration()
  {
    double coeff=MIDlet1.rmsManager.getUnitCoeff(true);
    return Math.ceil(accelerationCurrent*coeff*100.0)/100.0;
  }
  
  public double getMaxAcceleration()
  {
    double coeff=MIDlet1.rmsManager.getUnitCoeff(true);
    return Math.ceil(accelerationMax*coeff*100.0)/100.0;
  }
  
  public double getMinAcceleration()
  {
    double coeff=MIDlet1.rmsManager.getUnitCoeff(true);
    return Math.ceil(accelerationMin*coeff*100.0)/100.0;
  }

  public double getAltitude()
  {
    double coeff=MIDlet1.rmsManager.getUnitCoeff(true);
    return Math.ceil(altitudeCurrent*coeff*100.0)/100.0;
  }
  
  public double getMaxAltitude()
  {
    double coeff=MIDlet1.rmsManager.getUnitCoeff(true);
    return Math.ceil(altitudeMax*coeff*100.0)/100.0;
  }
  
  public double getMinAltitude()
  {
    double coeff=MIDlet1.rmsManager.getUnitCoeff(true);
    return Math.ceil(altitudeMin*coeff*100.0)/100.0;
  }
}
