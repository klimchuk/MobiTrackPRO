package henson.midp;

import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import java.io.*;
import java.util.*;
import henson.midp.View.*;
import henson.midp.Model.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class NetManager implements Runnable
{
  public final static int POST_SEND_TRACK=0;
  public final static int POST_SEND_MESSAGE=1;
  public final static int POST_RECEIVE_MESSAGES=2;
  public final static int GET_GOOGLE_MAP=3;
  public final static int GET_GOOGLE_SAT=4;
  public final static int GET_VEARTH_MAP=5;
  public final static int GET_VEARTH_SAT=6;
  public final static int POST_SEND_KML=7;
  //
  private HttpConnection conn=null;
  private Thread t=null;
  public String url="";
  public String query="";
  public boolean silent=false;
  //
  public int success=0;
  public int failed=0;
  public int lastCode=0;
  public String lastMessage="";
  // тип запроса GET?
  boolean get=false;
  int type=-1;
  Object obj=null;
  //
  long netBusyTime=0L;
  // Сохраняем zoomMode на случай если его изменят пока мы будем грузить картинку
  int zoomMode=0;
  //
  public NetManager()
  {
  }

  public void close()
  {
    if (conn != null)
    {
      try {
        conn.close();
      }
      catch (IOException ex) {
      }
      conn=null;
    }
  }

  public boolean isBusy()
  {
    return t!=null && t.isAlive();
  }

  /**
   * Выполнить HTTP запрос
   * @param method метод передачи параметров HttpConnection.POST или HttpConnection.GET
   * @param url адрес сервера
   * @param data данные для отправки
   * @return
   */
  public boolean send(boolean get, int type, Object o)
  {
    if(isBusy())
      return false;
    //
    this.get=get;
    this.type=type;
    obj=o;
    //
    t=new Thread(this);
    t.start();
    //
    return true;
  }

  public synchronized long getBusyTime()
  {
    return netBusyTime;
  }

  /**
   * Отдельный поток для отправки запроса
   */
  public void run()
  {
    InputStream is=null;
    OutputStream os=null;
    //
    try
    {
      long start=System.currentTimeMillis();
      //Util.playSound(Util.SOUND_NETWORK);
      RmsManager.setDebugInfo("Ready for connection");
      if(get)
      {
        conn = (HttpConnection) Connector.open(url + query);
        //System.out.println("GET "+url + "?" + query);
        RmsManager.setDebugInfo("Connected: "+url + query);
        //System.out.println("Connected: "+url + query);
        conn.setRequestMethod(HttpConnection.GET);
      }
      else
      {
          conn = (HttpConnection) Connector.open(url);
          conn.setRequestMethod(HttpConnection.POST);
          conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
          //
          byte[] data=query.getBytes();
          conn.setRequestProperty("Content-Length", Integer.toString(data.length));
          //
          os = conn.openOutputStream();
          os.write(data);
          // для совместимости с SE T610
          //os.flush();
      }
      conn.setRequestProperty("Connection", "close");
      /*
      conn.setRequestProperty("Content-Type", "application/octet-stream");
*/
      //conn.setRequestProperty("Content-Type", "text/plain");
        //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=windows-1251");
      //conn.setRequestProperty("Cache-Control","no-cache");
      //conn.setRequestProperty("Connection", "keep-alive");
      //
      lastCode = conn.getResponseCode();
      netBusyTime=System.currentTimeMillis()-start;
      RmsManager.setDebugInfo("Response:"+lastCode);
      //System.out.println("Response:"+lastCode);
      if (lastCode == HttpConnection.HTTP_OK)
      {
        success++;
        //
        byte[] buf=null;
        is = conn.openInputStream();
        int len = (int) conn.getLength();
        RmsManager.setDebugInfo("Length:"+len);
        //System.out.println("Length:"+len);
        if (len > 0)
        {
          buf = new byte[len];
          if(Util.smartRead(is, buf, len)!=len)
            buf=null;
        }
        else
        {
          int bytes;
          int offset=0;
          byte[] data=new byte[32768];
          while (true)
          {
            bytes = is.read();
            if (bytes == -1 || offset>=8192)
              break;
            data[offset++]=(byte)bytes;
          }
          if(offset>0)
          {
            buf=new byte[offset];
            System.arraycopy(data, 0, buf, 0, offset);
          }
        }        
        //
        if(type!=GET_GOOGLE_MAP && type!=GET_GOOGLE_SAT && 
                type!=GET_VEARTH_MAP && type!=GET_VEARTH_SAT && 
                buf!=null)
          lastMessage=new String(buf);
        else
          lastMessage="";
        RmsManager.setDebugInfo("Received:"+lastMessage);
        if(!get)
        {
          switch(type)
          {
          case POST_SEND_TRACK:
            // POST query
            MIDlet1.screenManager.pushScreen(new InfoForm("Track",
                "Track was successfully sent", null));
            break;
          case POST_SEND_MESSAGE:
            // Послали сообщение
            MIDlet1.screenManager.pushScreen(new InfoForm("SpotMessage",
                lastMessage, null));
            break;
          case POST_RECEIVE_MESSAGES:
            // Получаем сообщения
            Vector v=Util.parseString(lastMessage, ",", true);
            SpotMessage.spotmessages.removeAllElements();
            for(int i=0; i<v.size()/7; i++)
            {
              SpotMessage sm=new SpotMessage();
              sm.Date=(String)v.elementAt(i*7);
              sm.KeyID=(String)v.elementAt(i*7+1);
              sm.Name=(String)v.elementAt(i*7+2);
              sm.Message=(String)v.elementAt(i*7+3);
              sm.Latitude=Double.parseDouble((String)v.elementAt(i*7+4));
              sm.Longitude=Double.parseDouble((String)v.elementAt(i*7+5));
              sm.Altitude=Double.parseDouble((String)v.elementAt(i*7+6));
              SpotMessage.spotmessages.addElement(sm);
            }
            // Показать изменения
            MIDlet1.screenManager.popScreen(1, v);
            //
            break;
          }
        }
        else
        {
            switch(type)
            {
              case GET_GOOGLE_MAP:
              case GET_GOOGLE_SAT:
              case GET_VEARTH_MAP:
              case GET_VEARTH_SAT:
                 Image image=null;
                 try
                 {
                    image=Image.createImage(buf, 0, buf.length);
                 }
                 catch(Exception ex)
                 {
                      MIDlet1.screenManager.pushScreen(new InfoForm("Error",
                          "Not enough memory to create map "+ex.getMessage(), null));
                    image=null;
                 }
                 if(image!=null)
                 {
                     if(type==GET_GOOGLE_MAP || type==GET_GOOGLE_SAT)
                     {
                         MIDlet1.rmsManager.mapRect=new Rect(MIDlet1.gMap.centerLongitude-MIDlet1.gMap.sizeLongitude/2,
                                 MIDlet1.gMap.centerLatitude-MIDlet1.gMap.sizeLatitude/2,
                                 MIDlet1.gMap.sizeLongitude,
                                 MIDlet1.gMap.sizeLatitude);
                     }
                     else
                     {                         
                         MIDlet1.rmsManager.mapRect=new Rect(MIDlet1.vEarth.centerLongitude-MIDlet1.vEarth.sizeLongitude/2,
                                 MIDlet1.vEarth.centerLatitude-MIDlet1.vEarth.sizeLatitude/2,
                                 MIDlet1.vEarth.sizeLongitude,
                                 MIDlet1.vEarth.sizeLatitude);
                     }
                     // Записываем в кеш
                     String name=query;
                     if(type==GET_VEARTH_MAP || type==GET_VEARTH_SAT)
                         name=MIDlet1.vEarth.name;
                     if(!MIDlet1.rmsManager.saveImageToRMS(name, buf))
                     {
                         // Возможно нет места, поэтому чистим кеш
                         MIDlet1.rmsManager.removeOldestImageFromRMS();
                         MIDlet1.rmsManager.removeOldestImageFromRMS();                         
                         // Пробуем записать снова
                         MIDlet1.rmsManager.saveImageToRMS(name, buf);
                         //MIDlet1.rmsManager.smartSaveRMS();
                     }
                     //
                     MIDlet1.rmsManager.imageMap=image;
                     // Восстанавливаем правильный масштаб
                     MIDlet1.rmsManager.zoomMode=this.zoomMode;
                     MIDlet1.updateMainWindow(SplashScreen.TYPE_INFO3);
                 }                 
                 break;
              default:
                 // GET query
                if(obj!=null)
                {
                  if(obj instanceof Observable)
                  {
                    Observable o=(Observable)obj;
                    o.Parse(lastMessage);
                  }
                }
            }
        }
        //
        RmsManager.clearDebugInfo();
      }
      else
      {
        failed++;
        if(!get)
        {
          switch(type)
          {
            case POST_SEND_TRACK:
              MIDlet1.screenManager.pushScreen(new InfoForm("Track",
                  "There was an error during sending track. Code: " + lastCode, null));
              break;
            case POST_SEND_MESSAGE:
              MIDlet1.screenManager.pushScreen(new InfoForm("SpotMessage",
                  "There was an error during sending message. Code: " + lastCode, null));
              break;
            case POST_RECEIVE_MESSAGES:
              MIDlet1.screenManager.replaceScreen(new InfoForm("SpotMessage",
                  "There was an error during receiving messages. Code: " + lastCode, null));
              break;
          }
        }
      }
    }
    catch (Exception ex)
    {
      if(!get && type==POST_RECEIVE_MESSAGES)
        MIDlet1.screenManager.replaceScreen(new InfoForm("Error", ex.getMessage()+"\n"+query, null));
      else
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", ex.getMessage()+"\n"+query, null));
      failed++;
    }
    finally
    {
        try
        {
          if (is != null)
          {
            is.close();
            is=null;
          }
          if (os != null)
          {
            os.close();
            os=null;
          }
          /*
          if (conn != null)
          {
            conn.close();
            conn=null;
          }*/
        }
        catch (Exception ex1)
        {
          MIDlet1.screenManager.pushScreen(new InfoForm("Error2", ex1.getMessage(), null));
        }
    }
    //
    MIDlet1.rmsManager.pleaseWait=false;
  }

  public void sendTrack()
  {
    if(MIDlet1.rmsManager.currentTrack!=null)
    {
      if(MIDlet1.rmsManager.currentTrack.size()<2)
      {
        InfoForm iform=new InfoForm("Track", "Can't send empty track", null);
        MIDlet1.screenManager.pushScreen(iform);
      }
      else
      if(MIDlet1.rmsManager.login.length()==0 || MIDlet1.rmsManager.password.length()==0)
      {
        InfoForm iform=new InfoForm("Track", "Check network settings. Login and password must be exist.", null);
        MIDlet1.screenManager.pushScreen(iform);
      }
      else
      {
        MIDlet1.netManager.url = MIDlet1.rmsManager.addressNetTrack;
        MIDlet1.netManager.query = "un=" + MIDlet1.rmsManager.login + "&pw=" +
            MIDlet1.rmsManager.password + "&name=" +
            MIDlet1.rmsManager.currentTrack.name +
            "&cds=" + MIDlet1.rmsManager.currentTrack.createPltData();
        // POST query
        MIDlet1.netManager.send(false, POST_SEND_TRACK, null);
      }
    }
  }

  public void getObservable(Observable o)
  {
    MIDlet1.netManager.url = o.path;
    MIDlet1.netManager.query = "?un=" + o.login + "&pw=" + o.password;
    // GET query
    MIDlet1.netManager.send(true, 0, o);
  }

  public void sendSpotMessage(String text)
  {
    if(text==null || text.length()==0 || SpotMessage.MyKeyID.length()==0 || SpotMessage.MyName.length()==0)
    {
      InfoForm iform=new InfoForm("SpotMessage", "Please fill all fields in the form", null);
      MIDlet1.screenManager.pushScreen(iform);
      return;
    }
    //
    if(!MIDlet1.locationManager.getValid())
    {
      InfoForm iform=new InfoForm("SpotMessage", "Can't get position, please connect and adjust GPS receiver", null);
      MIDlet1.screenManager.pushScreen(iform);
      return;
    }
    //
    MIDlet1.netManager.url = "http://www.unteh.com/spotmessage/send.html";
    MIDlet1.netManager.query = "?KeyID=" +
                                                 SpotMessage.MyKeyID + "&Name=" +
                                                 SpotMessage.MyName + "&ToID=&Message=" + text +
                                                 "&Latitude=" + MIDlet1.locationManager.getLatitude() +
                                                 "&Longitude=" + MIDlet1.locationManager.getLongitude() +
                                                 "&Altitude=" + MIDlet1.locationManager.getAltitude(true);
    // POST query
    MIDlet1.netManager.send(false, POST_SEND_MESSAGE, null);
  }

  public void receiveSpotMessage(int count)
  {
    if(!MIDlet1.locationManager.getValid())
    {
      InfoForm iform=new InfoForm("SpotMessage", "Can't get position, please connect and adjust GPS receiver", null);
      MIDlet1.screenManager.replaceScreen(iform);
      return;
    }
    //
    MIDlet1.netManager.url = "http://www.unteh.com/spotmessage/receive.html";
    MIDlet1.netManager.query = "?Latitude=" + MIDlet1.locationManager.getLatitude()+
        "&Longitude=" + MIDlet1.locationManager.getLongitude()+
        "&ToID=&Count=" + count;
    // POST query
    MIDlet1.netManager.send(false, POST_RECEIVE_MESSAGES, null);
  }
  // Google Maps
  public boolean sendGoogleMap()
  {
    if(isBusy())
        return false;
    //
    MIDlet1.rmsManager.imageMap=null;
    MIDlet1.rmsManager.pleaseWait=true;
    MIDlet1.rmsManager.mapType=RmsManager.MAP_GOOGLE_MAP;
    //
    this.url=MIDlet1.gMap.getMapURL();
    this.zoomMode=MIDlet1.rmsManager.zoomMode;
    this.query=MIDlet1.gMap.getMapQuery(zoomMode,
            MIDlet1.locationManager.getLatitude(),
            MIDlet1.locationManager.getLongitude());
    // Смотрим в кэше
    Image image=MIDlet1.rmsManager.loadImageFromRMS(query);
    if(image==null)
        this.send(true, GET_GOOGLE_MAP, null);
    else
    {
        MIDlet1.rmsManager.mapRect=new Rect(MIDlet1.gMap.centerLongitude-MIDlet1.gMap.sizeLongitude/2,
                MIDlet1.gMap.centerLatitude-MIDlet1.gMap.sizeLatitude/2,
                MIDlet1.gMap.sizeLongitude,
                MIDlet1.gMap.sizeLatitude);
         //
         MIDlet1.rmsManager.imageMap=image;
         MIDlet1.updateMainWindow(SplashScreen.TYPE_INFO3);
    }
    //
    return true;
  }

  public boolean sendGoogleSat()
  {
    if(isBusy())
        return false;
    //
    MIDlet1.rmsManager.imageMap=null;
    MIDlet1.rmsManager.pleaseWait=true;
    MIDlet1.rmsManager.mapType=RmsManager.MAP_GOOGLE_SAT;
    //
    this.url=MIDlet1.gMap.getSatURL();
    this.zoomMode=MIDlet1.rmsManager.zoomMode;
    this.query=MIDlet1.gMap.getSatQuery(zoomMode,
            MIDlet1.locationManager.getLatitude(),
            MIDlet1.locationManager.getLongitude());
    // Смотрим в кэше
    Image image=MIDlet1.rmsManager.loadImageFromRMS(query);
    if(image==null)
        this.send(true, GET_GOOGLE_SAT, null);
    else
    {
        MIDlet1.rmsManager.mapRect=new Rect(MIDlet1.gMap.centerLongitude-MIDlet1.gMap.sizeLongitude/2,
                MIDlet1.gMap.centerLatitude-MIDlet1.gMap.sizeLatitude/2,
                MIDlet1.gMap.sizeLongitude,
                MIDlet1.gMap.sizeLatitude);
         //
         MIDlet1.rmsManager.imageMap=image;
         MIDlet1.updateMainWindow(SplashScreen.TYPE_INFO3);
    }
    //
    return true;
  }
  // Virtual Earth
  public boolean sendVEarthMap()
  {
    if(isBusy())
        return false;
    //
    MIDlet1.rmsManager.imageMap=null;
    MIDlet1.rmsManager.pleaseWait=true;
    MIDlet1.rmsManager.mapType=RmsManager.MAP_VEARTH_MAP;
    //
    this.url=MIDlet1.vEarth.getMapURL();
    this.zoomMode=MIDlet1.rmsManager.zoomMode;
    this.query=MIDlet1.vEarth.getMapQuery(zoomMode,
            MIDlet1.locationManager.getLatitude(),
            MIDlet1.locationManager.getLongitude());
    // Смотрим в кэше
    Image image=MIDlet1.rmsManager.loadImageFromRMS(query);
    if(image==null)
        this.send(true, GET_VEARTH_MAP, null);
    else
    {
        MIDlet1.rmsManager.mapRect=new Rect(MIDlet1.vEarth.centerLongitude-MIDlet1.vEarth.sizeLongitude/2,
                MIDlet1.vEarth.centerLatitude-MIDlet1.vEarth.sizeLatitude/2,
                MIDlet1.vEarth.sizeLongitude,
                MIDlet1.vEarth.sizeLatitude);
         //
         MIDlet1.rmsManager.imageMap=image;
         MIDlet1.updateMainWindow(SplashScreen.TYPE_INFO3);
    }
    //
    return true;
  }

  public boolean sendVEarthSat()
  {
    if(isBusy())
        return false;
    //
    MIDlet1.rmsManager.imageMap=null;
    MIDlet1.rmsManager.pleaseWait=true;
    MIDlet1.rmsManager.mapType=RmsManager.MAP_VEARTH_SAT;
    //
    this.url=MIDlet1.vEarth.getSatURL();
    this.zoomMode=MIDlet1.rmsManager.zoomMode;
    this.query=MIDlet1.vEarth.getSatQuery(zoomMode,
            MIDlet1.locationManager.getLatitude(),
            MIDlet1.locationManager.getLongitude());
    // Смотрим в кэше
    Image image=MIDlet1.rmsManager.loadImageFromRMS(query);
    if(image==null)
        this.send(true, GET_VEARTH_SAT, null);
    else
    {
        MIDlet1.rmsManager.mapRect=new Rect(MIDlet1.vEarth.centerLongitude-MIDlet1.vEarth.sizeLongitude/2,
                MIDlet1.vEarth.centerLatitude-MIDlet1.vEarth.sizeLatitude/2,
                MIDlet1.vEarth.sizeLongitude, 
                MIDlet1.vEarth.sizeLatitude);
         //
         MIDlet1.rmsManager.imageMap=image;
         MIDlet1.updateMainWindow(SplashScreen.TYPE_INFO3);
    }
    //
    return true;
  }
}
