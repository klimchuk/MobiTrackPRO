/*
 * GMap.java
 *
 * Created on 31 »юль 2005 г., 20:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package henson.midp.Model;

import henson.midp.Float11;

/**
 *
 * @author Administrator
 */
public class VEarth 
{
    // спутниковые снимки
    public double centerLatitude;
    public double centerLongitude;
    public double sizeLatitude;
    public double sizeLongitude;
    //
    //private long offsetMeters = 20971520L;
    private long offsetMeters = 20037509L;
    //private long baseMetersPerPixel=163840L;
    private double baseMetersPerPixel=156543.04;
    private long earthRadius=6378137L;
    
    public String name="";
            
    /** Creates a new instance of GMap */
    public VEarth() 
    {
    }   
   
    public String getMapURL()
    {
        //return "http://tiles";
        return "http://r";
    }

    private double pow2(int n)
    {
        long result=1L;
        for(int i=0; (n>0?i<n:i<-n); i++)
            result*=2;
        //
        if(n>0)
            return (double)result;
        else
            return 1.0/(double)result;
    }            
    
    private double getMercatorY(double latitude)
    {
        double sinlat=Math.sin(Math.toRadians(latitude));
        if(sinlat<-0.9999)
            sinlat=-0.9999;
        if(sinlat>0.9999)
            sinlat=0.9999;
        return Float11.log((1+sinlat)/(1-sinlat))/2;
    }
    
    private double _MetersPerPixel(int zl)
    {
        return this.baseMetersPerPixel/(1<<zl);
    }
    
    private long _LatToY(double latitude, int zl)
    {
        double sinlat=Math.sin(Math.toRadians(latitude));
        double metersY=(double)earthRadius/2.0*Float11.log((1+sinlat)/(1-sinlat));
        double metersPerPixel=_MetersPerPixel(zl);
        return (long)Math.floor((offsetMeters-metersY)/metersPerPixel);
    }

    private long _LonToX(double longitude, int zl)
    {
        double metersX=earthRadius*Math.toRadians(longitude);
        double metersPerPixel=_MetersPerPixel(zl);
        return (long)Math.floor((metersX+offsetMeters)/metersPerPixel);
    }
    
    private String getCommonQuery(int zoom, double latitude, double longitude, boolean sat)
    {
        int zl=0;
        switch(zoom)
        {
            case 25: zl=8; break;
            case 50: zl=9; break;
            case 75: zl=10; break;
            case 100: zl=11; break;
            case 125: zl=12; break;
            case 150: zl=13; break;
            case 175: zl=14; break;
            case 200: zl=15; break;
            case 225: zl=16; break;
            case 250: zl=17; break;
        }
        //
        long pixelx=_LonToX(longitude, zl);
        long tilex=pixelx>>8;
        long pixely=_LatToY(latitude, zl);
        long tiley=pixely>>8;
        //
        long pixelx1=_LonToX(longitude+1.0, zl);
        long xcoeff=pixelx1-pixelx;
        long offsetTileX=pixelx-(tilex<<8);
        sizeLongitude=256.0/(double)xcoeff;
        centerLongitude=longitude-(double)offsetTileX/(double)xcoeff+sizeLongitude/2;
        //
        long pixely1=_LatToY(latitude+1.0, zl);
        long ycoeff=pixely-pixely1;
        long offsetTileY=pixely-(tiley<<8);
        sizeLatitude=256.0/(double)ycoeff;
        centerLatitude=latitude+(double)offsetTileY/(double)ycoeff-sizeLatitude/2;
        // —обираем строку запроса
        StringBuffer sb=new StringBuffer();
        long _x=tilex;
        long _y=tiley;
        //
        long server=((_x&1)+((_y&1L)<<1L))%4L;
        for(int i=0; i<zl; i++)
        {
            long rx=_x%2;
            _x=_x>>1;
            //
            long ry=_y%2;
            _y=_y>>1;
            //
            if(rx==0)
            {
               if(ry==0) 
                   sb.append("0");
               else
                   sb.append("2");
            }
            else
            {
               if(ry==0) 
                   sb.append("1");
               else
                   sb.append("3");                
            }
        }
        name=(sat?'h':'r')+sb.reverse().toString();
        //
        //return server+".virtualearth.msn.com/tiles/"+name;
        return server+".ortho.tiles.virtualearth.net/tiles/"+name;
    }   

    public String getMapQuery(int zoom, double latitude, double longitude)
    {
        return getCommonQuery(zoom, latitude, longitude, false)+".png?g=22";
    }
        
    public String getSatURL()
    {
        //return "http://tiles";
        return "http://h";
    }
    
    public String getSatQuery(int zoom, double latitude, double longitude)
    {
        return getCommonQuery(zoom, latitude, longitude, true)+".jpeg?g=22";
    }
}
