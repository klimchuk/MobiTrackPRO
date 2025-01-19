/*
 * GMap.java
 *
 * Created on 31 Июль 2005 г., 20:53
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
public class GMap 
{
    // спутниковые снимки
    public double centerLatitude;
    public double centerLongitude;
    public double sizeLatitude;
    public double sizeLongitude;
    /** Creates a new instance of GMap */
    public GMap() 
    {
    }   
   
    public String getMapURL()
    {
        return "http://mt";
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
    
    private double sinh(double x)
    {
        return (Float11.exp(x)-Float11.exp(-x))/2;
    }

    private double getMercatorLatitude(double Y)
    {
        return Math.toDegrees(Float11.atan(sinh(Y)));
    }
/*
    public String getMapQuery(int zoom, double latitude, double longitude)
    {
        int count=0;
        switch(zoom)
        {
            case 25: count=17; break;
            case 50: count=12; break;
            case 75: count=11; break;
            case 100: count=10; break;
            case 125: count=9; break;
            case 150: count=8; break;
            case 175: count=7; break;
            case 200: count=6; break;
        }
        // количество элементов по x, y
        long size=(long)pow2(17-count);
        //
        double dx=360.0/size;
        //double dy=size/180.0;
        //
        long x=(long)Math.floor(longitude/dx);
        double YMax=getMercatorY(85);
        double coeff=getMercatorY(latitude)/YMax;
        double coeff1=getMercatorY(latitude-1.0)/YMax;
        long y=(long)Math.floor(coeff*size/2);
        // Позиция по оси Y
        double yp=coeff*size*256/2;
        double yp1=coeff1*size*256/2;
        // Количество градусов широты на 1 пиксел карты
        double ycoeff=1/(yp-yp1);
        //
        centerLongitude=dx/2+x*dx;
        double startLatitude=latitude-(yp%256)*ycoeff;
        centerLatitude=startLatitude+128*ycoeff;
        //
        sizeLongitude=dx;
        sizeLatitude=256*ycoeff;
        //
        return "v=w2.5&x="+(x+size/2)+"&y="+(size/2-1L-y)+"&zoom="+count;
    }*/
    
    public String getMapQuery(int zoom, double latitude, double longitude)
    {
        int count=0;
        switch(zoom)
        {
            case 25: count=9; break;
            case 50: count=8; break;
            case 75: count=7; break;
            case 100: count=6; break;
            case 125: count=5; break;
            case 150: count=4; break;
            case 175: count=3; break;
            case 200: count=2; break;
            case 225: count=1; break;
            case 250: count=0; break;
        }
        // количество элементов по x, y
        long mapsize=(long)pow2(17-count+8);
        long origin=mapsize/2;
        //
        double longdeg=Math.abs(-180.0-longitude);
        double longppd=(double)mapsize/360.0;
        double longppdrad=(double)mapsize/(2*Math.PI);
        double pixelx=longdeg*longppd;
        long tilex=(long)Math.floor(pixelx/256);        
        //
        double pixely=origin+getMercatorY(latitude)*(-longppdrad);
        long tiley=(long)Math.floor(pixely/256);
        //
        double pixely1=origin+getMercatorY(latitude-1.0)*(-longppdrad);
        // Количество градусов широты на 1 пиксел карты
        double ycoeff=1/(pixely1-pixely);
        centerLongitude=(tilex*256.0+128.0)/longppd-180.0;
        double startLatitude=latitude-(256-(pixely-tiley*256))*ycoeff;
        centerLatitude=startLatitude+128*ycoeff;
        //
        sizeLongitude=256/longppd;
        sizeLatitude=256*ycoeff;   
        //
        long server=((tilex&1)+((tiley&1L)<<1L))%4L;
        return server+".google.com/mt?n=404&v=w2.11&x="+tilex+"&y="+tiley+"&zoom="+count;
    }
        
    public String getSatURL()
    {
        return "http://kh";
    }

    public String getSatQuery(int zoom, double latitude, double longitude)
    {
        int count=0;
        switch(zoom)
        {
            case 25: count=9; break;
            case 50: count=8; break;
            case 75: count=7; break;
            case 100: count=6; break;
            case 125: count=5; break;
            case 150: count=4; break;
            case 175: count=3; break;
            case 200: count=2; break;
            case 225: count=1; break;
            case 250: count=0; break;
        }
        // количество элементов по x, y
        long mapsize=(long)pow2(17-count+8);
        long origin=mapsize/2;
        //
        double longdeg=Math.abs(-180.0-longitude);
        double longppd=(double)mapsize/360.0;
        double longppdrad=(double)mapsize/(2*Math.PI);
        double pixelx=longdeg*longppd;
        long tilex=(long)Math.floor(pixelx/256);        
        //
        double pixely=origin+getMercatorY(latitude)*(-longppdrad);
        long tiley=(long)Math.floor(pixely/256);
        //
        double pixely1=origin+getMercatorY(latitude-1.0)*(-longppdrad);
        // Количество градусов широты на 1 пиксел карты
        double ycoeff=1/(pixely1-pixely);
        centerLongitude=(tilex*256.0+128.0)/longppd-180.0;
        double startLatitude=latitude-(256-(pixely-tiley*256))*ycoeff;
        centerLatitude=startLatitude+128*ycoeff;
        //
        sizeLongitude=256/longppd;
        sizeLatitude=256*ycoeff;   
        // Собираем строку запроса
        StringBuffer sb=new StringBuffer();
        long _x=tilex;
        long _y=tiley;        
        for(int i=0; i<17-count; i++)
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
                   sb.append("q");
               else
                   sb.append("t");
            }
            else
            {
               if(ry==0) 
                   sb.append("r");
               else
                   sb.append("s");                
            }
        }
        //
        long server=((tilex&1)+((tiley&1L)<<1L))%4L;
        return server+".google.com/kh?n=404&v=6&t=t"+sb.reverse().toString();
    }
}
