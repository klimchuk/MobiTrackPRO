package henson.midp;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
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

public class MIDlet1 extends MIDlet
{
  static MIDlet1 instance;

  // Bluetooth соединение
  public static BluetoothManager bluetoothManager=null;
  // Соединение через COM порт
  public static CommManager commManager=null;
  // Хранение настроек
  public static RmsManager rmsManager=null;
  // Менеджер форм
  public static ScreenManager screenManager=null;
  // Работа с GPS и протоколом NMEA
  public static GpsManager gpsManager=null;
//#if JSR179
//#   // Если есть то работа с JSR-179
//#   public static LbsManager lbsManager=null;
//#endif
  // Текущий менеджер поставляющий геоинформацию
  public static ILocationManager locationManager=null;
  // Работа с сетью через HTTP
  public static NetManager netManager=null;
  
  // Работа с Google Maps
  public static GMap gMap=null;
  // Работа с Virtual Earth
  public static VEarth vEarth=null;

  // Главное окно
  public static SplashScreen mainWindow=null;

  public MIDlet1()
  {
    instance = this;
  }

  public static String getProperty(String key)
  {
    return instance.getAppProperty(key);
  }

  public void startApp()
  {
    // Экран
    if(screenManager==null)
    {
      screenManager = new ScreenManager(Display.getDisplay(this));
      screenManager.pushScreen(new SplashScreen(SplashScreen.TYPE_SPLASH));
    }
    else
    {
      // Восстановление после закрытия мидлета
      Displayable disp=screenManager.getTopWindow();
      screenManager.setCurrent(disp);
    }
    //
    if(gpsManager==null)
        gpsManager = new GpsManager();
    //
//#if JSR179
//#     if(lbsManager==null)
//#         lbsManager=new LbsManager();
//#endif
    //
    if(rmsManager==null)
    {
      rmsManager = new RmsManager();
      rmsManager.loadRMS(RmsManager.RMS_SETTINGS);
      rmsManager.loadRMS(RmsManager.RMS_TRACK);
    }
    //
    if(netManager==null)
      netManager = new NetManager();
    //
    if(gMap==null)
        gMap = new GMap();
    //
    if(vEarth==null)
        vEarth = new VEarth();
    //
    if(ListForm.demoTimer!=null)
      ListForm.demoTimer.schedule(ListForm.demoTask, 150, 150);
  }

  public void pauseApp()
  {
    // Если в демонстрационном режиме останавливаемся
    if(ListForm.demoTimer!=null)
    {
      // Останавливаем, но оставляем объект
      ListForm.demoTimer.cancel();      
    }
  }

  public void destroyApp(boolean unconditional)
  {
    if(rmsManager!=null)
    {
      rmsManager.smartSaveRMS(RmsManager.RMS_SETTINGS);
      rmsManager.smartSaveRMS(RmsManager.RMS_TRACK);
      rmsManager.close();
      rmsManager=null;
    }
    if(netManager!=null)
    {
      netManager.close();
      netManager=null;
    }
    if(bluetoothManager!=null)
    {
      if (bluetoothManager.State == BluetoothManager.STATE_CONNECTED)
        bluetoothManager.CloseConnection();
      bluetoothManager=null;
    }
    if(commManager!=null)
    {
      if (commManager.State == CommManager.STATE_CONNECTED)
        commManager.CloseConnection();
      commManager=null;
    }
  }

  public static void quitApp()
  {
    instance.destroyApp(true);
    instance.notifyDestroyed();
    instance = null;
  }

  public static void updateMainWindow(byte type)
  {
    if(mainWindow!=null)
    {
      mainWindow.type = type;
      mainWindow.repaint();
    }
  }
}
