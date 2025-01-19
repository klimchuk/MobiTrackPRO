package henson.midp.View;

import henson.midp.*;
import henson.midp.Model.*;
import javax.microedition.lcdui.*;
import javax.bluetooth.*;
import java.io.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DevicesList extends List implements CommandListener, Runnable, IUpdate
{
  //private Timer timer=null;
  private SplashScreen mc=null;
  boolean findingDevices=true;

  public DevicesList()
  {
    super("BT Devices", List.IMPLICIT);
    // Set up this Displayable to listen to command events
    setCommandListener(this);
    addCommand(CommandManager.backCommand);
    addCommand(CommandManager.refreshCommand);
    addCommand(CommandManager.sendTrackCommand2);    
    addCommand(CommandManager.sendWaypointsCommand);
    addCommand(CommandManager.sendKMLCommand);
    addCommand(CommandManager.sendScreenshotCommand);
    addCommand(CommandManager.hardwareCommand);
  }

  private void updateList()
  {
    this.deleteAll();
    // Парсим
    for(int i=0; i<MIDlet1.bluetoothManager.devicesList.size(); i++)
    {
      RemoteDevice rd=(RemoteDevice)MIDlet1.bluetoothManager.devicesList.elementAt(i);
      if(rd!=null)
      {
        try
        {
          this.append(rd.getFriendlyName(true), null);
        }
        catch (IOException ex1)
        {
          MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't get friendly name for bluetooth device: "+rd.getBluetoothAddress()+"\nDevicesList::updateList "+ex1.getMessage(), null));
        }
      }
    }
  }

  private void Updated(boolean success)
  {
    if(MIDlet1.screenManager.getTopWindow()==mc)
    {
      MIDlet1.screenManager.popScreen();
      //
      if (findingDevices)
        updateList();
      else
      {
        // Нашли SPP?
        if (MIDlet1.bluetoothManager.State == BluetoothManager.STATE_CONNECTED)
          MIDlet1.bluetoothManager.CloseConnection();
        else
        {
          if (MIDlet1.bluetoothManager.servicesList.size() == 0)
            MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Service not found", null));
          else
          {
            // Открывам первый подходящий сервис
            ServiceRecord sr = (ServiceRecord) MIDlet1.bluetoothManager.servicesList.elementAt(0);
            //
            String connectionString=sr.getConnectionURL((MIDlet1.rmsManager.useAuthenticate?ServiceRecord.AUTHENTICATE_NOENCRYPT:ServiceRecord.NOAUTHENTICATE_NOENCRYPT), false);
            if(connectionString.startsWith("btgoep:"))
              connectionString=new String("btspp:"+connectionString.substring(7));
            if (MIDlet1.bluetoothManager.OpenConnection(connectionString,
                                                        MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_GPS))
            {
              // Подключились!
              if(MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_GPS)
              {
                // Сохраняем настройки до следующего раза
                MIDlet1.rmsManager.setLastConfig(connectionString);
                // Показываем главное окно
                //MIDlet1.locationManager=MIDlet1.gpsManager;
                MIDlet1.mainWindow = new SplashScreen(SplashScreen.TYPE_INFO2);
                MIDlet1.screenManager.pushScreen(MIDlet1.mainWindow);
              }
              else
              if(MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_SENDTRACK ||
                 MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_SENDWAYPOINTS ||
                 MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_SENDKML ||
                 MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_SENDBITMAP)
              {
                // Показываем главное окно
                if(MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_SENDWAYPOINTS)
                  mc = new SplashScreen(0, "Sending waypoints...", 0x00ffffff, null, true);
                else
                if(MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_SENDTRACK)
                  mc = new SplashScreen(0, "Sending track...", 0x00ffffff, null, true);
                else
                if(MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_SENDKML)
                  mc = new SplashScreen(0, "Sending KML...", 0x00ffffff, null, true);
                else
                if(MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_SENDBITMAP)
                  mc = new SplashScreen(0, "Sending screenshot...", 0x00ffffff, null, true);
                mc.setCommandListener(this);
                MIDlet1.screenManager.pushScreen(mc);
                // Send track
                MIDlet1.bluetoothManager.PutObex();
                MIDlet1.bluetoothManager.CloseConnection();
                //
                while(MIDlet1.screenManager.getTopWindow()!=this)
                  MIDlet1.screenManager.popScreen();
              }
            }
          }
        }
      }
    }
  }

  public void Scan(int type)
  {
    if(MIDlet1.bluetoothManager.ScanDevices(this, type))
    {
      // Показываем окно
      findingDevices=true;
      //
      mc=new SplashScreen(0, "Searching for|bluetooth devices...", 0x00ffffff, null, true);
      mc.setCommandListener(this);
      MIDlet1.screenManager.pushScreen(mc);
    }
  }

  public void commandAction(Command command, Displayable displayable)
  {
    if(displayable==mc)
    {
      // Показано окно ожидания
      if(command==CommandManager.cancelCommand)
      {
        MIDlet1.screenManager.popScreen();
        //
        if(findingDevices)
          MIDlet1.bluetoothManager.CancelScanDevices();
        else
        {
          if(MIDlet1.bluetoothManager.State==BluetoothManager.STATE_CONNECTED)
            MIDlet1.bluetoothManager.CloseConnection();
          else
            MIDlet1.bluetoothManager.CancelScanServices();
        }
        //
        updateList();
      }
    }
    else
    if(command==CommandManager.backCommand)
    {
      MIDlet1.screenManager.popScreen();
    }
    else
    if(command==CommandManager.hardwareCommand)
    {
        MIDlet1.screenManager.pushScreen(new SettingsForm(SettingsForm.TYPE_SETTINGS_HARDWARE, null));
    }
    else
    if(command==CommandManager.refreshCommand)
    {
      Scan(DiscoveryAgent.GIAC);
    }
    else
    if(command==List.SELECT_COMMAND)
    {
      // Services
      if(MIDlet1.bluetoothManager.devicesList.size()==0)
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Devices not found", null));
      else
      {
          if(MIDlet1.rmsManager.fastConnect)
          {
            RemoteDevice rd=(RemoteDevice)MIDlet1.bluetoothManager.devicesList.elementAt(getSelectedIndex());
            String connectionString = "btspp://"+rd.getBluetoothAddress()+":1;authenticate="+(MIDlet1.rmsManager.useAuthenticate?"true":"false")+";encrypt=false;master=false";
            if (MIDlet1.bluetoothManager.OpenConnection(connectionString, MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_GPS))
            {
              // Подключились!
              if(MIDlet1.bluetoothManager.Type==BluetoothManager.TYPE_GPS)
              {
                // Показываем главное окно
                MIDlet1.rmsManager.setLastConfig(connectionString);
                //MIDlet1.locationManager=MIDlet1.gpsManager;
                MIDlet1.mainWindow = new SplashScreen(SplashScreen.TYPE_INFO2);
                MIDlet1.screenManager.pushScreen(MIDlet1.mainWindow);
              }
            }
          }
          else
          {
            // Сканируем сервисы (ищем Serial Port Profile)
            if (MIDlet1.bluetoothManager.ScanServices(this, this.getSelectedIndex(),
                BluetoothManager.TYPE_GPS)) {
              findingDevices = false;
              mc = new SplashScreen(0, "Connecting...", 0x00ffffff, null, true);
              mc.setCommandListener(this);
              MIDlet1.screenManager.pushScreen(mc);
            }
            else {
              MIDlet1.screenManager.pushScreen(new InfoForm("Error",
                  "BluetoothManager::ScanServices " +
                                               MIDlet1.bluetoothManager.lastError, null));
            }
          }
      }
    }
    else
    if(command==CommandManager.sendTrackCommand2 ||
       command==CommandManager.sendWaypointsCommand ||
       command==CommandManager.sendKMLCommand ||
       command==CommandManager.sendScreenshotCommand)
    {
      if(command==CommandManager.sendTrackCommand2)
      {
        if (MIDlet1.rmsManager.currentTrack.size() < 1)
        {
          InfoForm iform = new InfoForm("Track", "Can't send empty track", null);
          MIDlet1.screenManager.pushScreen(iform);
          return;
        }
      }
      //
      if(command==CommandManager.sendWaypointsCommand)
      {
        if (MIDlet1.rmsManager.points.size()<1)
        {
          InfoForm iform = new InfoForm("Waypoints", "Nothing to send", null);
          MIDlet1.screenManager.pushScreen(iform);
          return;
        }
      }
      //
      if(command==CommandManager.sendKMLCommand)
      {
        if (MIDlet1.rmsManager.currentTrack.size() < 1 &&
                MIDlet1.rmsManager.points.size()<1 &&
                !(MIDlet1.rmsManager.route!=null && MIDlet1.rmsManager.route.size()>0))
        {
          InfoForm iform = new InfoForm("KML", "Nothing to send", null);
          MIDlet1.screenManager.pushScreen(iform);
          return;
        }
      }      
      //
      if(command==CommandManager.sendScreenshotCommand)
      {
        if (MIDlet1.rmsManager.screenshot.baos==null)
        {
          InfoForm iform = new InfoForm("Screenshot", "Image not found", null);
          MIDlet1.screenManager.pushScreen(iform);
          return;
        }
      }
      //
      if(MIDlet1.bluetoothManager.devicesList.size()==0)
        MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Devices not found", null));
      else
      {
        byte obexcommand=0;
        if(command==CommandManager.sendWaypointsCommand)
           obexcommand=BluetoothManager.TYPE_SENDWAYPOINTS;
        else
        if(command==CommandManager.sendTrackCommand2)
           obexcommand=BluetoothManager.TYPE_SENDTRACK;
        else
        if(command==CommandManager.sendKMLCommand)
           obexcommand=BluetoothManager.TYPE_SENDKML;
        else
        if(command==CommandManager.sendScreenshotCommand)
           obexcommand=BluetoothManager.TYPE_SENDBITMAP;
        // Сканируем сервисы (ищем OBEX File Transfer)
        if(MIDlet1.bluetoothManager.ScanServices(this, this.getSelectedIndex(), obexcommand))
        {
          findingDevices = false;
          mc = new SplashScreen(0, "Connecting...", 0x00ffffff, null, true);
          mc.setCommandListener(this);
          MIDlet1.screenManager.pushScreen(mc);
        }
        else
        {
          MIDlet1.screenManager.pushScreen(new InfoForm("Error",
              "BluetoothManager::ScanServices "+MIDlet1.bluetoothManager.lastError, null));
        }
      }
    }
  }

  public void run()
  {
    Updated(true);
  }

  /**
   * Update
   *
   * @param data Object
   * @todo Implement this henson.midp.View.IUpdate method
   */
  public void Update(Object data)
  {
    if(data!=null)
    {
      if(data instanceof String)
        MIDlet1.screenManager.popScreen();
    }
  }
}
