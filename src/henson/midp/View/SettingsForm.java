package henson.midp.View;

import javax.microedition.lcdui.*;
import henson.midp.*;
import henson.midp.Model.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SettingsForm extends Form implements CommandListener
{
  public final static int TYPE_NETWORK=1;
  public final static int TYPE_SETTINGS=2;
  public final static int TYPE_REGISTRATION=3;
  public final static int TYPE_OBSERVABLE=4;
  public final static int TYPE_SERIAL=5;
  public final static int TYPE_SEND_MESSAGE=6;
  public final static int TYPE_SETTINGS_MAP=7;
  public final static int TYPE_SETTINGS_TRACK=8;
  public final static int TYPE_SETTINGS_ROUTE=9;
  public final static int TYPE_SETTINGS_HARDWARE=10;

  ChoiceGroup cg=null;
  ChoiceGroup cg2=null;
  ChoiceGroup cg3=null;
  ChoiceGroup cg4=null;
  TextField tf=null;
  TextField tf2=null;
  TextField tf3=null;
  TextField tf4=null;
  TextField tf5=null;
  TextField tf6=null;
  Gauge g=null;
  Object obj=null;

  int mode=0;

  public SettingsForm(int mode, Object obj)
  {
    super("Settings");
    //
    this.mode=mode;
    this.obj=obj;
    //
    switch(mode)
    {
    case TYPE_NETWORK:
       this.setTitle("Network");
       //
       cg=new ChoiceGroup("", Choice.MULTIPLE);
//#if JSR179
//#        cg.append("Use KMLGPS", null);
//#else
       cg.append("Use NetGPS/KMLGPS", null);
//#endif
       //
       boolean[] flags=new boolean[1];
       flags[0]=MIDlet1.rmsManager.useNetGPS;
       cg.setSelectedFlags(flags);
       //
       this.append(cg);
       //
//#if JSR179
//#        tf=new TextField("Address KMLGPS", ""+MIDlet1.rmsManager.addressNetGPS, 250, TextField.URL);
//#else
       tf=new TextField("Address NetGPS/KMLGPS", ""+MIDlet1.rmsManager.addressNetGPS, 250, TextField.URL);
//#endif
       this.append(tf);
       tf3=new TextField("Login", ""+MIDlet1.rmsManager.login, 12, TextField.ANY);
       this.append(tf3);
       tf4=new TextField("Password", ""+MIDlet1.rmsManager.password, 12, TextField.PASSWORD);
       this.append(tf4);
       tf2=new TextField("Interval, seconds", ""+MIDlet1.rmsManager.intervalNetGPS, 5, TextField.NUMERIC);
       this.append(tf2);
       tf6=new TextField("Minimal interval, "+MIDlet1.rmsManager.getDistanceName(false), ""+MIDlet1.rmsManager.distanceNetGPS, 10, TextField.NUMERIC);
       this.append(tf6);
       tf5=new TextField("Address NetTrack", ""+MIDlet1.rmsManager.addressNetTrack, 250, TextField.URL);
       this.append(tf5);
       //
       break;
    case TYPE_SETTINGS:
      this.setTitle("Settings");
      //
      cg4=new ChoiceGroup("Orientation for track and compass", Choice.EXCLUSIVE);
      cg4.append("North Up", null);
      cg4.append("Track Up", null);
      cg4.setSelectedIndex(MIDlet1.rmsManager.autoMapRotation?1:0, true);
      this.append(cg4);
      //
      cg=new ChoiceGroup("Features", Choice.MULTIPLE);
      cg.append("Autoload map", null);
      cg.append("Check turns of track", null);
      cg.append("Use sound", null);
      cg.append("Use trip computer", null);
      cg.append("Save NMEA log", null);
      flags=new boolean[5];
      flags[0]=MIDlet1.rmsManager.autoLoadMap;
      flags[1]=MIDlet1.rmsManager.checkTurns;
      flags[2]=MIDlet1.rmsManager.useSound;
      flags[3]=MIDlet1.rmsManager.useTrip;
      flags[4]=MIDlet1.rmsManager.nmeaLogSave;
      cg.setSelectedFlags(flags);
      this.append(cg);
      //
      tf=new TextField("Path to autoload maps directory", ""+MIDlet1.rmsManager.pathAutoloadMap, 100, TextField.ANY);
      this.append(tf);
      //
      tf2=new TextField("Path to NMEA log", ""+MIDlet1.rmsManager.nmeaLogPath, 100, TextField.ANY);
      this.append(tf2);
      //
      cg2=new ChoiceGroup("Units", Choice.EXCLUSIVE);
      cg2.append("English system", null);
      cg2.append("Metric system", null);
      cg2.append("Nautical system", null);
      cg2.setSelectedIndex(MIDlet1.rmsManager.typeUnit, true);
      this.append(cg2);
      //
      tf3=new TextField("Path for autosave", ""+MIDlet1.rmsManager.fastsavePath, 100, TextField.ANY);
      this.append(tf3);
      //
      tf4=new TextField("UTC offset, minutes", ""+MIDlet1.rmsManager.utcOffset, 4, TextField.NUMERIC);
      this.append(tf4);
      //
      cg3=new ChoiceGroup("Geoid correction", Choice.EXCLUSIVE);
      cg3.append("Negative", null);
      cg3.append("Ignore", null);
      cg3.append("Positive", null);
      cg3.setSelectedIndex(MIDlet1.rmsManager.geoidCorrection, true);
      this.append(cg3);
      //
      break;
    case TYPE_REGISTRATION:
      this.setTitle("Registration");
      //
      tf=new TextField("Name", ""+MIDlet1.rmsManager.userName, 250, TextField.EMAILADDR);
      this.append(tf);
      tf2=new TextField("Key", ""+MIDlet1.rmsManager.userKey, 12, TextField.PASSWORD);
      this.append(tf2);
      this.append("To register please send e-mail with subject 'MobiTrack' on henson@unteh.com. In this e-mail please specify your contact e-mail, place and order ID of purchasing. 'Name' and 'Key' will be sent to you in one business day.");
      //
      break;
    case TYPE_OBSERVABLE:
       this.setTitle("Observable");
       Observable o=(Observable)obj;
       //
       tf5=new TextField("Name", o.name, 250, TextField.URL);
       this.append(tf5);
       //
       cg=new ChoiceGroup("", Choice.MULTIPLE);
       cg.append("Active", null);
       cg.append("Visible", null);
       cg.append("Show Track", null);
       //
       flags=new boolean[3];
       flags[0]=o.active;
       flags[1]=o.visible;
       flags[2]=o.showTrack;
       cg.setSelectedFlags(flags);
       //
       this.append(cg);
       //
       tf=new TextField("Address NetGPS", ""+o.path, 250, TextField.URL);
       this.append(tf);
       tf3=new TextField("Login", ""+o.login, 12, TextField.ANY);
       this.append(tf3);
       tf4=new TextField("Password", ""+o.password, 12, TextField.PASSWORD);
       this.append(tf4);
       tf2=new TextField("Interval, seconds", ""+o.interval, 5, TextField.NUMERIC);
       this.append(tf2);
       //
       break;
    case TYPE_SERIAL:
      //
      Vector v=(Vector)obj;
      cg=new ChoiceGroup("Port", Choice.EXCLUSIVE);
      for(int i=0; i<v.size(); i++)
      {
        String s=(String)v.elementAt(i);
        cg.append(s, null);
      }
      cg.setSelectedIndex(0, true);
      this.append(cg);
      //
      cg2=new ChoiceGroup("Baud rate", Choice.EXCLUSIVE);
      cg2.append("4800", null);
      cg2.append("9600", null);
      cg2.append("38400", null);
      cg2.append("57600", null);
      cg2.append("115200", null);
      cg2.setSelectedIndex(0, true);
      this.append(cg2);
      //
      break;
    case TYPE_SEND_MESSAGE:
      tf=new TextField("Contact (phone number, e-mail, ICQ)", ""+SpotMessage.MyKeyID, 64, TextField.ANY);
      this.append(tf);
      tf3=new TextField("Name", ""+SpotMessage.MyName, 128, TextField.ANY);
      this.append(tf3);
      tf4=new TextField("Message", "", 256, TextField.ANY);
      this.append(tf4);
      //
      break;
    case TYPE_SETTINGS_MAP:
       this.setTitle("Map Settings");
       //
       cg=new ChoiceGroup("Show", Choice.MULTIPLE);
       cg.append("Track", null);
       cg.append("Route", null);
       cg.append("Waypoints", null);
       cg.append("Waypoints titles", null);
       cg.append("Direction to selected waypoint", null);
       //
       flags=new boolean[5];
       flags[0]=MIDlet1.rmsManager.showTrackMap;
       flags[1]=MIDlet1.rmsManager.showRouteMap;
       flags[2]=MIDlet1.rmsManager.showWaypointsMap;
       flags[3]=MIDlet1.rmsManager.showWaypointsTitleMap;
       flags[4]=MIDlet1.rmsManager.showTargetMap;
       cg.setSelectedFlags(flags);
       this.append(cg);
       //
       break;
    case TYPE_SETTINGS_TRACK:
       this.setTitle("Track Settings");
       //
       tf=new TextField("Interval between points of track, seconds", ""+MIDlet1.rmsManager.intervalTrack, 5, TextField.NUMERIC);
       this.append(tf);
       //
       tf2=new TextField("Minimal distance between points of track, "+MIDlet1.rmsManager.getDistanceName(false), ""+MIDlet1.rmsManager.distanceTrack, 10, TextField.NUMERIC);
       this.append(tf2);
       //
       tf3=new TextField("Break of track if delay more than, seconds", ""+MIDlet1.rmsManager.breakTrackDelay, 5, TextField.NUMERIC);
       this.append(tf3);
       //
       cg=new ChoiceGroup("Show", Choice.MULTIPLE);
       cg.append("Track", null);
       cg.append("Route", null);
       cg.append("Waypoints", null);
       cg.append("Waypoints titles", null);
       cg.append("Direction to selected waypoint", null);
       //
       flags=new boolean[5];
       flags[0]=MIDlet1.rmsManager.showTrackTrack;
       flags[1]=MIDlet1.rmsManager.showRouteTrack;
       flags[2]=MIDlet1.rmsManager.showWaypointsTrack;
       flags[3]=MIDlet1.rmsManager.showWaypointsTitleTrack;
       flags[4]=MIDlet1.rmsManager.showTargetTrack;
       cg.setSelectedFlags(flags);
       this.append(cg);
       //
       break;
    case TYPE_SETTINGS_ROUTE:
      this.setTitle("Route");
      //
      tf3=new TextField("Sound alarm before waypoint (route), "+(MIDlet1.rmsManager.typeUnit==Util.UNIT_METRIC?"m":"ft"), ""+MIDlet1.rmsManager.pointMarginSound, 10, TextField.DECIMAL);
      this.append(tf3);
      tf4=new TextField("Switch to next waypoint (route), "+(MIDlet1.rmsManager.typeUnit==Util.UNIT_METRIC?"m":"ft"), ""+MIDlet1.rmsManager.pointMarginNext, 10, TextField.DECIMAL);
      this.append(tf4);
      //
      break;
    case TYPE_SETTINGS_HARDWARE:
      this.setTitle("Hardware");
      //
      cg=new ChoiceGroup("Bluetooth GPS", Choice.MULTIPLE);
      cg.append("Fast connect", null);
      cg.append("Use authenticate", null);
      flags=new boolean[2];
      flags[0]=MIDlet1.rmsManager.fastConnect;
      flags[1]=MIDlet1.rmsManager.useAuthenticate;
      cg.setSelectedFlags(flags);
      this.append(cg);
      //
//#if NokiaGUI
//#       g=new Gauge("Backlight level", true, 10, MIDlet1.rmsManager.backlightLevel/10);
//#       this.append(g);
//#endif
      //
      break;
    }
    //
    setCommandListener(this);
    addCommand(CommandManager.okCommand);
    addCommand(CommandManager.cancelCommand);
  }

  public void commandAction(Command command, Displayable displayable)
  {
    if(command==CommandManager.okCommand)
    {
      boolean[] flags=new boolean[10];
      if(cg!=null)
        cg.getSelectedFlags(flags);
      // Записываем изменения
      switch(mode)
      {
      case TYPE_NETWORK:
        // Не может быть пустого логина или пароля
        if(flags[0])
        {
          String l=tf3.getString();
          String p=tf4.getString();
          if(l.length()==0 || p.length()==0)
          {
            MIDlet1.screenManager.pushScreen(new InfoForm("Error", "You can't use NetGPS/KMLGPS or NetTrack with empty login or password!", null));
            return;
          }
        }
        //
        MIDlet1.rmsManager.useNetGPS=flags[0];
        MIDlet1.rmsManager.addressNetGPS=tf.getString();
        MIDlet1.rmsManager.addressNetTrack=tf5.getString();
        MIDlet1.rmsManager.login=tf3.getString();
        MIDlet1.rmsManager.password=tf4.getString();
        MIDlet1.rmsManager.setInterval(Integer.parseInt(tf2.getString()), true);
        MIDlet1.rmsManager.distanceNetGPS=Integer.parseInt(tf6.getString());
        //
        break;
      case TYPE_SETTINGS:
        MIDlet1.rmsManager.autoMapRotation=(cg4.getSelectedIndex()==1);
        //
        MIDlet1.rmsManager.autoLoadMap=flags[0];
        MIDlet1.rmsManager.checkTurns=flags[1];
        MIDlet1.rmsManager.useSound=flags[2];
        MIDlet1.rmsManager.useTrip=flags[3];
        MIDlet1.rmsManager.nmeaLogSave=flags[4];
        //
        MIDlet1.rmsManager.pathAutoloadMap=tf.getString();
        MIDlet1.rmsManager.loadAutoloadMap();
        MIDlet1.rmsManager.nmeaLogPath=tf2.getString();
        //
        MIDlet1.rmsManager.geoidCorrection=cg3.getSelectedIndex();
        //
        MIDlet1.rmsManager.typeUnit=cg2.getSelectedIndex();
        //
        MIDlet1.rmsManager.fastsavePath=tf3.getString();
        if(!MIDlet1.rmsManager.fastsavePath.endsWith("/"))
            MIDlet1.rmsManager.fastsavePath = MIDlet1.rmsManager.fastsavePath+"/";
        //
        MIDlet1.rmsManager.utcOffset=Integer.parseInt(tf4.getString());
        //
        break;
      case TYPE_REGISTRATION:
        MIDlet1.rmsManager.userName=tf.getString();
        MIDlet1.rmsManager.userKey=tf2.getString();
        break;
      case TYPE_OBSERVABLE:
        // Не может быть пустого логина или пароля
        if(flags[0])
        {
          String l=tf3.getString();
          String p=tf4.getString();
          if(l.length()==0 || p.length()==0)
          {
            MIDlet1.screenManager.pushScreen(new InfoForm("Error", "You can't use NetGPS/KMLGPS with empty login or password!", null));
            return;
          }
        }
        //
        Observable o=(Observable)obj;
        o.active=flags[0];
        o.visible=flags[1];
        o.showTrack=flags[2];
        o.path=tf.getString();
        o.name=tf5.getString();
        o.login=tf3.getString();
        o.password=tf4.getString();
        o.setInterval(tf2.getString());
        MIDlet1.screenManager.popScreen(1, obj);
        return;
      case TYPE_SERIAL:
        String port=(String)((Vector)obj).elementAt(cg.getSelectedIndex());
        //
        int baudrate=0;
        switch(cg2.getSelectedIndex())
        {
          case 0: baudrate=4800; break;
          case 1: baudrate=9600; break;
          case 2: baudrate=38400; break;
          case 3: baudrate=57600; break;
          case 4: baudrate=115200; break;
        }
        //
        if(MIDlet1.commManager==null)
          MIDlet1.commManager=new CommManager();
        //
        String connectionString="comm:" + port + ";baudrate=" + baudrate;
        if(MIDlet1.commManager.OpenConnection(connectionString))
        {
          // Сохраняем настройки до следующего раза
          MIDlet1.rmsManager.setLastConfig(connectionString);
          //
          MIDlet1.mainWindow = new SplashScreen(SplashScreen.TYPE_INFO2);
          MIDlet1.screenManager.pushScreen(MIDlet1.mainWindow);
        }
        return;
      case TYPE_SEND_MESSAGE:
        SpotMessage.MyKeyID=tf.getString();
        SpotMessage.MyName=tf3.getString();
        MIDlet1.netManager.sendSpotMessage(tf4.getString());
        //
        break;
      case TYPE_SETTINGS_MAP:
        MIDlet1.rmsManager.showTrackMap=flags[0];
        MIDlet1.rmsManager.showRouteMap=flags[1];
        MIDlet1.rmsManager.showWaypointsMap=flags[2];
        MIDlet1.rmsManager.showWaypointsTitleMap=flags[3];
        MIDlet1.rmsManager.showTargetMap=flags[4];
        //
        break;
      case TYPE_SETTINGS_TRACK:
        MIDlet1.rmsManager.setInterval(Integer.parseInt(tf.getString()), false);
        MIDlet1.rmsManager.distanceTrack=Integer.parseInt(tf2.getString());
        MIDlet1.rmsManager.breakTrackDelay=Integer.parseInt(tf3.getString());
        //
        MIDlet1.rmsManager.showTrackTrack=flags[0];
        MIDlet1.rmsManager.showRouteTrack=flags[1];
        MIDlet1.rmsManager.showWaypointsTrack=flags[2];
        MIDlet1.rmsManager.showWaypointsTitleTrack=flags[3];
        MIDlet1.rmsManager.showTargetTrack=flags[4];
        //
        break;
      case TYPE_SETTINGS_ROUTE:
        MIDlet1.rmsManager.pointMarginSound=Double.parseDouble(tf3.getString());
        MIDlet1.rmsManager.pointMarginNext=Double.parseDouble(tf4.getString());        
        //
        break;
      case TYPE_SETTINGS_HARDWARE:
        cg.getSelectedFlags(flags);
        MIDlet1.rmsManager.fastConnect=flags[0];
        MIDlet1.rmsManager.useAuthenticate=flags[1];
        //
//#if NokiaGUI
//#         MIDlet1.rmsManager.backlightLevel=g.getValue()*10;
//#endif
        //
        break;
      }
      //
      MIDlet1.rmsManager.smartSaveRMS(RmsManager.RMS_SETTINGS);
    }
    MIDlet1.screenManager.popScreen();
  }
}
