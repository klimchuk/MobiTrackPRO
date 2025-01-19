/*
 * Rect.java
 *
 * Created on 24 январь 2006 г., 3:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package henson.midp.Model;

/**
 *
 * @author Administrator
 */
public class Rect
{
    public double startLongitude;
    public double startLatitude;
    public double widthLongitude;
    public double heightLatitude;
    
    public Rect()
    {
        
    }
    
    /** Creates a new instance of Rect */
    public Rect(double lon, double lat, double width, double height)
    {
        this.startLongitude=lon;
        this.startLatitude=lat;
        this.widthLongitude=width;
        this.heightLatitude=height;
    }
    
    public boolean isInside(double lon, double lat)
    {
        if(lon<startLongitude || lon>startLongitude+widthLongitude || 
                lat<startLatitude || lat>startLatitude+heightLatitude)
            return false;
        else
            return true;
    }
}
