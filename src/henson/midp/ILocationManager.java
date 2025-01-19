package henson.midp;
/*
 * ILocationManager.java
 *
 * Created on 7 Февраль 2006 г., 12:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.util.Vector;

/**
 *
 * @author Administrator
 */
public interface ILocationManager 
{
    public boolean Open();
    public boolean Close();
    public String getDate(boolean useLocalFormat);
    public String getTime(boolean useLocalFormat);    
    public double getLatitude();
    public String getLatitudeString();
    public double getLongitude();
    public String getLongitudeString();
    public double getAltitude(boolean feet);
    // Если local=true скорость в пользовательских единицах
    // Если local=false скорость в узлах (knots)
    public double getSpeed(boolean local);
    public double getCourse();
    public String getPositionMessage();
    public boolean getValid();
    public void setValid(boolean valid);
    //
    public Vector getSattelites();
    public int getPositionFix();
    public int getSatelliteMode();
    //
    public double getPrecesionHDOP();
    public double getPrecesionVDOP();
    public double getPrecesionPDOP();
}
