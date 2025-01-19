package henson.midp;

import javax.bluetooth.*;
import java.util.*;
import javax.microedition.io.*;
import henson.midp.View.*;
import java.io.*;

public class BluetoothManager implements DiscoveryListener, Runnable
{
  public final static byte STATE_INITIAL=0x01;
  public final static byte STATE_CONNECTED=0x02;
  public byte State = STATE_INITIAL;
  //
  public final static byte TYPE_GPS=0x01;
  public final static byte TYPE_SENDTRACK=0x02;
  public final static byte TYPE_SENDWAYPOINTS=0x03;
  public final static byte TYPE_SENDBITMAP=0x04;
  public final static byte TYPE_SENDKML=0x05;
  public byte Type = TYPE_GPS;
  // OBEX
  Obex obex=new Obex();
  //
  LocalDevice localDevice=null;
  DiscoveryAgent discoveryAgent=null;
  public Vector devicesList=new Vector();
  public Vector servicesList=new Vector();
  boolean busy=false;
  Runnable obj=null;
  int transID;
  // Client
  StreamConnection conn=null;
  // Server
  StreamConnectionNotifier serverconn=null;
  //
  String urlConnection="";
  // Поток входных данных
  Thread t=null;
  // Таймер проверки связи
  //Timer aliveTimer=null;
  // Последнее время получения данных
  long lastAlive=0L;

  // Последняя ошибка
  public String lastError="";

  DataInputStream is=null;
  OutputStream os=null;
  //ByteArrayOutputStream bytearrayoutputstream = null;
  
  private String prevNMEA="";
  
  // Фиксация количества сбоев
  private int attempts=0;
  
  // Prevents sleep bug in SE
  private long lastFlash=0L;
/*
  class AliveTask extends TimerTask
  {
      public void run()
      {
          long curtime = System.currentTimeMillis();
          //
          if(lastFlash-curtime>18000L)
          {
//#if NokiaGUI
//#               Util.playSound(Util.SOUND_KEYLOCK);
//#               // OFF
//#               DeviceControl.setLights(0, 0);
//#               // ON
//#               DeviceControl.setLights(0, 100);
//#endif
              lastFlash=curtime;
          }
          //
          synchronized(aliveTimer)
          {              
            if(curtime-lastAlive>30000L)
            {
              lastAlive=curtime;
              //
              MIDlet1.locationManager.setValid(false);
              Util.playSound(Util.SOUND_ERROR);
              // Закрываем потоки и подключение
              _CloseConnection();
            }
          }
      }
  }
 */
  
  public BluetoothManager()
  {
  }

  public String getName()
  {
    String name="";
    try
    {
      localDevice = LocalDevice.getLocalDevice();
      name=localDevice.getFriendlyName();
    }
    catch (BluetoothStateException ex)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "BluetoothManager::getName "+ex.getMessage(), null));
    }
    return name;
  }

  public boolean ScanDevices(Runnable obj, int type)
  {
    boolean result=false;
    try
    {
      localDevice = LocalDevice.getLocalDevice();
      discoveryAgent = localDevice.getDiscoveryAgent();
      devicesList.removeAllElements();
      if(discoveryAgent.startInquiry(type, this))
      {
        this.obj=obj;
        busy = result = true;
      }
    }
    catch (BluetoothStateException ex)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "BluetoothManager::ScanDevices Turn on bluetooth", null));
    }
    return result;
  }

  public boolean CancelScanDevices()
  {
    boolean result=false;
    try
    {
      localDevice = LocalDevice.getLocalDevice();
      discoveryAgent = localDevice.getDiscoveryAgent();
      result = discoveryAgent.cancelInquiry(this);
    }
    catch (BluetoothStateException ex)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "BluetoothManager::CancelScanDevices "+ex.getMessage(), null));
    }
    return result;
  }

  public boolean ScanServices(Runnable obj, int num, byte type)
  {
    boolean result=false;
    Type=type;

    /*
     * Service search will always give the default attributes:
     *
     ServiceRecordHandle (0x0000), ServiceClassIDList (0x0001),
     * ServiceRecordState (0x0002), ServiceID (0x0003) and
     * ProtocolDescriptorList (0x004).
     *
     * We want additional attributes, ServiceName (0x100),
     * ServiceDescription (0x101) and ProviderName (0x102).
     *
     * These hex-values must be supplied through an int array
     */

    int[] attributes = {0x100,0x101,0x102};

    UUID[] uuids=new UUID[1];

    // Ждем L2CAP
    //uuids[0]=new UUID(0x100);
    // Ждем RFCOMM
    //uuids[0]=new UUID(0x0003);
    // Ждем SPP
    //uuids[0]=new UUID(0x1101);
    if(Type==TYPE_GPS)
      uuids[0]=new UUID(0x0003);
    else
    if(Type==TYPE_SENDTRACK || Type==TYPE_SENDWAYPOINTS || Type==TYPE_SENDBITMAP || Type==TYPE_SENDKML)
      uuids[0]=new UUID(0x1105);

    try
    {
      localDevice = LocalDevice.getLocalDevice();
      discoveryAgent = localDevice.getDiscoveryAgent();
      servicesList.removeAllElements();
      transID=discoveryAgent.searchServices(attributes, uuids, (RemoteDevice)devicesList.elementAt(num), this);
      //
      this.obj=obj;
      busy = result = true;
      /*
      if(transID>0)
      {
        this.obj=obj;
        busy = result = true;
      }
      else
        lastError="BluetoothManager::ScanServices Can't start searching";
      */
    }
    catch (NullPointerException ex)
    {
      lastError="[NullPointerException] "+ex.getMessage();
    }
    catch (IllegalArgumentException ex)
    {
      lastError="[IllegalArgumentException] "+ex.getMessage();
    }
    catch (BluetoothStateException ex)
    {
      lastError="[BluetoothStateException] "+ex.getMessage();
    }
    catch (Exception ex)
    {
      lastError="[Exception] "+ex.getMessage();
    }
    return result;
  }

  public boolean CancelScanServices()
  {
    boolean result=false;
    try
    {
      localDevice = LocalDevice.getLocalDevice();
      discoveryAgent = localDevice.getDiscoveryAgent();
      result=discoveryAgent.cancelServiceSearch(transID);
    }
    catch (BluetoothStateException ex)
    {
      MIDlet1.screenManager.replaceScreen(new InfoForm("Error", "BluetoothManager::CancelScanServices [BluetoothStateException] "+ex.getMessage(), null));
    }
    return result;
  }

  public boolean CreateServer(UUID uuid, String name)
  {
    boolean result=false;
    try
    {
      localDevice = LocalDevice.getLocalDevice();
      localDevice.setDiscoverable(DiscoveryAgent.GIAC);
      serverconn = (StreamConnectionNotifier)Connector.open("btspp://localhost:" + uuid.toString()+";name="+name);
      result=true;
    }
    catch (IOException ex)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "BluetoothManager::CreateServer "+ex.getMessage(), null));
    }
    return result;
  }

  public boolean DestroyServer()
  {
    boolean result=false;
    if(serverconn!=null)
    {
      try
      {
        serverconn.close();
        localDevice = LocalDevice.getLocalDevice();
        localDevice.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
        result=true;
      }
      catch (IOException ex)
      {
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", "BluetoothManager::DestroyServer "+ex.getMessage(), null));
      }
      serverconn=null;
    }
    return result;
  }

  public boolean OpenConnection(String url, boolean async)
  {
    boolean result=false;
    urlConnection=url;
    try
    {
      conn = (StreamConnection) Connector.open(url);
      // стартуем поток
      State = STATE_CONNECTED;
      if(async)
      {
        //aliveTimer=new Timer();
        //aliveTimer.schedule(new AliveTask(), 3000L, 3000L);
        //
        t = new Thread(this);
        t.start();
      }
      //      
      result=true;
    }
    catch (Exception ex)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "BluetoothManager::OpenConnection "+ex.getMessage()+"\n"+url, null));
    }
    return result;
  }

  private String showHex(byte[] data)
  {
    StringBuffer sb=new StringBuffer();
    for(int i=0; i<data.length; i++)
    {
      String s=Integer.toHexString(data[i]);
      while(s.length()<2)
        s="0"+s;
      while(s.length()>2)
        s=s.substring(1);
      sb.append("0x"+s+" ");
    }
    return sb.toString();
  }

  private boolean ReceiveData()
  {
    int len=0;
    boolean result=false;
    if(conn==null)
    {
      // Переподключаемся
      try
      {     
          conn = (StreamConnection) Connector.open(this.urlConnection, Connector.READ, true);
          State = STATE_CONNECTED;
          attempts = 0;
      }
      catch(IOException ioex)
      {
          attempts++;
          Util.playSound(Util.SOUND_ERROR);
          // Пауза перед повторным подключением
          try
          {
            Thread.sleep(1000L);
          }
          catch(InterruptedException iex)
          {
              
          }
          if(attempts>5)
          {
            MIDlet1.screenManager.pushScreen(new InfoForm("Error", "BluetoothManager::ReceiveData Can't repair connection to GPS. Please reconnect manually.", null));
            conn=null;
            return false;
          }
          return true;
      }        
    }
      //return result;
    try
    {
      if(is==null)
        is=conn.openDataInputStream();
      if(is==null)
        return false;
      if(is.available()==0)
        return true;
      //
      int ch = 0;
//each NMEA Message ends with <CR><LF>
      /*
      if(bytearrayoutputstream==null)
        bytearrayoutputstream=new ByteArrayOutputStream();
      else
        bytearrayoutputstream.reset();*/
      //
      if(Type==TYPE_GPS)
      {
          /*
        while ( (ch = is.read()) != '\n')
          bytearrayoutputstream.write(ch);
        bytearrayoutputstream.flush();
        byte[] b = bytearrayoutputstream.toByteArray();
        if (b != null && b.length > 0) {
          len = b.length;
          //
          MIDlet1.gpsManager.Append(new String(b));
        }
        //
        result=true;*/
          byte[] mainBuffer=new byte[256];
          if((len=is.read(mainBuffer))>0)
          {
              attempts=0;
              String nmea=prevNMEA+new String(mainBuffer, 0, len);
              int pos=nmea.lastIndexOf('\n');
              if(pos!=-1)
              {
                  Vector vvx=Util.parseString(nmea.substring(0, pos), "\n", true);
                  if(vvx!=null && vvx.size()>0)
                  {
                    for(int k=0; k<vvx.size(); k++)
                    {
                        String str=(String)vvx.elementAt(k);
                        if(str.startsWith("$GP"))
                            MIDlet1.gpsManager.AnalyzeNMEA(str);
                    }
                  }
                  prevNMEA=nmea.substring(pos+1);
              }
              else
                  prevNMEA=nmea;
          }
          //
          result=true;
      }
      else
      if(Type==TYPE_SENDTRACK ||
         Type==TYPE_SENDWAYPOINTS ||
         Type==TYPE_SENDKML ||
         Type==TYPE_SENDBITMAP)
      {
        ch=is.read();
        //MIDlet1.screenManager.pushScreen(new InfoForm("Send trackX", "Info "+ch, null));
        // длина
        byte hbyte=(byte)is.read();
        //MIDlet1.screenManager.pushScreen(new InfoForm("BTInfo", "Step2"+hbyte, null));
        byte lbyte=(byte)is.read();
        //MIDlet1.screenManager.pushScreen(new InfoForm("BTInfo", "Step3"+lbyte, null));
        len=((int)hbyte*256+(int)lbyte);
        //
        if(len>3)
        {
          byte[] buffer = new byte[len - 3];
          int cnt = 0;
          while (cnt < buffer.length)
          {
            int k = is.read();
            if (k != -1)
              buffer[cnt++] = (byte) k;
          }
        }
        //
        if(obex.getData()==null)
        {
          // Самое начало соединения
          if(Obex.OBEX_SUCCESS==(byte)ch)
          {
            // Все OK
            // Заполняем массив данными
            StringBuffer sb=new StringBuffer();
            if(Type==TYPE_SENDTRACK)
            {
              // Current track
              sb.append("OziExplorer Track Point File Version 2.0\r\nWGS 84\r\nAltitude is in Feet\r\nReserved 3\r\n0,3,2951611,");
              sb.append(MIDlet1.rmsManager.currentTrack.getPltName());
              sb.append(",0\r\n");
              sb.append(MIDlet1.rmsManager.currentTrack.createPltData());
              obex.setData(sb.toString().getBytes());
            }
            else
            if(Type==TYPE_SENDWAYPOINTS)
            {
              // Waypoints
              sb.append("OziExplorer Waypoint File Version 1.1\r\nWGS 84\r\nReserved 2\r\nmagellan\r\n");
              sb.append(MIDlet1.rmsManager.createWptData());
              obex.setData(sb.toString().getBytes());
            }
            else
            if(Type==TYPE_SENDKML)
            {
              // Waypoints&Current track
              ByteArrayOutputStream baos=new ByteArrayOutputStream();
              baos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<kml xmlns=\"http://earth.google.com/kml/2.0\">\r\n<Document>\r\n<name>MobiTrack</name>\r\n".getBytes());
              MIDlet1.rmsManager.createKMLData(baos);
              baos.write("</Document>\r\n</kml>\r\n".getBytes());
              obex.setData(baos.toByteArray());
              baos.close();
            }
            else
            if(Type==TYPE_SENDBITMAP)
            {
              // Screenshot
              obex.setData(MIDlet1.rmsManager.screenshot.baos.toByteArray());
            }
            result=true;
          }
          else
            MIDlet1.screenManager.pushScreen(new InfoForm("Sending", "Error during connection with server", null));
        }
        else
        {
          // Продолжаем если все OK
          if(Obex.OBEX_CONTINUE==(byte)ch)
          {
            // CONTINUE
            result=true;
          }
          else
          if(Obex.OBEX_SUCCESS==(byte)ch)
          {
            // END
            obex.setData(null);
          }
          else
          {
            MIDlet1.screenManager.pushScreen(new InfoForm("Sending", "Error during sending with OBEX", null));
            obex.setData(null);
          }
        }
      }
    }
    catch (IOException ex)
    {
      attempts++;
      MIDlet1.locationManager.setValid(false);
      Util.playSound(Util.SOUND_ERROR);
      // Закрываем потоки и подключение
      _CloseConnection();
      //
      if(Type!=TYPE_GPS || attempts>3)
      {
          // Для не GPS сразу выдаем ошибку
          MIDlet1.screenManager.pushScreen(new InfoForm("Error", "BluetoothManager::ReceiveData Data length:"+len+" byte(s). Connection is lost! Please reconnect your device.", null));
          return false;
      }
      result=true;
    }
    return result;
  }

  public boolean SendData(byte[] data)
  {
    boolean result=false;
    if(conn==null)
      return result;
    try
    {
      if(os==null)
        os=conn.openOutputStream();
      if(os==null)
      {
        lastError="Can't open output stream";
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", "BluetoothManager::SendData "+lastError, null));
        return false;
      }
      //
      os.write(data);
      os.flush();
      result=true;
    }
    catch (Exception ex)
    {
      lastError=ex.getMessage();
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "BluetoothManager::SendData "+ex.getMessage(), null));
    }
    return result;
  }
  
  private void _CloseConnection()
  {
      try
      {
        if(is!=null)
          is.close();          
      }
      catch(IOException ioe)   {     }
      try
      {
        if(os!=null)
          os.close();          
      }
      catch(IOException ioe)   {     }
      try
      {
        if(conn!=null)
          conn.close();          
      }
      catch(IOException ioe)   {     }
      //
      is=null;
      os=null;
      conn=null;      
  }

  public boolean CloseConnection()
  {
    boolean result=false;
    State = STATE_INITIAL;
    if(t!=null)
    {
      try
      {
          synchronized(t)
          {            
            // Ждем завершения
            t.wait(2000L);
          }
          result=true;
      }
      catch (Exception ex)
      {
          MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Error while closing connection:"+ex.getMessage(), null));
      }
    }
    _CloseConnection();
    return result;
  }

  public void deviceDiscovered(RemoteDevice remoteDevice,
                               DeviceClass deviceClass)
  {
    devicesList.addElement(remoteDevice);
  }

  public void servicesDiscovered(int int0, ServiceRecord[] serviceRecordArray)
  {
    for(int i=0; i<serviceRecordArray.length; i++)
      servicesList.addElement(serviceRecordArray[i]);
  }

  public void serviceSearchCompleted(int int0, int int1)
  {
    String result="Result: ";
    switch(int1)
    {
        case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
           result+="COMPLETED";
           break;
        case DiscoveryListener.SERVICE_SEARCH_ERROR:
           result+="ERROR";
           break;
        case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
           result+="TERMINATED";
           break;
        case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
           result+="NO_RECORDS";
           break;
        case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
           result+="DEVICE_NOT_REACHABLE";
           break;
        default:
          result+=int1;
    }
    //
    if(int1!=DiscoveryListener.SERVICE_SEARCH_COMPLETED)
      MIDlet1.screenManager.replaceScreen(new InfoForm("Services", result, null));
    //
    busy=false;
    Thread t=new Thread(obj);
    t.start();
    obj=null;
  }

  public void inquiryCompleted(int int0)
  {
    String result="Result: ";
    switch(int0)
    {
        case DiscoveryListener.INQUIRY_COMPLETED:
           result+="COMPLETED";
           break;
        case DiscoveryListener.INQUIRY_ERROR:
           result+="ERROR";
           break;
        case DiscoveryListener.INQUIRY_TERMINATED:
           result+="TERMINATED";
           break;
        default:
          result+=int0;
    }
    //
    if(int0!=DiscoveryListener.INQUIRY_COMPLETED)
      MIDlet1.screenManager.replaceScreen(new InfoForm("Devices", result, null));
    //
    busy=false;
    Thread t=new Thread(obj);
    t.start();
    obj=null;
  }

  public void run()
  {
    prevNMEA="";
    while(State == STATE_CONNECTED)
    {
        /*
        if(aliveTimer!=null)
        {
            synchronized(aliveTimer)
            {
                lastAlive=System.currentTimeMillis();
            }
        }
         */
        if(!ReceiveData())
            break;
        // S65
        Util.wait100();
    }
    //
    /*
    if(aliveTimer!=null)
    {
        aliveTimer.cancel();
        aliveTimer=null;
    }*/
    //
    synchronized(t)
    {
        t.notify();
    }
    t=null;
  }

  public void PutObex()
  {
    int sent=0;
    // CONNECT      
    MIDlet1.screenManager.replaceScreen(new InfoForm("Sending", "Connecting...", null));
    SendData(obex.connect());
    MIDlet1.screenManager.replaceScreen(new InfoForm("Sending", "Connected", null));
    Util.wait100();
    Util.wait100();
    //
    if(ReceiveData())
    {
      MIDlet1.screenManager.replaceScreen(new InfoForm("Sending", "Preparing...", null));
      byte[] data = null;
      if(Type==this.TYPE_SENDTRACK)
        data=obex.put("Track.plt");
      else
      if(Type==this.TYPE_SENDWAYPOINTS)
        data=obex.put("Waypoints.wpt");
      else
      if(Type==this.TYPE_SENDKML)
      {
        String name=MIDlet1.locationManager.getDate(true);
        data=obex.put("MobiTrack"+name.replace('/','_')+".kml");
      }
      else
      if(Type==this.TYPE_SENDBITMAP)
        data=obex.put("Screenshot.bmp");
      MIDlet1.screenManager.replaceScreen(new InfoForm("Sending", "Sending...", null));
      //
      if (data != null)
      {
        SendData(data);
        sent+=data.length;
        MIDlet1.screenManager.replaceScreen(new InfoForm("Receiving", "Sending..."+sent, null));
        Util.wait100();
        //
        while (ReceiveData())
        {            
          data = obex.getPutChunk();
          if (data == null)
            break;          
          MIDlet1.screenManager.replaceScreen(new InfoForm("Sending", "Sending..."+sent, null));
          SendData(data);          
          sent+=data.length;
          MIDlet1.screenManager.replaceScreen(new InfoForm("PutObex", "Sending..."+sent, null));
          Util.wait100();
       }
      }      
      // DISCONNECT
      MIDlet1.screenManager.replaceScreen(new InfoForm("PutObex", "Disconnecting...", null));
      SendData(obex.disconnect());
      obex.setData(null);
    }
  }
}
