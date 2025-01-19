package henson.midp;

import henson.midp.Sms.SmsManager;
import javax.microedition.lcdui.Image;
import javax.microedition.io.*;
import javax.microedition.rms.*;
import javax.wireless.messaging.*;
import java.io.*;
import java.util.*;

import javax.microedition.io.file.*;
import javax.microedition.lcdui.game.*;
import javax.microedition.lcdui.*;
        
import henson.midp.Model.*;
import henson.midp.View.*;
import henson.midp.Sms.*;

public class RmsManager implements SmsHandler, Runnable
{
  static final String RMS_SETTINGS_NAME="MTS";
  public static final int RMS_SETTINGS=1;
  static final String RMS_TRACK_NAME="MTT";
  public static final int RMS_TRACK=2;
  
  static final int ZOOM_RADIUS=25;
  
  public static final byte MAP_INTERNAL=0;
  public static final byte MAP_PIXEL=1;
  public static final byte MAP_VECTOR=2;
  public static final byte MAP_GOOGLE_MAP=3;
  public static final byte MAP_GOOGLE_SAT=4;
  public static final byte MAP_VEARTH_MAP=5;
  public static final byte MAP_VEARTH_SAT=6;

  private static final int MAX_TRACK_POINTS=1000;
  
  public static final int DESTINATION_SMS_PORT=16314;

  public boolean useNetGPS=false;
  // Минимальное расстояние для передачи через GPRS, метры или футы
  public int distanceNetGPS=1000;
  // Минимальный интервал для передачи через GPRS, секунд
  public int intervalNetGPS=60;
  // Минимальное расстояние для трека, метры или футы
  public int distanceTrack=100;
  // Минимальный интервал для записи точек трека, секунд
  public int intervalTrack=10;
  // Смещение локального времени относительно UTC, минут
  public int utcOffset=0;
  //public String address="http://servername/netgps.pl?un=joe&pw=bloggs&cds=";
  public String addressNetGPS="http://www.unteh.com/cgi-bin/kmlgps.pl";
  public String addressNetTrack="http://www.unteh.com/cgi-bin/nettrack.pl";
  public String login="";
  public String password="";  
  // Точки
  public Vector points=new Vector();
  public Vector tracks=new Vector();
  public Vector observables=new Vector();
  // Выбранный waypoint
  public Point currentPoint=null;
  // Трек
  public Track currentTrack=null;  
  // Маршрут
  public Track route=null;
  //public Point routePoint=null;
  public int routePointIndex=-1;
  // Зона вокруг waypoint или точки трека со звуком
  public double pointMarginSound=200;
  // Зона вокруг waypoint с переключением на следующую точку
  public double pointMarginNext=50;
  // Current image map
  public int imageMapNum=1;
  public Image imageMap=null;
  public Rect mapRect=new Rect(-180.0, -90.0,  360.0, 180.0);
  // Current vector map
  public int vectorMapNum=0;
  public VectorMap vectorMap=null;
  // Лупа
  public Image imageZoom=null;
  public int leftZoom=0;
  public int topZoom=0;
  // Последняя конфигурация подключения
  // Строка подключения к BT или COM порту
  private String lastConfig="";
  
  // Настройки
  public boolean autoMapRotation=false;
  public boolean checkTurns=false;
  public boolean useSound=true;
  public boolean autoLoadMap=false;
  //public boolean americanUnits=false;
  public int typeUnit=Util.UNIT_METRIC;
  public boolean useTrip=true;
  // Scales
  public Vector englishScales=new Vector();
  public Vector metricScales=new Vector();  
  public Vector nauticalScales=new Vector();

  // Registration
  public String userName="";
  public String userKey="";

  // Trip Computer
  public Trip trip=new Trip();

  // BT GPS Connection
  public boolean fastConnect=true;
  public boolean useAuthenticate=false;

  // Начиная с версии 7  
  public boolean showTrackTrack=true;
  public boolean showRouteTrack=true;
  public boolean showWaypointsTrack=true;
  public boolean showWaypointsTitleTrack=true;
  public boolean showTargetTrack=true;
  public boolean showTrackMap=false;
  public boolean showRouteMap=false;
  public boolean showWaypointsMap=false;
  public boolean showWaypointsTitleMap=false;
  public boolean showTargetMap=false;
  
  // Fill rate of RMS
  //public int fillRMSLevel=0;
  
  // Screenshot
  public Bitmap screenshot=new Bitmap();
  
  // Выбранный тип карты
  public byte mapType=MAP_INTERNAL;
  
  // Масштаб карты
  public int zoomMode=100;
  // Загрузка карты
  public boolean pleaseWait=false;
  // Частота использования карт Google
  public Hashtable mapFreq=null;
  
  public SmsManager smsManager=null;
  private SmsManager smsManager2=null;
  // Rectangles to find right map
  public Vector mapRects=null;
  
  public Category[] categories=new Category[19];
  // Map directory
  public Vector mapDirectory = new Vector();

  public double defaultLatitude=0.0;
  public double defaultLongitude=0.0;
  
  // Save NMEA log
  public String nmeaLogPath = "E:/nmea.ubx";
  public boolean nmeaLogSave = false;
  public FileConnection nmeaLogConnection = null;
  public OutputStream nmeaLogOS = null;
  
  // Autload map
  public String pathAutoloadMap = "E:/dir.map";
  
  // Break of track
  public int breakTrackDelay = 300;
  
  // KML description
  private String descriptionKML="<a href=\"http://www.unteh.com/products/mobitrack/\" target=\"_blank\">MobiTrack</a> <a href=\"http://www.unteh.com/forum/\" target=\"_blank\">Forum</a>";
  
  // Path for fastsave feature
  public String fastsavePath="E:/";
  
  // Backlight level
  public int backlightLevel = 80;
  
  // Geoid correct
  // 0 - negative
  // 1 - ignore
  // 2 - positive
  public int geoidCorrection = 1;
  
  public RmsManager()
  {
    // Английские
    englishScales.addElement(new Scale("20ft", 6.096));
    englishScales.addElement(new Scale("50ft", 15.24));
    englishScales.addElement(new Scale("100ft", 30.48));
    englishScales.addElement(new Scale("200ft", 60.96));
    englishScales.addElement(new Scale("500ft", 152.4));
    englishScales.addElement(new Scale("1000ft", 304.8));
    englishScales.addElement(new Scale("1mi", 1609.0));
    englishScales.addElement(new Scale("2mi", 3218.0));
    englishScales.addElement(new Scale("5mi", 8045.0));
    englishScales.addElement(new Scale("10mi", 16090.0));
    englishScales.addElement(new Scale("20mi", 32180.0));
    englishScales.addElement(new Scale("50mi", 80450.0));
    // Метрические масштабы
    metricScales.addElement(new Scale("20m", 20.0));
    metricScales.addElement(new Scale("50m", 50.0));
    metricScales.addElement(new Scale("100m", 100.0));
    metricScales.addElement(new Scale("200m", 200.0));
    metricScales.addElement(new Scale("500m", 500.0));
    metricScales.addElement(new Scale("1km", 1000.0));
    metricScales.addElement(new Scale("2km", 2000.0));
    metricScales.addElement(new Scale("5km", 5000.0));
    metricScales.addElement(new Scale("10km", 10000.0));
    metricScales.addElement(new Scale("20km", 20000.0));
    metricScales.addElement(new Scale("50km", 50000.0));
    metricScales.addElement(new Scale("100km", 100000.0));
    // Морские масштабы
    nauticalScales.addElement(new Scale("20ft", 6.096));
    nauticalScales.addElement(new Scale("50ft", 15.24));
    nauticalScales.addElement(new Scale("100ft", 30.48));
    nauticalScales.addElement(new Scale("200ft", 60.96));
    nauticalScales.addElement(new Scale("500ft", 152.4));
    nauticalScales.addElement(new Scale("1000ft", 304.8));
    nauticalScales.addElement(new Scale("1nm", 1852.0));
    nauticalScales.addElement(new Scale("2nm", 3704.0));
    nauticalScales.addElement(new Scale("5nm", 9260.0));
    nauticalScales.addElement(new Scale("10nm", 18520.0));
    nauticalScales.addElement(new Scale("20nm", 37040.0));
    nauticalScales.addElement(new Scale("50nm", 92600.0));
    // Менеджер SMS сообщений
    if(smsManager==null)
    {
        smsManager=new SmsManager(this);
    }
    if(smsManager2==null)
    {
        smsManager2=new SmsManager(this);
        smsManager2.listen(DESTINATION_SMS_PORT);
    }
    // Частота использования карт
    if(mapFreq==null)
        mapFreq=new Hashtable();
    // Карты заложенные в JAD
    if(mapRects==null)
        mapRects=new Vector();
  }
  
  public void loadAutoloadMap()
  {
      new Thread(this).start();
  }
  
  public String getDateTimeName()
  {
    Calendar cal=Calendar.getInstance();
    long cur=cal.getTime().getTime();
    cur+=(long)MIDlet1.rmsManager.utcOffset*60000L;
    cal.setTime(new Date(cur));
    return cal.get(Calendar.YEAR)+Util.withLeadZero(cal.get(Calendar.MONTH))+Util.withLeadZero(cal.get(Calendar.DAY_OF_MONTH))+"-"+
            cal.get(Calendar.HOUR_OF_DAY)+Util.withLeadZero(cal.get(Calendar.MINUTE))+Util.withLeadZero(cal.get(Calendar.SECOND));
  }

  public boolean loadRMS(int rmstype)
  {
    if(rmstype==RMS_SETTINGS)
    {
        // Инициализация категорий
        Image imagePOI = Util.makeImage("/POI.png");
        categories[0]=new Category("Default", null, 0, "", 0, 0);
        // 1 - Dining palette-2.png   224,0
        categories[1]=new Category("Dining", imagePOI, 0, "palette-2.png", 224, 0);
        // 2 - Lodging    palette-2.png   128,128
        categories[2]=new Category("Lodging", imagePOI, 16, "palette-2.png", 128, 128);
        // 3 - ATM    palette-2.png   64,0
        categories[3]=new Category("ATM", imagePOI, 32, "palette-2.png", 64, 0);
        // 4 - Bar    palette-2.png   96,128
        categories[4]=new Category("Bar", imagePOI, 48, "palette-2.png", 96, 128);
        // 5 - Coffee palette-2.png   192,0
        categories[5]=new Category("Coffee", imagePOI, 64, "palette-2.png", 192, 0);
        // 6 - Mall   palette-2.png   160,192
        categories[6]=new Category("Mall", imagePOI, 80, "palette-2.png", 160, 192);
        // 7 - Movie  palette-2.png   192,128
        categories[7]=new Category("Movie", imagePOI, 96, "palette-2.png", 192, 128);
        // 8 - Store  palette-3.png   64,128
        categories[8]=new Category("Store", imagePOI, 112, "palette-3.png", 64, 128);
        // 9 - Pharmacy   palette-2.png   32,192
        categories[9]=new Category("Pharmacy", imagePOI, 128, "palette-2.png", 32, 192);
        // 10 - Gas   palette-2.png   160,128
        categories[10]=new Category("Gas", imagePOI, 144, "palette-2.png", 160, 128);
        // 11 - Sport palette-2.png   32,0
        categories[11]=new Category("Sport", imagePOI, 160, "palette-2.png", 32, 0);
        // 12 - Park  palette-2.png   128,192
        categories[12]=new Category("Park", imagePOI, 176, "palette-2.png", 128, 192);
        // 13 - Hospital palette-3.png    192,64
        categories[13]=new Category("Hospital", imagePOI, 192, "palette-3.png", 192, 64);
        // 14 - School    palette-2.png   64,192
        categories[14]=new Category("School", imagePOI, 208, "palette-2.png", 64, 192);
        // 15 - Churche   palette-2.png   96,192
        categories[15]=new Category("Churche", imagePOI, 224, "palette-2.png", 96, 192);
        // 16 - Airport   palette-2.png   0,0
        categories[16]=new Category("Airport", imagePOI, 240, "palette-2.png", 0, 0);
        // 17 - Service   palette-4.png   224,192  
        categories[17]=new Category("Service", imagePOI, 256, "palette-4.png", 224, 192);
        // 18 - Building  palette-3.png   160,160
        categories[18]=new Category("Building", imagePOI, 272, "palette-3.png", 160, 160);        
    }
    //
    String rsName="";
    RecordStore rs = null;
    try
    {
      switch(rmstype)
      {
          case RMS_SETTINGS: rsName=RMS_SETTINGS_NAME; break;
          case RMS_TRACK: rsName=RMS_TRACK_NAME; break;
          default: return false;
      }      
      rs = RecordStore.openRecordStore(rsName, true);
      RecordEnumeration re = rs.enumerateRecords(null, null, false);
      if (re.hasNextElement())
      {
        byte[] data = re.nextRecord();
        //rmsSize=data.length;
        //rmsAvailable=rs.getSizeAvailable();
        int sizeFull=rs.getSizeAvailable()+rs.getSize();
        //fillRMSLevel=data.length*100/sizeFull;
        //
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        try
        {
          int version = dis.readInt();
          switch(rmstype)
          {
              case RMS_SETTINGS:
                  points.removeAllElements();    
                  mapFreq.clear();
                  //
                  useNetGPS=dis.readBoolean();
                  setInterval(dis.readInt(), true);
                  setInterval(dis.readInt(), false);
                  addressNetGPS=dis.readUTF();
                  addressNetTrack=dis.readUTF();
                  login=dis.readUTF();
                  password=dis.readUTF();
                  // Image map
                  selectMap(dis.readInt());
                  // Vector map
                  selectMap(dis.readInt());
                  // Points
                  int size=dis.readInt();
                  for(int i=0; i<size; i++)
                  {
                    Point p=new Point(0.0, 0.0, 0.0);
                    p.Name=dis.readUTF();
                    p.Category=dis.readByte();
                    p.Latitude=dis.readDouble();
                    p.Longitude=dis.readDouble();
                    if(version>9)
                        p.Altitude=dis.readDouble();
                    points.addElement(p);
                  }
                  //
                  userName=dis.readUTF();
                  userKey=dis.readUTF();
                  //
                  autoMapRotation=dis.readBoolean();
                  useSound=dis.readBoolean();
                  if(version<5)
                  {
                    boolean americanUnits=dis.readBoolean();
                  }
                  useTrip=dis.readBoolean();
                  //
                  trip.load(dis);
                  // Настройки для наблюдения за другими
                  size=dis.readInt();
                  for(int i=0; i<size; i++)
                  {
                    Observable o=new Observable();
                    o.load(dis);
                    observables.addElement(o);
                  }
                  //
                  SpotMessage.MyKeyID=dis.readUTF();
                  SpotMessage.MyName=dis.readUTF();
                  //
                  fastConnect=dis.readBoolean();
                  useAuthenticate=dis.readBoolean();
                  //
                  lastConfig=dis.readUTF();
                  //
                  if(version>1)
                  {
                      //mapType=dis.readByte();
                      zoomMode=dis.readInt();
                  }
                  //
                  if(version>2)
                  {
                      // Загружаем кеш
                      int count=dis.readInt();
                      for(int j=0; j<count; j++)
                      {
                          String path=dis.readUTF();
                          Integer num=new Integer(dis.readInt());
                          mapFreq.put(path, num);
                      }
                  }
                  //
                  if(version>3)
                      utcOffset=dis.readInt();
                  //
                  if(version>4)
                      typeUnit=dis.readInt();
                  //
                  if(version>5)
                      autoLoadMap=dis.readBoolean();
                  //
                  if(version>6)
                  {
                    showTrackTrack=dis.readBoolean();
                    showWaypointsTrack=dis.readBoolean();
                    showWaypointsTitleTrack=dis.readBoolean();
                    showTargetTrack=dis.readBoolean();
                    //
                    showTrackMap=dis.readBoolean();
                    showWaypointsMap=dis.readBoolean();
                    showWaypointsTitleMap=dis.readBoolean();
                    showTargetMap=dis.readBoolean();
                  }
                  //
                  if(version>7)
                  {
                      showRouteTrack=dis.readBoolean();
                      showRouteMap=dis.readBoolean();
                      pointMarginSound=dis.readDouble();
                      pointMarginNext=dis.readDouble();
                  }
                  //
                  if(version>8)
                  {
                      checkTurns=dis.readBoolean();
                  }
                  //
                  if(version>9)
                  {
                      // Восстанавливаем координаты по умолчанию
                      defaultLatitude=dis.readDouble();
                      defaultLongitude=dis.readDouble();
                  }
                  //
                  if(version>10)
                  {
                      nmeaLogPath=dis.readUTF();
                      nmeaLogSave=dis.readBoolean();
                      breakTrackDelay=dis.readInt();
                      pathAutoloadMap=dis.readUTF();
                  }
                  //
                  if(version>11)
                  {
                      distanceNetGPS=dis.readInt();
                      distanceTrack=dis.readInt();
                  }
                  //
                  if(version>12)
                  {
                      fastsavePath=dis.readUTF();
                  }
                  //
                  if(version>13)
                  {
                      backlightLevel=dis.readInt();
                  }
                  //
                  if(version>14)
                  {
                      geoidCorrection=dis.readInt();
                  }
                  //
                  loadAutoloadMap();
                  break;
              case RMS_TRACK:
                  tracks.removeAllElements();
                  // Tracks
                  size=dis.readInt();
                  for(int i=0; i<size; i++)
                  {
                    Track t=new Track();
                    t.load(dis);
                    tracks.addElement(t);
                  }
                  break;
          }
          //
          dis.close();
          bais.close();
        }
        catch (IOException ex)
        {
            MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't load data\n"+ex.getMessage(), null));
        }
      }
      else
        // Default map
        selectMap(1);
      //
      rs.closeRecordStore();
    }
    catch (RecordStoreException ex)
    {
      try
      {        
          RecordStore.deleteRecordStore(rsName);
      }
      catch (RecordStoreException e)
      {
      }
      return false;
    }
    //
    if(tracks.size()==0)
    {
      Track t = new Track();
      tracks.addElement(t);
    }
    // Трек по умолчанию
    currentTrack=(Track)tracks.elementAt(0);
    //
    return true;
  }

  private synchronized boolean saveRMS(int rmstype)
  {
      int sizeFull=0;
      String rsName="";
      RecordStore rs = null;
      //
      try {
          switch(rmstype) 
          {
              case RMS_SETTINGS: rsName=RMS_SETTINGS_NAME; break;
              case RMS_TRACK: rsName=RMS_TRACK_NAME; break;
              default: return false;
          }
          rs=RecordStore.openRecordStore(rsName, true);
          sizeFull=rs.getSizeAvailable()+rs.getSize();
          rs.closeRecordStore();
      } catch (RecordStoreException ex) {
      }
      //
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream dos = new DataOutputStream(baos);
      try {
          switch(rmstype) {
              case RMS_SETTINGS:
                  // Version
                  dos.writeInt(15);
                  dos.writeBoolean(useNetGPS);
                  dos.writeInt(intervalNetGPS);
                  dos.writeInt(intervalTrack);
                  dos.writeUTF(addressNetGPS);
                  dos.writeUTF(addressNetTrack);
                  dos.writeUTF(login);
                  dos.writeUTF(password);
                  dos.writeInt(imageMapNum);
                  dos.writeInt(vectorMapNum);
                  // Points
                  dos.writeInt(points.size());
                  for(int i=0; i<points.size(); i++) 
                  {
                      Point p=(Point)points.elementAt(i);
                      dos.writeUTF(p.Name);
                      dos.writeByte(p.Category);
                      dos.writeDouble(p.Latitude);
                      dos.writeDouble(p.Longitude);
                      dos.writeDouble(p.Altitude);
                  }
                  //
                  dos.writeUTF(userName);
                  dos.writeUTF(userKey);
                  //
                  dos.writeBoolean(autoMapRotation);
                  dos.writeBoolean(useSound);
                  // Убрано начиная с версии 5
                  //dos.writeBoolean(americanUnits);
                  dos.writeBoolean(useTrip);
                  //
                  trip.save(dos);
                  //
                  dos.writeInt(observables.size());
                  for(int i=0; i<observables.size(); i++) {
                      Observable o=(Observable)observables.elementAt(i);
                      o.save(dos);
                  }
                  //
                  dos.writeUTF(SpotMessage.MyKeyID);
                  dos.writeUTF(SpotMessage.MyName);
                  //
                  dos.writeBoolean(fastConnect);
                  dos.writeBoolean(useAuthenticate);
                  //
                  dos.writeUTF(lastConfig);
                  //
                  //dos.writeByte(mapType);
                  dos.writeInt(zoomMode);
                  // Сохраняем кеш
                  dos.writeInt(mapFreq.size());
                  Enumeration enumX=mapFreq.keys();
                  while(enumX.hasMoreElements()) {
                      String path=(String)enumX.nextElement();
                      dos.writeUTF(path);
                      int count=((Integer)mapFreq.get(path)).intValue();
                      dos.writeInt(count);
                  }
                  // Смещение времени
                  dos.writeInt(utcOffset);
                  // Начиная с версии 5
                  dos.writeInt(typeUnit);
                  // Начиная с версии 6
                  dos.writeBoolean(autoLoadMap);
                  // Начиная с версии 7
                  dos.writeBoolean(showTrackTrack);
                  dos.writeBoolean(showWaypointsTrack);
                  dos.writeBoolean(showWaypointsTitleTrack);
                  dos.writeBoolean(showTargetTrack);
                  //
                  dos.writeBoolean(showTrackMap);
                  dos.writeBoolean(showWaypointsMap);
                  dos.writeBoolean(showWaypointsTitleMap);
                  dos.writeBoolean(showTargetMap);
                  //
                  dos.writeBoolean(showRouteTrack);
                  dos.writeBoolean(showRouteMap);
                  dos.writeDouble(pointMarginSound);
                  dos.writeDouble(pointMarginNext);
                  //
                  dos.writeBoolean(checkTurns);
                  // Сохраняем координаты по умолчанию
                  dos.writeDouble(defaultLatitude);
                  dos.writeDouble(defaultLongitude);
                  //
                  dos.writeUTF(nmeaLogPath);
                  dos.writeBoolean(nmeaLogSave);
                  dos.writeInt(breakTrackDelay);
                  dos.writeUTF(pathAutoloadMap);
                  //
                  dos.writeInt(distanceNetGPS);
                  dos.writeInt(distanceTrack);
                  // 13
                  dos.writeUTF(fastsavePath);
                  // 14
                  dos.writeInt(backlightLevel);
                  // 15
                  dos.writeInt(geoidCorrection);
                  //
                  break;
              case RMS_TRACK:
                  // Version
                  dos.writeInt(1);
                  dos.writeInt(tracks.size());
                  for(int i=0; i<tracks.size(); i++) {
                      Track t=(Track)tracks.elementAt(i);
                      t.save(dos);
                  }
                  break;
          }
          //
          dos.close();
          baos.close();
      } catch (IOException ex) {
          MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't save data 2\n"+ex.getMessage(), null));
      }
      //
      byte[] data=baos.toByteArray();
      //
      //if(rmstype==RMS_TRACK)
      //    fillRMSLevel=data.length*100/sizeFull;
      // Пишем БЕЗ проверки
      try 
      {
          try
          {
            // Normal way
            rs = RecordStore.openRecordStore(rsName, true);
            if(rs.getNumRecords()==0)
              rs.addRecord(data, 0, data.length);
            else 
              rs.setRecord(1, data, 0, data.length);
            rs.closeRecordStore();
            rs=null;
          }
          catch (RecordStoreException ex) 
          {
             // if something goes wrong
             if(rs!=null)
             {
                 rs.closeRecordStore();
                 //
                 RecordStore.deleteRecordStore(rsName);
                 rs = RecordStore.openRecordStore(rsName, true);
                 rs.addRecord(data, 0, data.length);
                 rs.closeRecordStore();
                 rs=null;
             }
          }          
      } 
      catch (RecordStoreException ex) 
      {
          MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't save data\n"+ex.getMessage(), null));
          return false;
      } 
      finally 
      {
          if(rs!=null)
          {
              try 
              {
                rs.closeRecordStore();
              } catch(RecordStoreException ex) 
              {
              }
          }
      }
      //
      return true;
  }

  public void setInterval(int value, boolean NetGPS)
  {
      if(value<2)
        value=2;
      if(value>10800)
        value=10800;
      //
      if(NetGPS)
          intervalNetGPS=value;
      else
          intervalTrack=value;
  }
  
  public byte[] loadFile(String path)
  {      
    byte[] imageData = new byte[0];
    try
    {
        FileConnection fileConn = (FileConnection)Connector.open(path, Connector.READ);
        long overallSize = fileConn.fileSize();
        // load the image data in memory
        // Read data in CHUNK_SIZE chunks
        InputStream fis = fileConn.openInputStream();        
        byte[] data = new byte[512];
        int length = 0;        
        while (length < overallSize)
        {            
            int readAmount = fis.read(data, 0, 512);
            byte[] newImageData = new byte[imageData.length + 512];
            System.arraycopy(imageData, 0, newImageData, 0, length);
            System.arraycopy(data, 0, newImageData, length, readAmount);
            imageData = newImageData;
            length += readAmount;
        }
        fis.close();
        fileConn.close();
    }
    catch (IOException e)
    {    
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", e.getMessage(), null));
    }
    catch (Exception e)
    {
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", e.getMessage(), null));
    }
    return imageData;
  }

  public FileConnection openFile(String path)
  {      
    FileConnection fileConn = null;
    try
    {        
        fileConn = (FileConnection)Connector.open(path);
        if(!fileConn.exists())
            fileConn.create();
    }
    catch (IOException e)
    {
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", path+"\n"+e.getMessage(), null));
    }
    catch (Exception e)
    {
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", "openFile\n"+path+"\n"+e.getMessage(), null));
    }
    //
    return fileConn;
  }

  public boolean saveFile(String path, byte[] data, boolean messageReplace)
  {      
    boolean result=false;
    try
    {        
        FileConnection fileConn = (FileConnection)Connector.open(path);
        if(!fileConn.exists())
            fileConn.create();
        // load the image data in memory
        // Read data in CHUNK_SIZE chunks
        OutputStream fos = fileConn.openOutputStream();
        fos.write(data);
        fos.close();
        fileConn.close();
        //
        if(messageReplace)
            MIDlet1.screenManager.replaceScreen(new InfoForm("Save", path+" was successfully saved", null));
        else
            MIDlet1.screenManager.pushScreen(new InfoForm("Save", path+" was successfully saved", null));
        //
        result=true;
    }
    catch (IOException e)
    {
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", path+"\n"+e.getMessage(), null));
    }
    catch (Exception e)
    {
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", path+"\n"+e.getMessage(), null));
    }
    //
    return result;
  }

  private boolean loadWaypoints(String path)
  {
      byte[] data=loadFile(path);
      if(data==null)
          return false;
      //
      boolean result=false;
      String body=new String(data);
      int count=0;      
      //
      if(path.endsWith(".wpt"))
      {
          Vector v=Util.parseString(body.replace('\n','|'), "|", true);
          if(v!=null)
          {
              for(int i=0; i<v.size(); i++)
              {
                  String s=(String)v.elementAt(i);
                  //MIDlet1.screenManager.pushScreen(new InfoForm("Waypoint", s, null));
                  Vector k=Util.parseString(s, ",", true);
                  if(k!=null && k.size()>10)
                  {
                      try
                      {
                          Point p=new Point(0.0, 0.0, 0.0);
                          p.Name=new String((String)k.elementAt(1));
                          p.Latitude=Double.parseDouble((String)k.elementAt(2));
                          p.Longitude=Double.parseDouble((String)k.elementAt(3));
                          this.points.addElement(p);
                          count++;
                          result=true;
                      }
                      catch(Exception e)
                      {
                      }
                  }
              }              
          }
      }
      else
      if(path.endsWith(".kml"))
      {
          int posEnd;
          int pos2;
          int pos=0;
          while(true)
          {
            // Поиск <Placemark>
            pos=body.indexOf("<Placemark",  pos);
            if(pos==-1)
                break;
            // Поиск >
            pos=body.indexOf(">",  pos);
            if(pos==-1)
                break;
            // Поиск </Placemark>
            posEnd=body.indexOf("</Placemark>",  pos);
            if(posEnd==-1)
                break;
            // Поиск <name>
            pos=body.indexOf("<name>",  pos);
            if(pos==-1)
                break;
            if(pos>posEnd)
            {
                pos=posEnd+12;
                continue;
            }
            // Поиск </name>
            pos2=body.indexOf("</name>",  pos);
            if(pos2==-1)
                break;
            // Запоминаем название
            String name=body.substring(pos+6, pos2);
            pos=pos2;
            // Поиск <Point
            pos=body.indexOf("<Point",  pos);
            if(pos==-1)
                break;
            // Поиск >
            pos=body.indexOf(">",  pos);
            if(pos==-1)
                break;
            int categoryIndex=0;
            // Определение категории (если есть)
            {
                int pos3=body.indexOf("<href>",  pos2);
                if(pos3!=-1 && pos3<pos)
                {
                    try
                    {
                        int pos4=body.indexOf("</href>",  pos3);
                        String palette=body.substring(pos3+6, pos4);
                        int x=0;
                        pos3=body.indexOf("<x>",  pos4);
                        if(pos3!=-1)
                        {
                            pos4=body.indexOf("</x>",  pos3);
                            x=Integer.parseInt(body.substring(pos3+3, pos4));
                        }
                        int y=0;
                        pos3=body.indexOf("<y>",  pos4);
                        if(pos3!=-1)
                        {
                            pos4=body.indexOf("</y>",  pos3);
                            y=Integer.parseInt(body.substring(pos3+3, pos4));
                        }
                        // Поиск подходящей категории
                        for(int i=0; i<categories.length; i++)
                        {
                            String palname="root://icons/"+categories[i].palette;
                            if(palname.equals(palette) && 
                                    categories[i].x==x &&
                                    categories[i].y==y)
                            {
                                categoryIndex=i;
                                break;
                            }
                        }
                    }
                    catch(NumberFormatException ex)
                    {
                        categoryIndex=0;
                    }
                }
            }
            // Возврат к обработке точки
            if(pos>posEnd)
            {
                pos=posEnd+12;
                continue;
            }                
            // Поиск <coordinates>
            pos=body.indexOf("<coordinates>",  pos);
            if(pos==-1)
                break;
            // Поиск </coordinates>
            pos2=body.indexOf("</coordinates>",  pos);
            if(pos==-1)
                break;
            // Запоминаем координаты
            String coordinates=body.substring(pos+13, pos2);
            Vector v=Util.parseString(coordinates, ",",  true);
            if(v!=null && v.size()==3)
            {
                try
                {
                    Point p=new Point(0.0, 0.0, 0.0);
                    p.Name=new String(name);
                    p.Longitude=Double.parseDouble((String)v.elementAt(0));
                    p.Latitude=Double.parseDouble((String)v.elementAt(1));
                    p.Altitude=Double.parseDouble((String)v.elementAt(2))*Util.M2FT;
                    p.Category=(byte)categoryIndex;
                    this.points.addElement(p);
                    count++;
                    result=true;
                }
                catch(Exception ex)
                {
                    
                }
            }
            pos=pos2;
          }
      }
      //
      MIDlet1.screenManager.pushScreen(new InfoForm("Waypoints", "Found "+count+" waypoint(s)", null));
      //
      return result;
  }
  /*
  private boolean loadVectorMap(String path)
  {
      byte[] data=loadFile(path);
      if(data==null)
          return false;
      //      
      ByteArrayInputStream bais=new ByteArrayInputStream(data);
      vectorMap=new VectorMap();
      boolean result=vectorMap.Open(bais);
      if(!result)
        vectorMap = null;
      return result;
  }*/
  
  private boolean loadRoute(String path)
  {
      byte[] data=loadFile(path);
      if(data==null)
          return false;
      //      
      boolean result=false;
      String body=new String(data);
      //
      this.route=null;
      //
      if(path.endsWith(".rte"))
      {      
          Vector v=Util.parseString(body.replace('\n','|'), "|", true);
          if(v!=null)
          {
              for(int i=0; i<v.size(); i++)
              {
                  String s=(String)v.elementAt(i);
                  Vector k=Util.parseString(s, ",", true);
                  if(k!=null && k.size()>7)
                  {
                      s=(String)k.elementAt(0);
                      if(s.startsWith("W"))
                      {
                          try
                          {
                              Point p=new Point(0.0, 0.0, 0.0);
                              p.Name=new String((String)k.elementAt(4));
                              p.Latitude=Double.parseDouble((String)k.elementAt(5));
                              p.Longitude=Double.parseDouble((String)k.elementAt(6));
                              if(this.route==null)
                                  this.route=new Track();
                              this.route.addElement(p);
                              result=true;
                          }
                          catch(Exception e)
                          {
                          }
                      }
                  }
              }              
          }
      }
      else
      if(path.endsWith(".kmr"))
      {
          int posEnd;
          int pos2;
          int pos=0;
          while(true)
          {
            // Поиск <Placemark>
            pos=body.indexOf("<Placemark",  pos);
            if(pos==-1)
                break;
            // Поиск >
            pos=body.indexOf(">",  pos);
            if(pos==-1)
                break;
            // Поиск </Placemark>
            posEnd=body.indexOf("</Placemark>",  pos);
            if(posEnd==-1)
                break;
            // Поиск <name>
            pos=body.indexOf("<name>",  pos);
            if(pos==-1)
                break;
            if(pos>posEnd)
            {
                pos=posEnd+12;
                continue;
            }
            // Поиск </name>
            pos2=body.indexOf("</name>",  pos);
            if(pos2==-1)
                break;
            // Запоминаем название
            String name=body.substring(pos+6, pos2);
            pos=pos2;
            // Поиск <Point
            pos=body.indexOf("<Point",  pos);
            if(pos==-1)
                break;
            // Поиск >
            pos=body.indexOf(">",  pos);
            if(pos==-1)
                break;
            //
            if(pos>posEnd)
            {
                pos=posEnd+12;
                continue;
            }
            // Поиск <coordinates>
            pos=body.indexOf("<coordinates>",  pos);
            if(pos==-1)
                break;
            // Поиск </coordinates>
            pos2=body.indexOf("</coordinates>",  pos);
            if(pos==-1)
                break;
            // Запоминаем координаты
            String coordinates=body.substring(pos+13, pos2);
            Vector v=Util.parseString(coordinates, ",",  true);
            if(v!=null && v.size()==3)
            {
                try
                {
                    Point p=new Point(0.0, 0.0, 0.0);
                    p.Name=new String(name);
                    p.Longitude=Double.parseDouble((String)v.elementAt(0));
                    p.Latitude=Double.parseDouble((String)v.elementAt(1));
                    p.Altitude=(Double.parseDouble((String)v.elementAt(2))*Util.M2FT);
                    if(this.route==null)
                        this.route=new Track();
                    this.route.addElement(p);
                    result=true;
                }
                catch(Exception ex)
                {
                }
            }
            pos=pos2;
          }
      }
      //
      return result;
  }
  
  public Rect calcMapRect(int width, int height,
          int x1, int y1, double lon1, double lat1,
          int x2, int y2, double lon2, double lat2)
  {
      Rect rect=new Rect();
      double dx=(lon2-lon1)/(x2-x1);
      double dy=(lat2-lat1)/(y2-y1);
      //
      rect.startLongitude=lon1-x1*dx;
      rect.widthLongitude=width*dx;
      if(dy<0)
        rect.heightLatitude=-height*dy;
      else
        rect.heightLatitude=height*dy;
      rect.startLatitude=lat1-y1*dy-rect.heightLatitude;
      //
      return rect;
  }

  private boolean loadPixelMap(String path)
  {
      byte[] data=loadFile(path);
      if(data==null)
          return false;
      //
      boolean result=false;
      String body=new String(data);
      Vector v=Util.parseString(body.replace('\n',','), ",", true);
      if(v!=null && v.size()>5)
      {
          imageMap = null;
          String s=(String)v.elementAt(0);
          if(s.startsWith("OziExplorer"))
          {
              // map
              // 0 - ждем имя файла
              String name="";
              // 1 - ждем левую верхнюю точку
              int x1=0;
              int y1=0;
              // 2 - ждем правую нижнюю точку
              int x2=0;
              int y2=0;
              // 3 - ждем широту, долготу левая верхняя
              double lat1=0.0;
              double lon1=0.0;
              // 4 - ждем широту, долготу правую нижнюю
              double lat2=0.0;
              double lon2=0.0;
              // 5 - конец
              int state=0;
              // map в формает OziExplorer
              for(int i=1; i<v.size(); i++)
              {
                  s=((String)v.elementAt(i)).toLowerCase(); 
                  if(state==0)
                  {
                      if(s.indexOf(".png")!=-1 || s.indexOf(".gif")!=-1 || s.indexOf(".jpg")!=-1)
                      {                          
                          int pos=s.lastIndexOf('\\');
                          if(pos!=-1)
                          {
                              name=s.substring(pos+1);
                              state=1;
                          }
                      }
                  }
                  else
                  if(state==1)
                  {
                      String sprev2=(String)v.elementAt(i-2);
                      String sprev=(String)v.elementAt(i-1);
                      if(sprev2.equals("MMPXY") && sprev.equals("1"))
                      {
                          x1=Integer.parseInt(s);
                          y1=Integer.parseInt((String)v.elementAt(i+1));
                          state=2;
                      }
                  }
                  else
                  if(state==2)
                  {
                      String sprev2=(String)v.elementAt(i-2);
                      String sprev=(String)v.elementAt(i-1);
                      if(sprev2.equals("MMPXY") && sprev.equals("3"))
                      {
                          x2=Integer.parseInt(s);
                          y2=Integer.parseInt((String)v.elementAt(i+1));
                          state=3;
                      }
                  }
                  else
                  if(state==3)
                  {
                      String sprev2=(String)v.elementAt(i-2);
                      String sprev=(String)v.elementAt(i-1);
                      if(sprev2.equals("MMPLL") && sprev.equals("1"))
                      {
                          lon1=Double.parseDouble(s);
                          lat1=Double.parseDouble((String)v.elementAt(i+1));
                          state=4;
                      }
                  }
                  else
                  if(state==4)
                  {
                      String sprev2=(String)v.elementAt(i-2);
                      String sprev=(String)v.elementAt(i-1);
                      if(sprev2.equals("MMPLL") && sprev.equals("3"))
                      {
                          lon2=Double.parseDouble(s);
                          lat2=Double.parseDouble((String)v.elementAt(i+1));
                          state=5;
                          break;
                      }
                  }
              }
              //
              switch(state)
              {
                  case 0:
                      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Image name not found", null));
                      break;
                  case 1:
                      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Left upper corner of image not found", null));
                      break;
                  case 2:
                      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Right lower corner of image not found", null));
                      break;
                  case 3:
                      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Left upper coordinates not found", null));
                      break;
                  case 4:
                      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Right lower coordinates not found", null));
                      break;
                  default:
            try
            {                
                int pos=path.lastIndexOf(ListForm.FILE_SEPARATOR.charAt(0));
                //data=loadFile(path.substring(0, pos+1)+name);
                //imageMap = Image.createImage(data, 0, data.length);
                FileConnection fileConn = (FileConnection)Connector.open(path.substring(0, pos+1)+name, Connector.READ);
                imageMap = Image.createImage(fileConn.openInputStream());
                if(imageMap!=null)
                {                   
                    mapRect=new Rect(lon1, lat2, lon2 - lon1, lat1 - lat2);
                    result=true;
                }
                fileConn.close();
            }
            catch(Exception ex)
            {
                imageMap = null;
                MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't load map ["+name+"]\n"+ex.toString(), null));
            }                      
              }
          }
          else
          {
            // gmi
            String firstString=(String)v.elementAt(0);
            if(firstString.startsWith("Map Calibration data file v2.0"))
            {
                // GMI 2.0
                String name=(String)v.elementAt(1);
                int pos=name.lastIndexOf('\\');
                if(pos!=-1)
                   name=new String(name.substring(pos+1));
                try
                {                
                    //int width=Integer.parseInt((String)v.elementAt(2));
                    //int height=Integer.parseInt((String)v.elementAt(3));
                    // точка 1
                    Vector v1=Util.parseString((String)v.elementAt(4), ";", true);
                    int x1=Integer.parseInt((String)v1.elementAt(0));
                    int y1=Integer.parseInt((String)v1.elementAt(1));
                    double lon1=Double.parseDouble((String)v1.elementAt(2));
                    double lat1=Double.parseDouble((String)v1.elementAt(3));                    
                    // точка 2
                    Vector v2=Util.parseString((String)v.elementAt(5), ";", true);
                    int x2=Integer.parseInt((String)v2.elementAt(0));
                    int y2=Integer.parseInt((String)v2.elementAt(1));
                    double lon2=Double.parseDouble((String)v2.elementAt(2));
                    double lat2=Double.parseDouble((String)v2.elementAt(3));
                    //
                    pos=path.lastIndexOf(ListForm.FILE_SEPARATOR.charAt(0));
                    FileConnection fileConn = (FileConnection)Connector.open(path.substring(0, pos+1)+name, Connector.READ);
                    imageMap = Image.createImage(fileConn.openInputStream());
                    if(imageMap!=null)
                    {
                        mapRect=calcMapRect(imageMap.getWidth(), imageMap.getHeight(), 
                                x1, y1, lon1, lat1, 
                                x2, y2, lon2, lat2);
                        result=true;
                    }
                    fileConn.close();
                }
                catch(Exception ex)
                {
                    imageMap = null;
                    MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't load map ["+name+"]\n"+ex.getMessage(), null));
                }
            }
            else
            if(v.size()>10)
            {
                String name=(String)v.elementAt(10);
                int pos=name.lastIndexOf('\\');
                if(pos!=-1)
                   name=new String(name.substring(pos+1));
                try
                {                
                    int x1=Integer.parseInt((String)v.elementAt(0));
                    int y1=Integer.parseInt((String)v.elementAt(1));
                    int x2=Integer.parseInt((String)v.elementAt(2));
                    int y2=Integer.parseInt((String)v.elementAt(3));
                    double lon1=Double.parseDouble((String)v.elementAt(4));
                    double lat1=Double.parseDouble((String)v.elementAt(5));
                    double lon2=Double.parseDouble((String)v.elementAt(6));
                    double lat2=Double.parseDouble((String)v.elementAt(7));
                    int width=Integer.parseInt((String)v.elementAt(8));
                    int height=Integer.parseInt((String)v.elementAt(9));                
                    //
                    pos=path.lastIndexOf(ListForm.FILE_SEPARATOR.charAt(0));
                    FileConnection fileConn = (FileConnection)Connector.open(path.substring(0, pos+1)+name, Connector.READ);
                    imageMap = Image.createImage(fileConn.openInputStream());
                    if(imageMap!=null)
                    {
                        mapRect=calcMapRect(imageMap.getWidth(), imageMap.getHeight(), 
                                x1, y1, lon1, lat1, 
                                x2, y2, lon2, lat2);
                        result=true;
                    }
                    fileConn.close();
                }
                catch(Exception ex)
                {
                    imageMap = null;
                    MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't load map ["+name+"]\n"+ex.getMessage(), null));
                }
            }
            else
                MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Unknown file format ["+path+"]", null));
          }
      }
       return result;
  }

  public void selectMap(int index)
  {
    String mapName;
    String mapPath;
    String stage="INIT";
    //
    if(mapDirectory.size()>0)
    {
        // Load external image
        if(index<1 || index>mapDirectory.size())
            return;
        mapName = (String)mapDirectory.elementAt(index-1);
        mapPath = "";
    }
    else
    {
        // Load image description from JAD
        mapName = MIDlet1.getProperty("map"+index);
        mapPath = MIDlet1.getProperty("path"+index);
    }
    if(mapName==null || mapName.length()==0)
      return;
    //
    mapType=MAP_INTERNAL;
    //
    boolean isGMI=false;
    Vector v=Util.parseString(mapName, ";", true);
    if(v.size()==12)
    {
        mapName=(String)v.elementAt(0);
        mapPath=(String)v.elementAt(1);
        if(!mapPath.startsWith("file:///"))
        {
            int pos=pathAutoloadMap.lastIndexOf('/');
            mapPath="file:///"+pathAutoloadMap.substring(0, pos+1)+mapPath;
        }
        isGMI=true;
    }
    //
    try
    {
      if(mapPath.endsWith(".mtp"))
      {
        InputStream is = getClass().getResourceAsStream(mapPath);
        //
        vectorMapNum = index;
        vectorMap=new VectorMap();
        if(!vectorMap.Open(is))
        {
          vectorMapNum = 0;
          vectorMap = null;
        }
      }
      else
      {
        imageMapNum = index;
        // Освобждаем память перед загрузкой другой карты
        imageMap = null;
        //
        if(mapPath.startsWith("file:///"))
        {
            stage="LOADRMS";
            // Пробуем загрузить из RMS
            imageMap=loadImageFromRMS(mapPath);            
            if(imageMap==null)
            {
                stage="LOADFILE";
                byte[] data=loadFile(mapPath);
                if(data!=null)
                {
                    stage="SAVERMS";
                    if(!saveImageToRMS(mapPath, data))
                    {
                        // Free some place in RMS
                        removeOldestImageFromRMS();
                        // One more try to save
                        saveImageToRMS(mapPath, data);
                    }
                    stage="CREATEIMAGE";
                    imageMap=Image.createImage(data, 0, data.length);                    
                }
            }
        }
        else
        {
            // Загружаем из JAR
            imageMap = Util.makeImage(mapPath);
        }
        //
        if(imageMap!=null)
        {
            stage="CALIBRATE";
            if(isGMI)
            {
                int x1=Integer.parseInt((String)v.elementAt(4));
                int y1=Integer.parseInt((String)v.elementAt(5));
                double lon1=Double.parseDouble((String)v.elementAt(6));
                double lat1=Double.parseDouble((String)v.elementAt(7));
                //
                int x2=Integer.parseInt((String)v.elementAt(8));
                int y2=Integer.parseInt((String)v.elementAt(9));
                double lon2=Double.parseDouble((String)v.elementAt(10));
                double lat2=Double.parseDouble((String)v.elementAt(11));
                //
                mapRect=this.calcMapRect(imageMap.getWidth(), imageMap.getHeight(),
                        x1, y1, lon1, lat1,
                        x2, y2, lon2, lat2);                
            }
            else
            {
                mapRect=new Rect(Double.parseDouble(MIDlet1.getProperty("minlon" + index)), 
                        Double.parseDouble(MIDlet1.getProperty("minlat" + index)),
                        Double.parseDouble(MIDlet1.getProperty("maxlon" + index)) - Double.parseDouble(MIDlet1.getProperty("minlon" + index)), 
                        Double.parseDouble(MIDlet1.getProperty("maxlat" + index)) - Double.parseDouble(MIDlet1.getProperty("minlat" + index)));
            }
        }
        else
            imageMapNum=0;
      }
    }
    catch (Exception ex)
    {
      if(mapPath.endsWith(".mtp"))
      {
        vectorMapNum = 0;
        vectorMap = null;
      }
      else
      {
        imageMapNum = 0;
        imageMap = null;
      }
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't load map ["+index+"] "+mapPath+"\n"+ex.getMessage()+"\n"+stage, null));
    }
  }

  public boolean createKMLData(OutputStream os)
  {
      try
      {
        // Добавляем стиль точек трека
        os.write("<Style id=\"trackPoints\">\r\n".getBytes());
        os.write("<IconStyle>\r\n".getBytes());
        os.write("<scale>0.25</scale>\r\n".getBytes());
        os.write("<Icon>\r\n".getBytes());
        os.write("<href>http://www.unteh.com/products/mobitrack/images/GETP.png</href>\r\n".getBytes());
        os.write("</Icon></IconStyle>\r\n".getBytes());
        os.write("</Style>\r\n".getBytes());
        // Добавляем стиль точек маршрута
        os.write("<Style id=\"routePoints\">\r\n".getBytes());
        os.write("<IconStyle>\r\n".getBytes());
        os.write("<Icon>\r\n".getBytes());
        os.write("<href>http://www.unteh.com/products/mobitrack/images/GER.png</href>\r\n".getBytes());
        os.write("</Icon></IconStyle>\r\n".getBytes());
        os.write("</Style>\r\n".getBytes());
        // Добавляем трек
        if(currentTrack!=null && currentTrack.size()>0)
        {
            os.write("<Folder>\r\n".getBytes());
            Point p1=(Point)currentTrack.elementAt(0);
            Point p2=(Point)currentTrack.elementAt(currentTrack.size()-1);
            os.write(("<description><![CDATA[Points: "+currentTrack.size()+" Length: "+currentTrack.getLength()+" "+MIDlet1.rmsManager.getDistanceName(true)).getBytes());
            if(p1.getDateTime(Point.TYPE_DATE).equals(p2.getDateTime(Point.TYPE_DATE)))
                os.write(("<br/>"+p1.getDateTime(Point.TYPE_DATE)+" "+p1.getDateTime(Point.TYPE_TIME)+" - "+p2.getDateTime(Point.TYPE_TIME)).getBytes());
            else
                os.write(("<br/>"+p1.getDateTime(Point.TYPE_DATETIME)+" - "+p2.getDateTime(Point.TYPE_DATETIME)).getBytes());
            os.write(("<br/>"+descriptionKML+"]]></description>\r\n").getBytes());
            os.write("<name>Track</name>\r\n".getBytes());
            if(!currentTrack.createKMLData(false, os))
                return false;
            os.write("</Folder>\r\n".getBytes());
        }
        // Добавляем путевые точки
        if(points!=null && points.size()>0)
        {
            os.write("<Folder>\r\n".getBytes());
            os.write(("<description><![CDATA[Quantity: "+points.size()+"<br/>"+descriptionKML+"]]></description>\r\n").getBytes());
            os.write("<name>Waypoints</name>\r\n".getBytes());
            for(int i=0; i<points.size(); i++)
            {
              Point p = (Point)points.elementAt(i);
              //
              double altitude=Math.ceil(p.Altitude*10.0)/10.0;
              if(typeUnit==Util.UNIT_METRIC)
                    altitude=Math.ceil(p.Altitude*10.0/Util.M2FT)/10.0;
              //
              os.write("<Placemark>\r\n".getBytes());
              if(p.Category>0)
                  os.write(categories[p.Category].getKML().getBytes());
              os.write(("<description>Altitude: "+altitude+" "+getDistanceName(false)+"</description>\r\n").getBytes());
              os.write(("<name>"+p.Name+"</name>").getBytes());
              os.write("<visibility>1</visibility>".getBytes());
              os.write("<Point>\r\n".getBytes());
              os.write(("<coordinates>"+p.Longitude+","+p.Latitude+","+(p.Altitude/Util.M2FT)+"</coordinates>").getBytes());
              os.write("</Point>\r\n".getBytes());
              os.write("</Placemark>\r\n".getBytes());
            }
            os.write("</Folder>\r\n".getBytes());
        }
        // Добавляем маршрут
        if(route!=null && route.size()>0)
        {
            os.write("<Folder>\r\n".getBytes());
            //
            double distance=0.0;
            for (int i = 0; i < route.size(); i++)
            {
              Point p = (Point) MIDlet1.rmsManager.route.elementAt(i);
              if(i>0)
              {
                  Point prev = (Point)route.elementAt(i-1);
                  distance+=Util.distance(prev.Latitude, prev.Longitude, p.Latitude, p.Longitude);
              }
              p.Distance=distance;
            }
            //
            Vector v=Util.getDistanceFromKM(distance);
            os.write(("<description><![CDATA[Points: "+route.size()+" Length: "+(Double)v.elementAt(1)+" "+(String)v.elementAt(0)+"<br/>"+descriptionKML+"]]></description>\r\n").getBytes());
            os.write("<name>Route</name>\r\n".getBytes());
            if(!route.createKMLData(true, os))
                return false;
            os.write("</Folder>\r\n".getBytes());
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
  public String createKMLData()
  {
    StringBuffer sb=new StringBuffer();
    // Добавляем стиль точек трека
    sb.append("<Style id=\"trackPoints\">\r\n");
    sb.append("<IconStyle>\r\n");
    sb.append("<scale>0.25</scale>\r\n");
    sb.append("<Icon>\r\n");
    sb.append("<href>http://www.unteh.com/products/mobitrack/images/GETP.png</href>\r\n");
    sb.append("</Icon></IconStyle>\r\n");
    sb.append("</Style>\r\n");
    // Добавляем стиль точек маршрута
    sb.append("<Style id=\"routePoints\">\r\n");
    sb.append("<IconStyle>\r\n");
    sb.append("<Icon>\r\n");
    sb.append("<href>http://www.unteh.com/products/mobitrack/images/GER.png</href>\r\n");
    sb.append("</Icon></IconStyle>\r\n");
    sb.append("</Style>\r\n");
    // Добавляем трек
    if(currentTrack!=null && currentTrack.size()>0)
    {
        sb.append("<Folder>\r\n");
        Point p1=(Point)currentTrack.elementAt(0);
        Point p2=(Point)currentTrack.elementAt(currentTrack.size()-1);
        sb.append("<description><![CDATA[Points: "+currentTrack.size()+" Length: "+currentTrack.getLength()+" "+MIDlet1.rmsManager.getDistanceName(true));
        if(p1.getDateTime(Point.TYPE_DATE).equals(p2.getDateTime(Point.TYPE_DATE)))
            sb.append("<br/>"+p1.getDateTime(Point.TYPE_DATE)+" "+p1.getDateTime(Point.TYPE_TIME)+" - "+p2.getDateTime(Point.TYPE_TIME));
        else
            sb.append("<br/>"+p1.getDateTime(Point.TYPE_DATETIME)+" - "+p2.getDateTime(Point.TYPE_DATETIME));
        sb.append("<br/>"+descriptionKML+"]]></description>\r\n");
        sb.append("<name>Track</name>\r\n");
        sb.append(currentTrack.createKMLData(false));
        sb.append("</Folder>\r\n");
    }
    // Добавляем путевые точки
    if(points!=null && points.size()>0)
    {
        sb.append("<Folder>\r\n");
        sb.append("<description><![CDATA[Quantity: "+points.size()+"<br/>"+descriptionKML+"]]></description>\r\n");
        sb.append("<name>Waypoints</name>\r\n");
        for(int i=0; i<points.size(); i++)
        {
          Point p = (Point)points.elementAt(i);
          //
          double altitude=Math.ceil(p.Altitude*10.0)/10.0;
          if(typeUnit==Util.UNIT_METRIC)
                altitude=Math.ceil(p.Altitude*10.0/Util.M2FT)/10.0;
          //
          sb.append("<Placemark>\r\n");
          if(p.Category>0)
              sb.append(categories[p.Category].getKML());
          sb.append("<description>Altitude: "+altitude+" "+getDistanceName(false)+"</description>\r\n");
          sb.append("<name>"+p.Name+"</name>");
          sb.append("<visibility>1</visibility>");
          sb.append("<Point>\r\n");
          sb.append("<coordinates>"+p.Longitude+","+p.Latitude+","+(p.Altitude/Util.M2FT)+"</coordinates>");
          sb.append("</Point>\r\n");
          sb.append("</Placemark>\r\n");
        }
        sb.append("</Folder>\r\n");
    }
    // Добавляем маршрут
    if(route!=null && route.size()>0)
    {
        sb.append("<Folder>\r\n");
        //
        double distance=0.0;
        for (int i = 0; i < route.size(); i++)
        {
          Point p = (Point) MIDlet1.rmsManager.route.elementAt(i);
          if(i>0)
          {
              Point prev = (Point)route.elementAt(i-1);
              distance+=Util.distance(prev.Latitude, prev.Longitude, p.Latitude, p.Longitude);
          }
          p.Distance=distance;
        }
        //
        Vector v=Util.getDistanceFromKM(distance);
        sb.append("<description><![CDATA[Points: "+route.size()+" Length: "+(Double)v.elementAt(1)+" "+(String)v.elementAt(0)+"<br/>"+descriptionKML+"]]></description>\r\n");
        sb.append("<name>Route</name>\r\n");
        sb.append(route.createKMLData(true));
        sb.append("</Folder>\r\n");
    }
    //
    return sb.toString();
  }
  */
  public String createWptData()
  {
    StringBuffer sb=new StringBuffer();
    for(int i=0; i<points.size(); i++)
    {
      Point p = (Point)points.elementAt(i);
      sb.append(""+(i+1));
      sb.append(",");
      sb.append(p.Name);
      sb.append(",");
      sb.append(p.Latitude);
      sb.append(",");
      sb.append(p.Longitude);
      sb.append(",");
      sb.append(p.OleDateFromTm());
      sb.append(", 0, 3, 0, 65535, 0, , 0, 0, 0, -777, 6, 0,17,0,0,2,,,\r\n");
    }
    return sb.toString();
  }

  // DEBUG
  static public boolean setDebugInfo(String info)
  {
    try
    {
      RecordStore rs = RecordStore.openRecordStore("DebugInfo", true);
      byte[] data=info.getBytes();
      if(rs.getNumRecords()>0)
        rs.setRecord(1, data, 0, data.length);
      else
        rs.addRecord(data, 0, data.length);
      rs.closeRecordStore();
    }
    catch (RecordStoreException ex)
    {
      return false;
    }
    return true;
  }

  static public boolean clearDebugInfo()
  {
    try
    {
      RecordStore.deleteRecordStore("DebugInfo");
    }
    catch (RecordStoreException ex)
    {
      return false;
    }
    return true;
  }

  static public String getDebugInfo()
  {
    String info=null;
    try
    {
      RecordStore rs = RecordStore.openRecordStore("DebugInfo", false);
      if(rs.getNumRecords()>0)
      {
        int size=rs.getRecordSize(1);
        byte[] data=new byte[size];
        rs.getRecord(1, data, 0);
        info=new String(data);
      }
      rs.closeRecordStore();
    }
    catch (RecordStoreException ex)
    {
    }
    return info;
  }

  public void makeZoomImage(int scale)
  {
    imageZoom=null;
    if(mapType==MAP_GOOGLE_MAP || mapType==MAP_GOOGLE_SAT ||
            mapType==MAP_VEARTH_MAP || mapType==MAP_VEARTH_SAT)
      return;
    if(imageMap==null)
      return;
    if(scale==100)
      return;
    // Ищем точку на карте
    int x = (int) ( (MIDlet1.locationManager.getLongitude() - mapRect.startLongitude) * imageMap.getWidth() / mapRect.widthLongitude);
    int y = imageMap.getHeight() - (int) ( (MIDlet1.locationManager.getLatitude() - mapRect.startLatitude) * imageMap.getHeight() / mapRect.heightLatitude);
    //
    int realRadius=ZOOM_RADIUS*100/scale;
    int left=x-realRadius;
    if(left<0)
      left=0;
    leftZoom=(x-left)*scale/100;
    //
    int top=y-realRadius;
    if(top<0)
      top=0;
    topZoom=(y-top)*scale/100;
    //
    int right=x+realRadius;
    if(right>=imageMap.getWidth())
      right=imageMap.getWidth()-1;
    //
    int bottom=y+realRadius;
    if(bottom>=imageMap.getHeight())
      bottom=imageMap.getHeight()-1;
    //
    int width=right-left+1;
    int height=bottom-top+1;
    if(width>0 && height>0)
    {
        try
        {
            int[] zoomArray=new int[width*height];
            imageMap.getRGB(zoomArray, 0, width, left, top, width, height);
            imageZoom=Util.resizeImage(zoomArray, width, height, scale, true);
            //imageZoom=Util.resize(zoomArray, width, height, scale);
        }
        catch(OutOfMemoryError error)
        {
            imageZoom=null;
        }
    }
  }
  
  public boolean hasLastConfig()
  {
      return (lastConfig!=null && lastConfig.length()>0);
  }
  
  public boolean setLastConfig(String config)
  {
      lastConfig=new String(config);
      return smartSaveRMS(RmsManager.RMS_SETTINGS);
  }

  public String getLastConfig()
  {
      return lastConfig;
  }
  
  public boolean processFile(String path)
  {
      boolean result=false;
      //
      if(path.endsWith(".map") || path.endsWith(".gmi")) 
      {
          loadPixelMap(path);
          MIDlet1.updateMainWindow(SplashScreen.TYPE_INFO3);
          result=true;
      } 
      else
      if(path.endsWith(".rte") || path.endsWith(".kmr")) 
      {
          if(loadRoute(path))
            // Route
            MIDlet1.screenManager.pushScreen(new ListForm("Route", ListForm.TYPE_ROUTE, null));
          result=true;
      }
      else
      if(path.endsWith(".wpt") || path.endsWith(".kml")) 
      {
          loadWaypoints(path);
          result=true;
      }
      else
      if(path.endsWith(".ubx"))
      {
          // Demonstartion
          MIDlet1.locationManager=MIDlet1.gpsManager;
          MIDlet1.mainWindow=new SplashScreen(SplashScreen.TYPE_INFO2);
          MIDlet1.screenManager.pushScreen(MIDlet1.mainWindow);
          ListForm.demoTimer=new Timer();
          ListForm.demoTask=new Demo(path);
          ListForm.demoTimer.schedule(ListForm.demoTask, 150, 150);
          result=true;
      }
      //
      return result;
  }


  /**
   * Внешний обработчик бинарных SMS сообщений
   * @param msg полученное сообщение
   * @param data массив байт готовых к обработке
   */
  public void Handler(Message msg, byte[] data)
  {
      try
      {
          ByteArrayInputStream bais=new ByteArrayInputStream(data);
          DataInputStream dis=new DataInputStream(bais);
          byte tag=dis.readByte();
          switch(tag)
          {
              case 1:
                  // Receive waypoint
                  Point p=new Point(0.0, 0.0, 0.0);
                  String address=msg.getAddress();
                  if(address.startsWith("sms://"))
                     address=new String(address.substring(6));
                  int pos=address.indexOf(":", 6);
                  if(pos!=-1)
                     address=new String(address.substring(0, pos));
                  p.Name=address;
                  p.Category=dis.readByte();
                  p.Latitude=dis.readDouble();
                  p.Longitude=dis.readDouble();
                  p.Altitude=dis.readDouble();
                  appendWaypoint(p, true);
                  //
                  Util.playSound(Util.SOUND_SMS);
                  //
                  break;
          }      
          dis.close();
          bais.close();
      }
      catch(Exception ex)
      {
          
      }
  }
  
  /**
   * Внешний обработчик текстовых SMS сообщений
   * @param msg полученное сообщение
   * @param text текст сообщения
   */
  public void Handler(Message msg, String text)
  {      
  }
  
  /**
   * Внешний обработчик ошибок
   * @param s описание ошибки
   */
  public void Error(String s)
  {
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", s, null));
  }
  
  /**
   * Сообщение о выполненной операции
   * @param s текст сообщения
   */
  public void Info(String s)
  {      
      MIDlet1.screenManager.pushScreen(new InfoForm("Info", s, null));
  }

  public Image loadImageFromRMS(String path)
  {
    Image result=null;
    path = Util.removeControlSymbols(path, 25);
    try
    {
      RecordStore rs = RecordStore.openRecordStore(path, false);
      RecordEnumeration re = rs.enumerateRecords(null, null, false);
      if (re.hasNextElement())
      {
        byte[] data = re.nextRecord();
        result=Image.createImage(data,  0, data.length);
      }
      //
      rs.closeRecordStore();
      // Увеличиваем счетчик
      //System.out.println("Load image: "+path);
      if(mapFreq.containsKey(path))
      {
          Integer val=(Integer)mapFreq.get(path);
          mapFreq.put(path, new Integer(val.intValue()+1));
      }
      else
          mapFreq.put(path, new Integer(1));
    }
    catch (Exception ex)
    {
        result=null;
    }
    //
    return result;
  }
  
  public boolean removeOldestImageFromRMS()
  {
      int count=Integer.MAX_VALUE;
      String name="";
      Enumeration enumX=mapFreq.keys();
      while(enumX.hasMoreElements()) 
      {
          String path=(String)enumX.nextElement();
          int val=((Integer)mapFreq.get(path)).intValue();
          if(count>val)
          {
              count=val;
              name=new String(path);
          }
      }
      //
      if(name.length()>0)
      {
          try 
          {
              RecordStore.deleteRecordStore(name);
              mapFreq.remove(name);
              //System.out.println("Remove image: "+name);
          } catch (RecordStoreException ex) 
          {
              return false;
          }
      }
      //
      return true;
  }

  public boolean saveImageToRMS(String path, byte[] buffer)
  {
    path = Util.removeControlSymbols(path, 25);
    try 
    {
      RecordStore.deleteRecordStore(path);
    }
    catch (RecordStoreException ex)
    {
    }
    try 
    {
      RecordStore rs = RecordStore.openRecordStore(path, true);      
      rs.addRecord(buffer, 0, buffer.length);
      rs.closeRecordStore();
      //System.out.println("Save image: "+path+" Size: "+rs.getSizeAvailable());
    }
    catch (RecordStoreException ex)
    {
      return false;
    }
    //
    return true;
  }
  
  public boolean smartSaveRMS(int rmstype)
  {
      for(int i=0; i<5; i++)
      {
          // Пробуем записать данные
          if(saveRMS(rmstype))
              return true;
          // Если не хватает места, то чистим кеш
          removeOldestImageFromRMS();
      }
      //
      switch(rmstype)
      {
          case RMS_SETTINGS:
            MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't save settings", null));
            break;
          case RMS_TRACK:
            MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't save track", null));
            break;
      }
      //
      return false;
  }
  
  public void clearMapCache()
  {
      Enumeration enumX=mapFreq.keys();
      while(enumX.hasMoreElements()) 
      {
          String path=(String)enumX.nextElement();
          try 
          {
              RecordStore.deleteRecordStore(path);
          } 
          catch (RecordStoreException ex) 
          {
          }      
      }
      mapFreq.clear();
  }
  
  public String getSpeedName()
  {
      switch(typeUnit)
      {
          case Util.UNIT_ENGLISH: return "mph";
          case Util.UNIT_METRIC: return "kmh";
          case Util.UNIT_NAUTICAL: return "kt";
      }
      return "";
  }

  public String getAccelerationName()
  {
      switch(typeUnit)
      {
          case Util.UNIT_ENGLISH: return "ft/s2";
          case Util.UNIT_METRIC: return "m/s2";
          case Util.UNIT_NAUTICAL: return "ft/s2";
      }
      return "";
  }

  public String getDistanceName(boolean big)
  {
      switch(typeUnit)
      {
          case Util.UNIT_ENGLISH: return (big?"mi":"ft");
          case Util.UNIT_METRIC: return (big?"km":"m");
          case Util.UNIT_NAUTICAL: return (big?"nm":"ft");
      }
      return "";
  }

  public double getUnitCoeff(boolean smallUnits)
  {
    double coeff=1.0;
    if(smallUnits)
    {
        switch(typeUnit)
        {
            case Util.UNIT_ENGLISH:
            case Util.UNIT_NAUTICAL:
                // m -> ft
                coeff=Util.M2FT;
                break;
        }        
    }
    else
    {
        switch(typeUnit)
        {
            case Util.UNIT_ENGLISH:
                // km -> mi
                coeff=Util.KM2MI;
                break;
            case Util.UNIT_NAUTICAL:
                // km -> nm
                coeff=Util.KM2NM;
                break;
        }
    }
    return coeff;
  }

  public boolean isFullTrack()
  {
    if(currentTrack==null)
      return false;
    if(currentTrack.size()<MAX_TRACK_POINTS)
      return false;
    return true;
  }  
  
  public boolean appendTrackPoint(double Latitude, double Longitude, double Altitude, double Speed, String time, String date)
  {
      if(currentTrack!=null && currentTrack.size()<MAX_TRACK_POINTS)
      {
        currentTrack.append(Latitude, Longitude, Altitude, Speed, time, date);
        smartSaveRMS(RmsManager.RMS_TRACK);
        //
        if(currentTrack.size()==MAX_TRACK_POINTS)
          MIDlet1.screenManager.pushScreen(new InfoForm("Track", "The maximum quantity of points ("+MAX_TRACK_POINTS+") in track has been reached. Please send track to NetTrack server script or clear track for further processing."));
        return true;
      }
      //
      return false;
  }

  public String getDate(Calendar cal, boolean useLocalFormat)
  {
      String syear=""+cal.get(Calendar.YEAR);
      if(useLocalFormat && MIDlet1.rmsManager.typeUnit==Util.UNIT_ENGLISH)
          return (cal.get(Calendar.MONTH)+1)+"/"+cal.get(Calendar.DAY_OF_MONTH)+"/"+syear.substring(2, 4);
      else
          return cal.get(Calendar.DAY_OF_MONTH)+"/"+(cal.get(Calendar.MONTH)+1)+"/"+syear.substring(2, 4);
  }

  public String getTime(Calendar cal, boolean useLocalFormat)
  {
      int m=cal.get(Calendar.MINUTE);
      int s=cal.get(Calendar.SECOND);
      if(useLocalFormat && MIDlet1.rmsManager.typeUnit==Util.UNIT_ENGLISH)
      {
          switch(cal.get(Calendar.HOUR_OF_DAY))
          {
              case 0:
                  return "12:"+Util.withLeadZero(m)+":"+Util.withLeadZero(s)+"pm";
              case 12:
                  return "12:"+Util.withLeadZero(m)+":"+Util.withLeadZero(s)+"am";
              default:
                  return cal.get(Calendar.HOUR)+":"+Util.withLeadZero(m)+":"+Util.withLeadZero(s)+(cal.get(Calendar.AM_PM)==Calendar.AM?"am":"pm");
          }          
      }
      else
          return cal.get(Calendar.HOUR_OF_DAY)+":"+Util.withLeadZero(m)+":"+Util.withLeadZero(s);
  }
  
  public boolean appendWaypoint(Point newPoint, boolean forceOverwrite)
  {
      for(int i=0; i<points.size(); i++)
      {
          Point p=(Point)points.elementAt(i);
          if(newPoint.Name.equals(p.Name))
          {
              if(!forceOverwrite)
                  return false;
              //
              if(p==currentPoint)
                  currentPoint=newPoint;
              points.removeElementAt(i);
              break;
          }
      }
      points.addElement(newPoint);
      //
      return true;
  }
  
  public void close()
  {
      if(smsManager2!=null)
      {
          smsManager2.close();
          smsManager2=null;
      }
      if(smsManager!=null)
      {
          smsManager.close();
          smsManager=null;
      }
      //
      smartSaveRMS(RmsManager.RMS_SETTINGS);
      smartSaveRMS(RmsManager.RMS_TRACK);
  }
  
  private Rect getRects(String s, int index)
  {
      Rect rect=null;
        if(s.length()>0)
        {            
            Vector v=Util.parseString(s, ";", true);
            if(v.size()==12)
            {
                int width=Integer.parseInt((String)v.elementAt(2));
                int height=Integer.parseInt((String)v.elementAt(3));
                if(width>0 && height>0)
                {
                    int x1=Integer.parseInt((String)v.elementAt(4));
                    int y1=Integer.parseInt((String)v.elementAt(5));
                    double lon1=Double.parseDouble((String)v.elementAt(6));
                    double lat1=Double.parseDouble((String)v.elementAt(7));
                    //
                    int x2=Integer.parseInt((String)v.elementAt(8));
                    int y2=Integer.parseInt((String)v.elementAt(9));
                    double lon2=Double.parseDouble((String)v.elementAt(10));
                    double lat2=Double.parseDouble((String)v.elementAt(11));
                    //
                    rect=this.calcMapRect(width, height,
                            x1, y1, lon1, lat1,
                            x2, y2, lon2, lat2);
                }
            }
            else
            if(v.size()==1)
            {
                rect=new Rect(Double.parseDouble(MIDlet1.getProperty("minlon" + index)), 
                        Double.parseDouble(MIDlet1.getProperty("minlat" + index)),
                        Double.parseDouble(MIDlet1.getProperty("maxlon" + index)) - Double.parseDouble(MIDlet1.getProperty("minlon" + index)), 
                        Double.parseDouble(MIDlet1.getProperty("maxlat" + index)) - Double.parseDouble(MIDlet1.getProperty("minlat" + index)));
            }
        }
      return rect;
  }

  public boolean appendNMEA(String nmea)
  {
    boolean result=false;
    //
    if(!nmeaLogSave)
        return true;
    if(ListForm.demoTask!=null && ListForm.demoTask.path!=null)
        return true;
    //
    try
    {
        if(nmeaLogConnection==null)
        {
            nmeaLogConnection = (FileConnection)Connector.open("file:///"+nmeaLogPath);
            if(!nmeaLogConnection.exists())
                nmeaLogConnection.create();
        }
        if(nmeaLogOS==null)
            nmeaLogOS = nmeaLogConnection.openOutputStream(nmeaLogConnection.fileSize());
        nmeaLogOS.write(nmea.getBytes());
        if(!nmea.endsWith("\n"))
            nmeaLogOS.write(13);
        nmeaLogOS.flush();
        result=true;
    }
    catch (IOException e)
    {    
        nmeaLogSave=false;
        MIDlet1.screenManager.pushScreen(new InfoForm("NMEA log error", nmeaLogPath+"\n"+e.getMessage()+"\nSaving of log will be off", null));
    }
    catch (Exception e)
    {
        nmeaLogSave=false;
        MIDlet1.screenManager.pushScreen(new InfoForm("NMEA log error", nmeaLogPath+"\n"+e.getMessage()+"\nSaving of log will be off", null));
    }
    //
    return result;      
  }
  
  public boolean closeNMEA()
  {
        try
        {
            if(nmeaLogOS!=null)
            {
                nmeaLogOS.close();
                nmeaLogOS=null;
            }
            if(nmeaLogConnection!=null)
            {
                nmeaLogConnection.close();
                nmeaLogConnection=null;
            }
        }
        catch(IOException e)
        {    
            MIDlet1.screenManager.pushScreen(new InfoForm("NMEA log error", nmeaLogPath+"\n"+e.getMessage(), null));
            return false;
        }        
    return true;
  }
  
  public void run()
  {
      mapDirectory.removeAllElements();
      mapRects.removeAllElements();
        // Load map directory
        if(autoLoadMap && pathAutoloadMap!=null && pathAutoloadMap.length()>0)
        {
            // Try to load            
            try
            {
                FileConnection file = (FileConnection)Connector.open("file:///"+pathAutoloadMap, Connector.READ);
                int len=(int)file.fileSize();
                byte[] data=new byte[len];
                InputStream is = file.openInputStream();
                is.read(data);
                is.close();
                file.close();
                //
                ByteArrayInputStream bais=new ByteArrayInputStream(data);
                String s=null;
                while( (s=Util.readLine(bais))!=null )
                {
                    if(s.length()>0)
                    {
                        //System.out.println(s);
                        mapDirectory.addElement(s);
                        Rect rect=getRects(s, 0);
                        mapRects.addElement(rect);
                    }
                }
                //
                bais.close();     
            }
            catch(Exception ex)
            {
                MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't open maps directory: "+pathAutoloadMap+"\n"+ex.getMessage(), null));
            }
            imageMap=null;
        }
        else
        {
            for(int index=1; index<100; index++)
            {
                String s=MIDlet1.getProperty("map"+index);
                if(s==null)
                    break;
                //System.out.println(s);
                Rect rect=getRects(s, index);
                mapRects.addElement(rect);
            }        
        }      
  }
  
  public boolean Action(int code, String param, boolean flag)
  {
      StringBuffer sb=new StringBuffer();
      switch(code)
      {
          case 70:
              // Сохраняем трек
              sb.append("OziExplorer Track Point File Version 2.0\r\nWGS 84\r\nAltitude is in Feet\r\nReserved 3\r\n0,3,2951611,Demonstration Test Track ,0\r\n36\r\n");
              sb.append(currentTrack.createPltData());
              saveFile(param+".plt", sb.toString().getBytes(), flag);
              break;
          case 71:
              sb.append("OziExplorer Waypoint File Version 1.1\r\nWGS 84\r\nReserved 2\r\nmagellan\r\n");
              sb.append(createWptData());
              saveFile(param+".wpt", sb.toString().getBytes(), flag);
              break;
          case 72:
              FileConnection conn = openFile(param+".kml");
              if(conn!=null)
              {
                  try
                  {
                      OutputStream os = conn.openOutputStream();
                      os.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<kml xmlns=\"http://earth.google.com/kml/2.0\">\r\n<Document>\r\n<name>MobiTrack</name>\r\n".getBytes());
                      createKMLData(os);
                      os.write("</Document>\r\n</kml>\r\n".getBytes());
                      os.close();
                      conn.close();
                      //
                      if(flag)
                        MIDlet1.screenManager.replaceScreen(new InfoForm("Save", param+".kml was successfully saved", null));
                      else
                        MIDlet1.screenManager.pushScreen(new InfoForm("Save", param+".kml was successfully saved", null));
                  }
                  catch(IOException ex)
                  {                
                      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "action 72\n"+param+".kml\n"+ex.getMessage(), null));
                  }
              }
              break;
          case 73:
              sb.append("Summary\r\n");
              saveFile(param+".txt", sb.toString().getBytes(), flag);
              break;
          case 80:
              if(currentTrack!=null)
                currentTrack.clear();
              trip.ResetTrip();
              trip.ResetOdometer();
              trip.ResetAcceleration();
              trip.ResetAltitude();
              smartSaveRMS(RmsManager.RMS_SETTINGS);
              break;
          case 81:
              if(currentTrack!=null)
                currentTrack.clear();
              break;
          case 82:
              trip.ResetTrip();
              smartSaveRMS(RmsManager.RMS_SETTINGS);
              break;
          case 83:
              trip.ResetOdometer();
              smartSaveRMS(RmsManager.RMS_SETTINGS);
              break;
          case 84:
              trip.ResetAcceleration();
              smartSaveRMS(RmsManager.RMS_SETTINGS);
              break;
          case 85:
              trip.ResetAltitude();
              smartSaveRMS(RmsManager.RMS_SETTINGS);
              break;
          case 86:
              clearMapCache();
              smartSaveRMS(RmsManager.RMS_SETTINGS);
              break;
      }
      return false;
  }
  
  public Point getRoutePoint()
  {
    if(route==null || routePointIndex<0 || routePointIndex>=route.size())
        return null;
    return (Point)route.elementAt(routePointIndex);
  }
}
