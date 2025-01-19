package henson.midp.View;

import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import henson.midp.*;
import henson.midp.Model.*;
import javax.bluetooth.*;
import java.util.*;
import java.io.*;

import javax.microedition.io.file.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ListForm extends List implements CommandListener, IUpdate
{
  public final static int TYPE_MAINMENU=-1;
  public final static int TYPE_POINTS=0;
  public final static int TYPE_MAPS=1;
  public final static int TYPE_OBSERVABLES=2;
  public final static int TYPE_FILELIST=3;
  public final static int TYPE_LOAD=4;
  public final static int TYPE_SAVE=5;
  public final static int TYPE_FILESAVE=6;
  public final static int TYPE_LOAD2=7;
  public final static int TYPE_ROUTE=8;
  public final static int TYPE_CLEAR=9;
  public final static int TYPE_SETTINGS=10;
  public final static int TYPE_CATEGORY=11;

  public static Timer demoTimer=null;
  public static Demo demoTask=null;
  
  private TextBox tb=null;

  int type;
  String[] currentArray=null;
  Image imageMap=null;
  Image vectorMap=null; 

  FileConnection currentRoot=null;
  public final static String FILE_SEPARATOR =
    (System.getProperty("file.separator")!=null)?
    System.getProperty("file.separator"):
    "/";
  public Vector rootsList=new Vector();
  private final static String UPPER_DIR = "..";
  
  private Object obj=null;
  // Редактировать точку  
  private boolean editPOI=false;
  // Добавить точку перед
  private boolean insertBefore=false;
  
  private DevicesList devicesList = null;
    
  public ListForm(String name, int type, Object obj)
  {
    super(name, List.IMPLICIT);
    this.obj=obj;
    // Set up this Displayable to listen to command events
    setCommandListener(this);
    // add the Back command
    switch(type)
    {
    case TYPE_MAINMENU:
      addCommand(CommandManager.loadCommand2);
      addCommand(CommandManager.saveCommand);      
      addCommand(CommandManager.settingsCommand);
      addCommand(CommandManager.clearCommand);
      addCommand(CommandManager.helpCommand);
      addCommand(CommandManager.aboutCommand);
      addCommand(CommandManager.exitCommand);
      break;
    case TYPE_POINTS:
      if(MIDlet1.rmsManager.currentPoint!=null)
        addCommand(CommandManager.unselectCommand);
      addCommand(CommandManager.editCommand);
      addCommand(CommandManager.appendCommand);
      addCommand(CommandManager.sendCommand);
      addCommand(CommandManager.deleteCommand);
      addCommand(CommandManager.deleteAllCommand);
      addCommand(CommandManager.backCommand);
      break;
    case TYPE_MAPS:
      imageMap=Util.makeImage("/imagemap.png");
      vectorMap=Util.makeImage("/vectormap.png");
      addCommand(CommandManager.backCommand);
      break;
    case TYPE_OBSERVABLES:
      if(MIDlet1.gpsManager.currentObservable!=null)
        addCommand(CommandManager.unselectCommand);
      addCommand(CommandManager.editCommand);
      addCommand(CommandManager.appendCommand);
      addCommand(CommandManager.deleteCommand);
      addCommand(CommandManager.backCommand);
      break;
    case TYPE_FILESAVE:
      addCommand(CommandManager.saveFileCommand);
      addCommand(CommandManager.backCommand);
      break;
    case TYPE_ROUTE:
      if(MIDlet1.rmsManager.routePointIndex!=-1)
        addCommand(CommandManager.unselectCommand);
      addCommand(CommandManager.reverseCommand);
      addCommand(CommandManager.insertWptBeforeCommand);
      addCommand(CommandManager.insertWptAfterCommand);
      addCommand(CommandManager.deleteCommand);
      addCommand(CommandManager.deleteAllCommand);
      addCommand(CommandManager.backCommand);
      break;
    case TYPE_CATEGORY:
      addCommand(CommandManager.cancelCommand);
      addCommand(CommandManager.backCommand);
      break;
    default:
      addCommand(CommandManager.backCommand);
    }
    //
    this.type=type;
    updateData();
  }

  public void updateData()
  {
    this.deleteAll();
    Font boldFont=Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    //
    switch(type)
    {
      case TYPE_MAINMENU:
        this.append("Go!", null);
//#if WithJSR179
//#         this.append("Internal GPS", null);
//#endif
        this.append("Bluetooth", null);
        this.append("Serial", null);
        this.append("Without GPS", null);
        this.append("Demonstration", null);
        break;
      case TYPE_POINTS:
        int count=MIDlet1.rmsManager.points.size();
        if(count>0)
        {
            double[] distances=new double[count];
            for (int i = 0; i < count; i++)
            {
              Point p = (Point) MIDlet1.rmsManager.points.elementAt(i);
              distances[i]=Util.distance(MIDlet1.locationManager.getLatitude(), MIDlet1.locationManager.getLongitude(), p.Latitude, p.Longitude);
            }
            // Sort array of waypoints
            int j;
            for (int i = 1; i < count; i++) 
            {
                Point tmp = (Point) MIDlet1.rmsManager.points.elementAt(i);
                double dist=distances[i];
                j = i;
                while (j>0 && distances[j-1]>dist) 
                {
                    Point p = (Point) MIDlet1.rmsManager.points.elementAt(j-1);
                    MIDlet1.rmsManager.points.setElementAt(p, j);
                    distances[j]=distances[j-1];
                    j--;
                }
                MIDlet1.rmsManager.points.setElementAt(tmp, j);
                distances[j]=dist;
            }
            // Output
            for (int i = 0; i < count; i++)
            {
              Point p = (Point) MIDlet1.rmsManager.points.elementAt(i);
              Vector v=Util.getDistanceFromKM(distances[i]);
              this.append(p.Name+" ["+(Double)v.elementAt(1)+(String)v.elementAt(0)+"]", 
                      MIDlet1.rmsManager.categories[p.Category].image);
              //
              if(MIDlet1.rmsManager.currentPoint!=null)
              {
                if(MIDlet1.rmsManager.currentPoint.Name.equals(p.Name))
                {
                  this.setFont(i, boldFont);
                  this.setSelectedIndex(i, true);
                }
              }
            }
        }
        //
        break;
      case TYPE_MAPS:
        for(int i=1; i<100; i++)
        {
            String s = null;
            if(MIDlet1.rmsManager.mapDirectory.size()>0)
            {
                if(i<1 || i>MIDlet1.rmsManager.mapDirectory.size())
                    break;
                // External maps
                s=(String)MIDlet1.rmsManager.mapDirectory.elementAt(i-1);
            }
            else
                // Internal maps
                s=MIDlet1.getProperty("map"+i);
            if(s==null)
                break;
            if(s.length()==0)
                break;
            //
            Vector v=Util.parseString(s, ";", true);
          /*String path=MIDlet1.getProperty("path"+i);
          if(path.endsWith(".mtp"))
            this.append(s, vectorMap);
          else*/
            if(v.size()>1)
                s=(String)v.elementAt(0);
            this.append(s, imageMap);
          //
          /*
          if(MIDlet1.rmsManager.vectorMapNum!=0)
          {
            if (MIDlet1.getProperty("map"+MIDlet1.rmsManager.vectorMapNum).equals(s))
              this.setFont(i - 1, boldFont);
          }*/
        }
        //
        if(MIDlet1.rmsManager.imageMapNum>0 && MIDlet1.rmsManager.imageMapNum<=this.size())
            this.setFont(MIDlet1.rmsManager.imageMapNum - 1, boldFont);
        break;
      case TYPE_OBSERVABLES:
        for (int i = 0; i < MIDlet1.rmsManager.observables.size(); i++)
        {
          Observable o = (Observable) MIDlet1.rmsManager.observables.elementAt(i);
          this.append(o.name, null);
          //
          if(MIDlet1.gpsManager.currentObservable!=null)
          {
            if(MIDlet1.gpsManager.currentObservable.name.equals(o.name))
              this.setFont(i, boldFont);
          }
        }
        break;
      case TYPE_FILELIST:
      case TYPE_FILESAVE:
        String initDir = System.getProperty("fileconn.dir.memorycard");
        loadRoots();
        if (initDir != null)
        {
            try
            {
                currentRoot = (FileConnection) Connector.open(initDir, Connector.READ);
                displayCurrentRoot(type==TYPE_FILESAVE);
            }
            catch (Exception e)
            {
                MIDlet1.screenManager.pushScreen(new InfoForm("Error",e.getMessage(),null));
                displayAllRoots();
            }
        }
        else
        {
            displayAllRoots();
        }        
        break;
      case TYPE_LOAD:
        this.append("Pixel map", null);
        this.append("Route", null);
        this.append("Waypoints", null);
        this.append("Map (Google)", null);
        this.append("Satellite Map (Google)", null);        
        this.append("Map (Virtual Earth)", null);
        this.append("Satellite Map (Virtual Earth)", null);        
        break;
      case TYPE_SAVE:
        this.append("Track", null);
        this.append("Waypoints", null);
        this.append("KML", null);
        this.append("Summary", null);
        break;
      case TYPE_LOAD2:
        this.append("Pixel map", null);
        this.append("Route", null);
        this.append("Waypoints", null);
        this.append("NMEA Log", null);
        break;
      case TYPE_ROUTE:
        if(MIDlet1.rmsManager.route!=null)
        {
            double distance=0.0;
            for (int i = 0; i < MIDlet1.rmsManager.route.size(); i++)
            {
              Point p = (Point) MIDlet1.rmsManager.route.elementAt(i);
              if(i==0)
              {
                  this.append(p.Name, null);
              }
              else
              {
                  Point prev = (Point) MIDlet1.rmsManager.route.elementAt(i-1);
                  distance+=Util.distance(prev.Latitude, prev.Longitude, p.Latitude, p.Longitude);
                  Vector v=Util.getDistanceFromKM(distance);
                  this.append(p.Name+" ["+(Double)v.elementAt(1)+(String)v.elementAt(0)+"]", null);
              }
              p.Distance=distance;
            }
            //
            if(MIDlet1.rmsManager.routePointIndex!=-1)
            {
               this.setFont(MIDlet1.rmsManager.routePointIndex, boldFont);
               this.setSelectedIndex(MIDlet1.rmsManager.routePointIndex, true);
            }
        }
        break;
      case TYPE_CLEAR:
        this.append("All", null);
        this.append("Track", null);
        this.append("Trip", null);
        this.append("Odometer", null);
        this.append("Acceleration", null);
        this.append("Altitude", null);
        this.append("Map cache", null);
        break;
      case TYPE_SETTINGS:
        this.append("Common", null);
        this.append("Map", null);
        this.append("Track", null);
        this.append("Network", null);
        this.append("Route", null);
        this.append("Hardware", null);
        break;
      case TYPE_CATEGORY:
        for(int i=0; i<MIDlet1.rmsManager.categories.length; i++)
        {
            Category cat=MIDlet1.rmsManager.categories[i];
            this.append(cat.name, cat.image);
        }
        //
        if(obj instanceof Point)
        {
            Point p=(Point)obj;
            if(p.Category>=0 && p.Category<this.size())
            {
                this.setFont(p.Category, boldFont);
                this.setSelectedIndex(p.Category, true);
            }
        }
        //
        break;
    }
  }
    
  private void displayAllRoots()
  {
      setTitle("Roots");
      deleteAll();
      // List all roots
      Enumeration roots=rootsList.elements();
      while(roots.hasMoreElements())
      {
          String root=(String)roots.nextElement();
          if(root.endsWith(FILE_SEPARATOR))
              append(root.substring(1), null);
      }
      currentRoot=null;
  }
  
  private void loadRoots()
  {
    if (!rootsList.isEmpty())
        rootsList.removeAllElements();
    try 
    {
        Enumeration roots = FileSystemRegistry.listRoots();
        while (roots.hasMoreElements())
            rootsList.addElement(FILE_SEPARATOR + (String) roots.nextElement());
    }
    catch (Throwable e)
    {
        MIDlet1.screenManager.pushScreen(new InfoForm("Error",e.getMessage(),null));
    }
  }

  private void displayCurrentRoot(boolean onlyFolders)
  {
      try
      {
          setTitle(currentRoot.getURL());
          //deleteAll();
          Vector list=new Vector();
          //append(UPPER_DIR, null);
          list.addElement(UPPER_DIR);
          // List all dirs
          Enumeration listOfDirs=currentRoot.list();
          while(listOfDirs.hasMoreElements())
          {
              String currentDir=(String)listOfDirs.nextElement();
              if(currentDir.endsWith(FILE_SEPARATOR))
                  //append(currentDir, null);
                  list.addElement(currentDir);
          }
          //
          if(!onlyFolders)
          {
              Vector v=null;
              if(obj==null)
              {
                  v=new Vector();
                  v.addElement("*.*");
              }
              else
                  v=Util.parseString((String)obj, ";", true);
              for(int i=0; i<v.size(); i++)
              {
                  Enumeration listOfFiles=currentRoot.list((String)v.elementAt(i), false);
                  while(listOfFiles.hasMoreElements())
                  {
                      String currentFile=(String)listOfFiles.nextElement();
                      if(!currentFile.endsWith(FILE_SEPARATOR))
                          //append(currentFile, null);
                          list.addElement(currentFile);
                  }
              }
          }
          //
          this.smartFill(list);
      }
      catch(IOException ex)
      {
          MIDlet1.screenManager.pushScreen(new InfoForm("Error",ex.getMessage(),null));
      }
      catch(SecurityException e)
      {
          MIDlet1.screenManager.pushScreen(new InfoForm("Error",e.getMessage(),null));
      }
  }

  public void commandAction(Command command, Displayable displayable)
  {
    if(displayable==tb)
    {
        if(command==CommandManager.okCommand)
        {
            String s=new String(tb.getString());
            if(s.length()>0)
                MIDlet1.screenManager.popScreen();
            else
                return;
            //
            StringBuffer sb=new StringBuffer();
            if(tb.getMaxSize()==45)
            {
                // Сохраняем трек
                MIDlet1.rmsManager.Action(70, currentRoot.getURL()+s, true);
            }
            else
            if(tb.getMaxSize()==50)
            {
                // Сохраняем путевые точки
                MIDlet1.rmsManager.Action(71, currentRoot.getURL()+s, true);
            }
            else
            if(tb.getMaxSize()==55)
            {
                // Сохраняем KML файл для Google Earth
                MIDlet1.rmsManager.Action(72, currentRoot.getURL()+s, true);
            }
            else
            if(tb.getMaxSize()==60)
            {
                // Сохраняем информация о треке и пути
                MIDlet1.rmsManager.Action(73, currentRoot.getURL()+s, true);
            }
            else
            if(tb.getMaxSize()==30)
            {
                int i = this.getSelectedIndex();
                switch (type)
                {
                    case TYPE_POINTS:
                        if(i>=0 && i<MIDlet1.rmsManager.points.size())
                        {
                            Point p=(Point)MIDlet1.rmsManager.points.elementAt(i);
                            MIDlet1.rmsManager.smsManager.sendText(s, 0, "MobiTrackPRO\n"+"Waypoint: "+p.Name+"\n"+p.getLatitude()+"\n"+p.getLongitude());
                        }
                        break;
                }
            }
        }
        else
            MIDlet1.screenManager.popScreen();
    }
    else
    if(command==CommandManager.backCommand)
      MIDlet1.screenManager.popScreen();
    else
    if(command==CommandManager.deleteCommand)
    {
      int i=this.getSelectedIndex();
      switch (type)
      {
        case TYPE_POINTS:
          if (i >= 0 && i < MIDlet1.rmsManager.points.size())
          {
            if(MIDlet1.rmsManager.currentPoint==MIDlet1.rmsManager.points.elementAt(i))
              MIDlet1.rmsManager.currentPoint=null;
            MIDlet1.rmsManager.points.removeElementAt(i);
            this.delete(i);
            MIDlet1.rmsManager.smartSaveRMS(RmsManager.RMS_SETTINGS);
          }
          break;
        case TYPE_OBSERVABLES:
          if (i >= 0 && i < MIDlet1.rmsManager.observables.size())
          {
            if(MIDlet1.gpsManager.currentObservable==MIDlet1.rmsManager.observables.elementAt(i))
              MIDlet1.gpsManager.currentObservable=null;
            MIDlet1.rmsManager.observables.removeElementAt(i);
            this.delete(i);
            MIDlet1.rmsManager.smartSaveRMS(RmsManager.RMS_SETTINGS);
          }
          break;
      case TYPE_ROUTE:
          if (i >= 0 && i < MIDlet1.rmsManager.route.size())
          {
            if(MIDlet1.rmsManager.currentPoint==MIDlet1.rmsManager.route.elementAt(i))
              MIDlet1.rmsManager.currentPoint=null;
            MIDlet1.rmsManager.route.removeElementAt(i);
            this.delete(i);
            MIDlet1.rmsManager.smartSaveRMS(RmsManager.RMS_SETTINGS);
          }
          
          break;
      }
    }
    else
    if(command==CommandManager.deleteAllCommand)
    {
      switch (type)
      {
          case TYPE_POINTS:              
              MIDlet1.rmsManager.points.removeAllElements();
              MIDlet1.rmsManager.currentPoint=null;
          break;
          case TYPE_ROUTE:
              MIDlet1.rmsManager.route.removeAllElements();        
              MIDlet1.rmsManager.routePointIndex=-1;
              break;
      }
      //      
      this.deleteAll();
      MIDlet1.rmsManager.smartSaveRMS(RmsManager.RMS_SETTINGS);
    }
    else
    if(command==List.SELECT_COMMAND)
    {
      int i = this.getSelectedIndex();
      // Выбор текущего места
      switch (type)
      {
        case TYPE_MAINMENU:
//#if WithJSR179
//#           if(i==0)
//#           {
//#                if(!MIDlet1.rmsManager.appendNMEA("\n"))
//#                     return;          
//#                // Подключаемся к LBS
//#                if (MIDlet1.lbsManager.Open())
//#                {
//#                    // Подключились!
//#                    // Показываем главное окно
//#                    MIDlet1.locationManager=MIDlet1.lbsManager;
//#                    MIDlet1.mainWindow = new SplashScreen(SplashScreen.TYPE_INFO2);
//#                    MIDlet1.screenManager.pushScreen(MIDlet1.mainWindow);
//#                }
//#                else
//#                {
//#                    InfoForm iform=new InfoForm("Error", "Can't get location", null);
//#                    MIDlet1.screenManager.pushScreen(iform);
//#                }
//#                //
//#                return;
//#           }
//#           i--;
//#endif
          switch(i)
          {
          case 0:
            if(MIDlet1.rmsManager.hasLastConfig())
            {
                  if(!MIDlet1.rmsManager.appendNMEA("\n"))
                      return;
                  //
                  String config=MIDlet1.rmsManager.getLastConfig();
                  // Использовать последнюю известную конфигурацию для подключения к устройству сбора данных
                  if(config.startsWith("comm:"))
                  {                    
                    if(MIDlet1.commManager==null)
                        MIDlet1.commManager=new CommManager();
                    //
                    if(MIDlet1.commManager.OpenConnection(config))
                    {
                      MIDlet1.locationManager=MIDlet1.gpsManager;
                      MIDlet1.mainWindow = new SplashScreen(SplashScreen.TYPE_INFO2);
                      MIDlet1.screenManager.pushScreen(MIDlet1.mainWindow);
                    }                      
                  }
                  else
                  if(config.startsWith("btspp:"))
                  {
                    if (MIDlet1.bluetoothManager == null)
                        MIDlet1.bluetoothManager = new BluetoothManager();
                    //
                    if (MIDlet1.bluetoothManager.OpenConnection(config, true))
                    {
                        // Подключились!
                        // Показываем главное окно
                        MIDlet1.locationManager=MIDlet1.gpsManager;
                        MIDlet1.mainWindow = new SplashScreen(SplashScreen.TYPE_INFO2);
                        MIDlet1.screenManager.pushScreen(MIDlet1.mainWindow);
                    }                      
                  }
                break;
            }
          case 1:
            // Bluetooth
            MIDlet1.locationManager=MIDlet1.gpsManager;
            //
            if(devicesList==null)
                devicesList = new DevicesList();
            MIDlet1.screenManager.pushScreen(devicesList);
            // Bluetooth
            if (MIDlet1.bluetoothManager == null)
            {
              MIDlet1.bluetoothManager = new BluetoothManager();
              devicesList.Scan(DiscoveryAgent.GIAC);
            }
            break;
          case 2:
            // Serial
            MIDlet1.locationManager=MIDlet1.gpsManager;
            Vector v=CommManager.getPortsArray();
            if(v==null || v.size()<1)
            {
              MIDlet1.screenManager.pushScreen(new InfoForm("Warning", "Serial ports not found, please try bluetooth connection", null));
            }
            else
            {
              SettingsForm sf = new SettingsForm(SettingsForm.TYPE_SERIAL, v);
              MIDlet1.screenManager.pushScreen(sf);
            }
            break;
          case 3:
            // Without GPS
            MIDlet1.locationManager=MIDlet1.gpsManager;
            MIDlet1.mainWindow=new SplashScreen(SplashScreen.TYPE_INFO2);
            MIDlet1.screenManager.pushScreen(MIDlet1.mainWindow);
            break;
          case 4:
            // Demonstration
            if(!MIDlet1.rmsManager.appendNMEA("\n"))
                return;
            //
            MIDlet1.locationManager=MIDlet1.gpsManager;
            MIDlet1.mainWindow=new SplashScreen(SplashScreen.TYPE_INFO2);
            MIDlet1.screenManager.pushScreen(MIDlet1.mainWindow);
            demoTimer=new Timer();
            demoTask=new Demo(null);
            demoTimer.schedule(demoTask, 150, 150);
            break;
          }
          break;
        case TYPE_POINTS:
          // Отключаем точку Route
          MIDlet1.rmsManager.routePointIndex=-1;
          // Включаем выбранный waypoint
          MIDlet1.rmsManager.currentPoint=(Point) MIDlet1.rmsManager.points.elementAt(i);
          MIDlet1.screenManager.popScreen(1, MIDlet1.rmsManager.currentPoint);
          break;
        case TYPE_MAPS:
          MIDlet1.screenManager.popScreen();
          MIDlet1.rmsManager.selectMap(i+1);
          // Переключаем на карту
          Image selImage=this.getImage(i);
          if(selImage==imageMap)
            MIDlet1.updateMainWindow(SplashScreen.TYPE_INFO3);
          else
          if(selImage==vectorMap)
            MIDlet1.updateMainWindow(SplashScreen.TYPE_INFO7);
          break;
        case TYPE_OBSERVABLES:
          MIDlet1.gpsManager.currentObservable=(Observable) MIDlet1.rmsManager.observables.elementAt(i);
          MIDlet1.screenManager.popScreen();
          // Переключаем на компас
          MIDlet1.updateMainWindow(SplashScreen.TYPE_INFO7);
          break;
        case TYPE_FILELIST:
        case TYPE_FILESAVE:
          String selectedFile=getString(i);
          if(selectedFile.endsWith(FILE_SEPARATOR))
          {
              try
              {
                  if(currentRoot==null)
                    currentRoot=(FileConnection)Connector.open("file:///"+selectedFile, Connector.READ);
                  else
                    currentRoot.setFileConnection(selectedFile);
                  this.displayCurrentRoot(type==TYPE_FILESAVE);
              }
              catch(IOException ex)
              {
                  MIDlet1.screenManager.pushScreen(new InfoForm("Error",ex.getMessage(),null));
              }
              catch(SecurityException e)
              {
                  MIDlet1.screenManager.pushScreen(new InfoForm("Error",e.getMessage(),null));
              }
          }
          else if (selectedFile.equals(UPPER_DIR))
          {
                try
                {
                    String oldURL=currentRoot.getURL();
                    currentRoot.setFileConnection(UPPER_DIR);
                    if(oldURL.equals(currentRoot.getURL()))
                        displayAllRoots();
                    else
                        displayCurrentRoot(type==TYPE_FILESAVE);
                }
                catch (Exception e)
                {
                    MIDlet1.screenManager.pushScreen(new InfoForm("Error",e.getMessage(),null));
                }
          }   
          else
          {
              // Выбор файла
              MIDlet1.screenManager.popScreen(1, currentRoot.getURL()+selectedFile);
          }
          break;
        case TYPE_LOAD:
          switch(i)
          {
              case 0:
                  // Pixel maps
                  MIDlet1.screenManager.replaceScreen(new ListForm("Pixel maps", ListForm.TYPE_FILELIST, "*.map;*.gmi"));
                  break;
              case 1:
                  // Routes
                  MIDlet1.screenManager.replaceScreen(new ListForm("Routes", ListForm.TYPE_FILELIST, "*.rte;*.kmr"));
                  break;
              case 2:
                  // Waypoints
                  MIDlet1.screenManager.replaceScreen(new ListForm("Waypoints", ListForm.TYPE_FILELIST, "*.wpt;*.kml"));
                  break;
              case 3:
                  // Map (Google)
                  MIDlet1.netManager.sendGoogleMap();
                  MIDlet1.screenManager.popScreen();
                  break;
              case 4:
                  // Satellite Map (Google)
                  MIDlet1.netManager.sendGoogleSat();
                  MIDlet1.screenManager.popScreen();
                  break;
              case 5:
                  // Map (Virtual Earth)
                  MIDlet1.netManager.sendVEarthMap();
                  MIDlet1.screenManager.popScreen();
                  break;
              case 6:
                  // Satellite Map (Virtual Earth)
                  MIDlet1.netManager.sendVEarthSat();
                  MIDlet1.screenManager.popScreen();
                  break;
          }
          break;
        case TYPE_LOAD2:
          switch(i)
          {
              case 0:
                  // Pixel maps
                  MIDlet1.screenManager.replaceScreen(new ListForm("Pixel maps", ListForm.TYPE_FILELIST, "*.map;*.gmi"));
                  MIDlet1.rmsManager.mapType=RmsManager.MAP_PIXEL;
                  break;
              case 1:
                  // Routes
                  MIDlet1.screenManager.replaceScreen(new ListForm("Routes", ListForm.TYPE_FILELIST, "*.rte;*.kmr"));
                  break;
              case 2:
                  // Waypoints
                  MIDlet1.screenManager.replaceScreen(new ListForm("Waypoints", ListForm.TYPE_FILELIST, "*.wpt;*.kml"));
                  break;
              case 3:
                  // NMEA Log
                  MIDlet1.screenManager.replaceScreen(new ListForm("NMEA log", ListForm.TYPE_FILELIST, "*.ubx"));
                  break;
          }
          break;
        case TYPE_SAVE:
          switch(i)
          {
              case 0:
                  // Track
                  MIDlet1.screenManager.replaceScreen(new ListForm("Track", ListForm.TYPE_FILESAVE, "*.plt"));
                  break;
              case 1:
                  // Waypoints
                  MIDlet1.screenManager.replaceScreen(new ListForm("Waypoints", ListForm.TYPE_FILESAVE, "*.wpt"));
                  break;
              case 2:
                  // KML
                  MIDlet1.screenManager.replaceScreen(new ListForm("KML", ListForm.TYPE_FILESAVE, "*.kml"));
                  break;
              case 3:
                  // Summary
                  MIDlet1.screenManager.replaceScreen(new ListForm("Summary", ListForm.TYPE_FILESAVE, "*.txt"));
                  break;
          }
          break;
        case TYPE_ROUTE:
          // Отключаем текущий waypoint
          MIDlet1.rmsManager.currentPoint=null;
          // Включаем waypoint из Route
          MIDlet1.rmsManager.routePointIndex=i;
          MIDlet1.screenManager.popScreen(1, MIDlet1.rmsManager.getRoutePoint());
          break;
        case TYPE_CLEAR:
          switch(i)
          {
              // All
              case 0:
                  MIDlet1.rmsManager.Action(80, null, false);
                  break;
              // Track
              case 1:
                  MIDlet1.rmsManager.Action(81, null, false);
                  break;
              // Trip
              case 2:
                  MIDlet1.rmsManager.Action(82, null, false);
                  break;
              // Odometer
              case 3:
                  MIDlet1.rmsManager.Action(83, null, false);
                  break;
              // Odometer
              case 4:
                  MIDlet1.rmsManager.Action(84, null, false);
                  break;
              // Odometer
              case 5:
                  MIDlet1.rmsManager.Action(85, null, false);
                  break;
              // Map cache
              case 6:
                  MIDlet1.rmsManager.Action(86, null, false);
                  break;
          }
          MIDlet1.screenManager.popScreen();
          break;
        case TYPE_SETTINGS:
          switch(i)
          {
              // Common
              case 0:
                  MIDlet1.screenManager.replaceScreen(new SettingsForm(SettingsForm.TYPE_SETTINGS, null));
                  break;
              // Map
              case 1:
                  MIDlet1.screenManager.replaceScreen(new SettingsForm(SettingsForm.TYPE_SETTINGS_MAP, null));
                  break;
              // Track
              case 2:
                  MIDlet1.screenManager.replaceScreen(new SettingsForm(SettingsForm.TYPE_SETTINGS_TRACK, null));
                  break;
              // Network
              case 3:
                  MIDlet1.screenManager.replaceScreen(new SettingsForm(SettingsForm.TYPE_NETWORK, null));
                  break;
              // Route
              case 4:
                  MIDlet1.screenManager.replaceScreen(new SettingsForm(SettingsForm.TYPE_SETTINGS_ROUTE, null));
                  break;
              // Hardware
              case 5:
                  MIDlet1.screenManager.replaceScreen(new SettingsForm(SettingsForm.TYPE_SETTINGS_HARDWARE, null));
                  break;
          }
          break;
          case TYPE_CATEGORY:
              Point p=(Point)obj;
              p.Category=(byte)i;
              MIDlet1.screenManager.popScreen(2, p);
              break;
      }
    }
    else
    if(command==CommandManager.insertWptBeforeCommand)
    {
      switch (type)
      {
      case TYPE_ROUTE:
          insertBefore=true;
          MIDlet1.screenManager.pushScreen(new ListForm("Waypoints", ListForm.TYPE_POINTS, null));
          break;
      }
    }
    else
    if(command==CommandManager.insertWptAfterCommand)
    {
      switch (type)
      {
      case TYPE_ROUTE:
          insertBefore=false;
          MIDlet1.screenManager.pushScreen(new ListForm("Waypoints", ListForm.TYPE_POINTS, null));
          break;
      }
    }
    else
    if(command==CommandManager.reverseCommand)
    {
      switch (type)
      {
      case TYPE_ROUTE:
          // Переворачиваем точки
          if(MIDlet1.rmsManager.route!=null)
          {
              int size=MIDlet1.rmsManager.route.size();
              for(int i=0; i<size/2; i++)
              {
                Point p=(Point)MIDlet1.rmsManager.route.elementAt(i);
                Point p2=(Point)MIDlet1.rmsManager.route.elementAt(size-i-1);
                MIDlet1.rmsManager.route.setElementAt(p, size-i-1);
                MIDlet1.rmsManager.route.setElementAt(p2, i);
              }
              updateData();
          }
          break;
      }
    }
    else
    if(command==CommandManager.unselectCommand)
    {
      switch (type)
      {
      case TYPE_POINTS:
          MIDlet1.rmsManager.currentPoint = null;
          break;
      case TYPE_OBSERVABLES:
          MIDlet1.gpsManager.currentObservable = null;
          break;
      case TYPE_ROUTE:
          MIDlet1.rmsManager.routePointIndex = -1;
          break;
      }
      MIDlet1.screenManager.popScreen();
    }
    else
    if(command==CommandManager.appendCommand)
    {
      switch (type)
      {
        case TYPE_POINTS:
          editPOI=false;
          MIDlet1.screenManager.pushScreen(new PoiForm(new Point(0.0, 0.0, 0.0)));
          break;
        case TYPE_OBSERVABLES:
          Observable o=new Observable();
          MIDlet1.rmsManager.observables.addElement(o);
          int i=this.append(o.name, null);
          this.setSelectedIndex(i, true);
          MIDlet1.screenManager.pushScreen(new SettingsForm(SettingsForm.TYPE_OBSERVABLE, o));
          break;
      }
    }
    else
    if(command==CommandManager.editCommand)
    {
        int i = this.getSelectedIndex();
        switch (type)
        {
            case TYPE_POINTS:
                if(i>=0 && i<MIDlet1.rmsManager.points.size())
                {
                    Point p=(Point)MIDlet1.rmsManager.points.elementAt(i);
                    editPOI=true;
                    MIDlet1.screenManager.pushScreen(new PoiForm(p));
                }
                break;
            case TYPE_OBSERVABLES:
                MIDlet1.screenManager.pushScreen(new SettingsForm(SettingsForm.TYPE_OBSERVABLE,
                                       MIDlet1.rmsManager.observables.elementAt(i)));
                break;
        }
    }
    else
    if(command==CommandManager.sendCommand) 
    {
        int i = this.getSelectedIndex();
        switch (type)
        {
            case TYPE_POINTS:
                if(i>=0 && i<MIDlet1.rmsManager.points.size())
                {
                    Point p=(Point)MIDlet1.rmsManager.points.elementAt(i);
                    // Посылаем SMS
                    tb = new TextBox("Send waypoint by SMS", "", 30, TextField.PHONENUMBER);
                    tb.addCommand(CommandManager.okCommand);
                    tb.addCommand(CommandManager.cancelCommand);
                    tb.setCommandListener(this);
                    MIDlet1.screenManager.pushScreen(tb);
                }
                break;
        }
    }
    else
    if(command==CommandManager.exitCommand)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("Exit", "Are you sure?"));
    }
    else
    if(command==CommandManager.helpCommand)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("Help", SplashScreen.helpMessage, null));
    }
    else
    if(command==CommandManager.loadCommand)
    {
        MIDlet1.screenManager.pushScreen(new ListForm("Load", ListForm.TYPE_LOAD, null));
    }
    else
    if(command==CommandManager.loadCommand2)
    {
        MIDlet1.screenManager.pushScreen(new ListForm("Load", ListForm.TYPE_LOAD2, null));
    }
    else
    if(command==CommandManager.saveFileCommand)
    {
        if(type==TYPE_FILESAVE)
        {
            if(currentRoot==null)
                return;
            //
            String s=(String)obj;
            if(s.endsWith(".plt")) 
            {
                tb = new TextBox("Change name", "track", 45, TextField.ANY);
                tb.addCommand(CommandManager.okCommand);
                tb.addCommand(CommandManager.cancelCommand);
                tb.setCommandListener(this);
                MIDlet1.screenManager.pushScreen(tb);
            } 
            else
            if(s.endsWith(".wpt")) 
            {
                tb = new TextBox("Change name", "waypoint", 50, TextField.ANY);
                tb.addCommand(CommandManager.okCommand);
                tb.addCommand(CommandManager.cancelCommand);
                tb.setCommandListener(this);
                MIDlet1.screenManager.pushScreen(tb);
            }
            else
            if(s.endsWith(".kml")) 
            {
                tb = new TextBox("Change name", "google", 55, TextField.ANY);
                tb.addCommand(CommandManager.okCommand);
                tb.addCommand(CommandManager.cancelCommand);
                tb.setCommandListener(this);
                MIDlet1.screenManager.pushScreen(tb);
            }
            else
            if(s.endsWith(".txt")) 
            {
                tb = new TextBox("Change name", "Summary", 60, TextField.ANY);
                tb.addCommand(CommandManager.okCommand);
                tb.addCommand(CommandManager.cancelCommand);
                tb.setCommandListener(this);
                MIDlet1.screenManager.pushScreen(tb);
            }
        }
    }
    else
    if(command==CommandManager.saveCommand)
    {
        MIDlet1.screenManager.pushScreen(new ListForm("Save", ListForm.TYPE_SAVE, null));
    }
    else
    if(command==CommandManager.settingsCommand)
    {
      MIDlet1.screenManager.pushScreen(new ListForm("Settings", ListForm.TYPE_SETTINGS, null));
    }
    else
    if(command==CommandManager.aboutCommand)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("About", "MobiTrackPRO 1.85 UNTEH (C) 2004-2006 All rights reserved", "/Cursor.png"));
    }
    else
    if(command==CommandManager.cancelCommand)
    {
      MIDlet1.screenManager.popScreen(2, null);
    }
    else
    if(command==CommandManager.clearCommand)
    {
       MIDlet1.screenManager.pushScreen(new ListForm("Clear", ListForm.TYPE_CLEAR, null));
    }
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
      if(data instanceof Point)
      {  
        Point p=(Point)data;
        switch (type)
        {
        case TYPE_POINTS:
            // Добавить или отредактировать waypoint
            if(!editPOI)
            {            
                MIDlet1.rmsManager.points.addElement(p);
                this.append(p.Name, MIDlet1.rmsManager.categories[p.Category].image);
            }
            else
                this.set(getSelectedIndex(), p.Name, MIDlet1.rmsManager.categories[p.Category].image);
            break;
        case TYPE_ROUTE:
            // Добавить waypoint
            int sel=getSelectedIndex();
            if(sel==-1)
            {
                if(MIDlet1.rmsManager.route==null)
                    MIDlet1.rmsManager.route=new Track();
                MIDlet1.rmsManager.route.addElement(p);
                sel=0;
            }
            else
            if(insertBefore)
                MIDlet1.rmsManager.route.insertElementAt(p, sel);
            else
            {
                MIDlet1.rmsManager.route.insertElementAt(p, sel+1);
                sel++;
            }
            this.updateData();
            this.setSelectedIndex(sel, true);
            break;
        }
        MIDlet1.rmsManager.smartSaveRMS(RmsManager.RMS_SETTINGS);
      }
      if(data instanceof Observable)
      {
        Observable o=(Observable)data;
        this.set(getSelectedIndex(), o.name, null);
        MIDlet1.rmsManager.smartSaveRMS(RmsManager.RMS_SETTINGS);
      }
      if(data instanceof String)
      {
        String path=(String)data;
        if(path.equals("Yes"))
            MIDlet1.screenManager.popScreen();
        else
            MIDlet1.rmsManager.processFile(path);
      }
    }    
  }
  
  private void smartFill(Vector strings)
  {
      int newSize=strings.size();
      // Пишем новые данные
      for(int i=0; i<newSize; i++)
      {
          String s=(String)strings.elementAt(i);
          if(i<size())
              this.set(i, s, null);
          else
              this.append(s, null);
      }
      // Удаляем старые
      while(size()>newSize)
          this.delete(size()-1);
  }
}
