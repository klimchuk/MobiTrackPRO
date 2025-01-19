package henson.midp.View;

import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.util.*;
import java.io.*;
import javax.wireless.messaging.*;
import javax.microedition.lcdui.game.Sprite;

import javax.microedition.io.file.*;

import henson.midp.*;
import henson.midp.Model.*;
import henson.midp.Sms.*;

//#if NokiaGUI
//# import com.nokia.mid.ui.*;
//#endif

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class SplashScreen extends Canvas implements CommandListener, IUpdate, Runnable
{
  public static final byte TYPE_SPLASH=1;
  public static final byte TYPE_MESSAGE=2;
  // Спутники
  public static final byte TYPE_INFO1=3;
  // Навигация
  public static final byte TYPE_INFO2=4;
  // Карта
  public static final byte TYPE_INFO3=5;
  // NetGPS
  public static final byte TYPE_INFO4=6;
  // Курс/скорость
  public static final byte TYPE_INFO5=7;
  // Трек
  public static final byte TYPE_INFO6=8;
  // Карта
  public static final byte TYPE_INFO7=9;
  // Путевой компьютер
  public static final byte TYPE_INFO8=10;
  
  public byte type;

  int textColor=0;
  String message="";
  int backColor=0x00ffffff;
  //
  static Image imageBackBuffer=null;
  Image image=null;
  Image imageNoData=null;
  Image imageSPS=null;
  Image imageDGPS=null;
  Image imagePPS=null;
  Image imageWAAS=null;
  Image image2D=null;
  Image image3D=null;
  Image imageArrow=null;
  Image imagePanArrow=null;
  Image imageKeylock=null;

  private Timer timer = null;
  boolean colorBlack=false;
  boolean keyLocked=false;
  boolean outOfMap=false;

  TextBox tb=null;
  InfoForm spotForm=null;

  // Режим отображения  
  int infoMode=0;
  int waypointMode=0;
  int scaleMode=0;
  int tripMode=0;

  // Временные координаты после поворота карты
  double tmpX;
  double tmpY;

  // Привязка трека к текущим координатам
  public static boolean snapTrack=true;
  // Позиция центра экрана при скроллинге
  public static int xoffsetTrack=0;
  public static int yoffsetTrack=0;
  // Привязка карты к текущим координатам
  public static boolean snapMap=true;
  // Позиция центра экрана при скроллинге
  public static int xoffsetMap=0;
  public static int yoffsetMap=0;
  
  //public static final String helpMessage="Controls:\n<1>-Satellite page\n<2>-Navigation page\n<3>-Map page\n<4>-Network page\n<5>-Compass page\n<6>-Track summary page\n<7>-Track map page\n<8>-Trip computer\n<0>-SpotMessage\n<*>-Mark waypoint\n<#>-Observables\n<Left/Right>-View mode on selected page";
  public static final String helpMessage="Keys:\n<1>-Satellites\n<2>-Navigation\n<3>-Map\n<4>-Information\n<5>-Compass\n<6>-Elevation profile\n<7>-Track\n<8>-Trip computer\n<9>-Screenshot\n<0>-Scale/Pan for map and track\n<*>-Mark waypoint\n<#>-Fast codes\n<Left/Right>-View mode on selected page";
  
  private boolean isVisible=false;
  
  // DEBUG
  /*
  String mess1 = "";
  String mess2 = "";
  String mess3 = "";
   */
  long lastFlash = 0L;
  
  double tmpLatitude;
  double tmpLongitude;
  
  public SplashScreen(byte type)
  {
    this.type = type;
    this.message = message;
    //
    switch (type)
    {
      case TYPE_SPLASH:
        image = Util.makeImage("/Splash.png");
        break;
      case TYPE_INFO1:
      case TYPE_INFO2:
      case TYPE_INFO3:
      case TYPE_INFO4:
      case TYPE_INFO5:
      case TYPE_INFO6:
      case TYPE_INFO7:
      case TYPE_INFO8:
        imageNoData = Util.makeImage("/NoData.png");
        imageSPS = Util.makeImage("/SPS.png");
        imageDGPS = Util.makeImage("/DGPS.png");
        imagePPS = Util.makeImage("/PPS.png");
        imageWAAS = Util.makeImage("/WAAS.png");
        image2D = Util.makeImage("/2D.png");
        image3D = Util.makeImage("/3D.png");
        imageArrow = Util.makeImage("/Arrow.png");
        imageKeylock = Util.makeImage("/Keylock.png");
        imagePanArrow = Util.makeImage("/PanArrow.png");
        //this.addCommand(CommandManager.markPointCommand);
        this.addCommand(CommandManager.selectPointCommand);
        this.addCommand(CommandManager.routeCommand);
        this.addCommand(CommandManager.lockCommand);        
        this.addCommand(CommandManager.loadCommand);
        this.addCommand(CommandManager.saveCommand);        
        this.addCommand(CommandManager.settingsCommand);
        this.addCommand(CommandManager.clearCommand);
        this.addCommand(CommandManager.sendTrackCommand);
        this.addCommand(CommandManager.selectMapCommand);
        this.addCommand(CommandManager.helpCommand);
        this.addCommand(CommandManager.hideCommand);
        this.addCommand(CommandManager.disconnectCommand);
        this.setCommandListener(this);
        break;
    }
    //
    this.setFullScreenMode(true);
  }

  public SplashScreen(int textColor, String message, int backColor, Image image, boolean cancel)
  {
    this.type=TYPE_MESSAGE;
    this.textColor = textColor;
    this.message = message;
    this.backColor = backColor;
    this.image = image;
    //
    if (cancel)
      this.addCommand(CommandManager.cancelCommand);
  }

  private void showSpotMessages()
  {
    Vector v=new Vector();
    StringBuffer sb=new StringBuffer();
    v.addElement(new String("SpotMessage"));
    for(int i=0; i<SpotMessage.spotmessages.size(); i++)
    {
      SpotMessage sm=(SpotMessage)SpotMessage.spotmessages.elementAt(i);
      sb.setLength(0);
      sb.append(sm.Date);
      sb.append(" ");
      sb.append(sm.Name);
      sb.append(" ");
      sb.append(sm.KeyID);
      //
      if(MIDlet1.locationManager.getValid())
      {
        sb.append(" ");
        double d=Util.distance(sm.Latitude, sm.Longitude, MIDlet1.locationManager.getLatitude(), MIDlet1.locationManager.getLongitude());
        double coeff=MIDlet1.rmsManager.getUnitCoeff(false);
        sb.append(Math.ceil(d*coeff*10.0)/10.0);
        sb.append(MIDlet1.rmsManager.getDistanceName(true));
      }
      v.addElement(new String(sb.toString()));
      v.addElement(new String(sm.Message));
    }
    spotForm=new InfoForm(v, false);
    // Удаляем команду Close
    spotForm.removeCommand(CommandManager.closeCommand);
    // Заменяем ее на Back
    spotForm.addCommand(CommandManager.backCommand);
    //
    spotForm.addCommand(CommandManager.receiveCommand);
    spotForm.addCommand(CommandManager.sendCommand);
    spotForm.setCommandListener(this);
    MIDlet1.screenManager.pushScreen(spotForm);
  }
  
  private void gameActions(int gameCode)
  {
      switch(gameCode) 
      {
          case Canvas.UP: 
              if(type==TYPE_INFO3 && !snapMap) 
                  yoffsetMap-=getWidth()/4;
              else
              if(type==TYPE_INFO7 && !snapTrack) 
                  yoffsetTrack-=getWidth()/4;
              else
              {
                  type--;
                  if(type<TYPE_INFO1)
                    type=TYPE_INFO8;
              }
              break;
          case Canvas.DOWN: 
              if(type==TYPE_INFO3 && !snapMap) 
                  yoffsetMap+=getWidth()/4;
              else
              if(type==TYPE_INFO7 && !snapTrack) 
                  yoffsetTrack+=getWidth()/4;
              else
              {
                  type++; 
                  if(type>TYPE_INFO8)
                    type=TYPE_INFO1;
              }
              break;
          case Canvas.LEFT:
              if(type==TYPE_INFO3) 
              {
                  if(snapMap)
                  {
                    MIDlet1.rmsManager.zoomMode+=25;
                    if(MIDlet1.rmsManager.zoomMode>250)
                        MIDlet1.rmsManager.zoomMode=25;
                  }
                  else
                    xoffsetMap-=getWidth()/4;
              }
              else
              if(type==TYPE_INFO4) 
              {
                  infoMode--;
                  if(infoMode<0)
                    infoMode=1;
              }
              else
              if(type==TYPE_INFO5) 
              {
                  waypointMode--;
                  if(waypointMode<0)
                  {
                      if(MIDlet1.rmsManager.routePointIndex!=-1)
                        waypointMode=3;
                      else
                        waypointMode=2;
                  }
              }
              else
              if(type==TYPE_INFO7)
              {
                  if(snapTrack)
                  {
                    scaleMode--;
                    if(scaleMode<0) scaleMode=MIDlet1.rmsManager.englishScales.size()-1;
                  }
                  else
                    xoffsetTrack-=getWidth()/4;
              }
              else
              if(type==TYPE_INFO8)
              {
                  tripMode--;
                  if(tripMode<0) tripMode=3;
              }
              break;
          case Canvas.RIGHT:
              if(type==TYPE_INFO3) 
              {
                  if(snapMap)
                  {
                      MIDlet1.rmsManager.zoomMode-=25;
                      if(MIDlet1.rmsManager.zoomMode<25)
                          MIDlet1.rmsManager.zoomMode=250;
                  }
                  else
                    xoffsetMap+=getWidth()/4;
              }
              else
              if(type==TYPE_INFO4)
              {
                  infoMode++;
                  if(infoMode>1) 
                    infoMode=0;
              }
              else
              if(type==TYPE_INFO5)
              {
                  waypointMode++;
                  if(MIDlet1.rmsManager.routePointIndex!=-1)
                  {
                    if(waypointMode>3) 
                      waypointMode=0;
                  }
                  else
                  {
                    if(waypointMode>2) 
                      waypointMode=0;                      
                  }
              }
              else
              if(type==TYPE_INFO7)
              {
                  if(snapTrack)
                  {
                      scaleMode++;
                      if(scaleMode>=MIDlet1.rmsManager.englishScales.size())
                          scaleMode=0;
                  }
                  else
                    xoffsetTrack+=getWidth()/4;
              }
              else
              if(type==TYPE_INFO8)
              {
                  tripMode++;
                  if(tripMode>3) tripMode=0;
              }
              break;
      }
  }
  
  private boolean executeOperation(int number)
  {
      switch(number)
      {
          case 1:
              // Send text SMS with position
              tb = new TextBox("Phone number", "", 30, TextField.PHONENUMBER);
              tb.addCommand(CommandManager.okCommand);
              tb.addCommand(CommandManager.cancelCommand);
              tb.setCommandListener(this);
              MIDlet1.screenManager.pushScreen(tb);
              break;
          case 2:
              // Send binary SMS with position
              tb = new TextBox("Phone number", "", 31, TextField.PHONENUMBER);
              tb.addCommand(CommandManager.okCommand);
              tb.addCommand(CommandManager.cancelCommand);
              tb.setCommandListener(this);
              MIDlet1.screenManager.pushScreen(tb);
              break;
          case 10:
              // Pixel maps
              MIDlet1.screenManager.pushScreen(new ListForm("Pixel maps", ListForm.TYPE_FILELIST, "*.map;*.gmi"));
              break;
          case 11:
              // Routes
              MIDlet1.screenManager.pushScreen(new ListForm("Routes", ListForm.TYPE_FILELIST, "*.rte;*.kmr"));
              break;
          case 12:
              // Waypoints
              MIDlet1.screenManager.pushScreen(new ListForm("Waypoints", ListForm.TYPE_FILELIST, "*.wpt;*.kml"));
              break;
          case 13:
              // Map (Google)
              MIDlet1.netManager.sendGoogleMap();
              break;
          case 14:
              // Satellite Map (Google)
              MIDlet1.netManager.sendGoogleSat();
              break;
          case 15:
              // Map (Virtual Earth)
              MIDlet1.netManager.sendVEarthMap();
              break;
          case 16:
              // Satellite Map (Virtual Earth)
              MIDlet1.netManager.sendVEarthSat();
              break;
          case 70:
              MIDlet1.rmsManager.Action(70, "file:///"+MIDlet1.rmsManager.fastsavePath+MIDlet1.rmsManager.getDateTimeName(), false);
              break;
          case 71:
              MIDlet1.rmsManager.Action(71, "file:///"+MIDlet1.rmsManager.fastsavePath+MIDlet1.rmsManager.getDateTimeName(), false);
              break;
          case 72:
              MIDlet1.rmsManager.Action(72, "file:///"+MIDlet1.rmsManager.fastsavePath+MIDlet1.rmsManager.getDateTimeName(), false);
              break;
          case 73:
              MIDlet1.rmsManager.Action(73, "file:///"+MIDlet1.rmsManager.fastsavePath+MIDlet1.rmsManager.getDateTimeName(), false);
              break;
          case 80:
              MIDlet1.rmsManager.Action(80, null, false);
              break;
          case 81:
              MIDlet1.rmsManager.Action(81, null, false);
              break;
          case 82:
              MIDlet1.rmsManager.Action(82, null, false);
              break;
          case 83:
              MIDlet1.rmsManager.Action(83, null, false);
              break;
          case 84:
              MIDlet1.rmsManager.Action(84, null, false);
              break;
          case 85:
              MIDlet1.rmsManager.Action(85, null, false);
              break;
          case 86:
              MIDlet1.rmsManager.Action(86, null, false);
              break;
          case 90:
              // Common settings
              MIDlet1.screenManager.pushScreen(new SettingsForm(SettingsForm.TYPE_SETTINGS, null));
              break;
          case 91:
              // Map settings
              MIDlet1.screenManager.pushScreen(new SettingsForm(SettingsForm.TYPE_SETTINGS_MAP, null));
              break;
          case 92:
              // Track settings
              MIDlet1.screenManager.pushScreen(new SettingsForm(SettingsForm.TYPE_SETTINGS_TRACK, null));
              break;
          case 93:
              // Network settings
              MIDlet1.screenManager.pushScreen(new SettingsForm(SettingsForm.TYPE_NETWORK, null));
              break;
          case 94:
              // Route settings
              MIDlet1.screenManager.pushScreen(new SettingsForm(SettingsForm.TYPE_SETTINGS_ROUTE, null));
              break;              
          case 95:
              // Hardware settings
              MIDlet1.screenManager.pushScreen(new SettingsForm(SettingsForm.TYPE_SETTINGS_HARDWARE, null));
              break;              
          default:
              return false;
      }
      //
      return true;
  }

  protected void keyPressed(int keyCode)
  {
    if(keyLocked)
      return;
    //
    lastFlash = System.currentTimeMillis();
    //
    switch (type)
    {
      case TYPE_SPLASH:
        dismiss();
        break;
      case TYPE_INFO1:
      case TYPE_INFO2:
      case TYPE_INFO3:
      case TYPE_INFO4:
      case TYPE_INFO5:
      case TYPE_INFO6:
      case TYPE_INFO7:
      case TYPE_INFO8:
        if(keyCode==Canvas.KEY_NUM1)
          type=TYPE_INFO1;
        else
        if(keyCode==Canvas.KEY_NUM2)
          type=TYPE_INFO2;
        else
        if(keyCode==Canvas.KEY_NUM3)
        {
          type=TYPE_INFO3;
          if(MIDlet1.rmsManager.autoLoadMap && 
                  MIDlet1.rmsManager.imageMap == null &&
                   !MIDlet1.rmsManager.pleaseWait)
          {
            //mess1="Find map...";
            loadRightMap(MIDlet1.locationManager.getLatitude(), MIDlet1.locationManager.getLongitude());
          }
        }
        else
        if(keyCode==Canvas.KEY_NUM4)
          type=TYPE_INFO4;
        else
        if(keyCode==Canvas.KEY_NUM5)
          type=TYPE_INFO5;
        else
        if(keyCode==Canvas.KEY_NUM6)
          type=TYPE_INFO6;
        else
        if(keyCode==Canvas.KEY_NUM7)
          type=TYPE_INFO7;
        else
        if(keyCode==Canvas.KEY_NUM8)
          type=TYPE_INFO8;
        else
        if(keyCode==Canvas.KEY_NUM9)
        {
          MIDlet1.rmsManager.screenshot.createBitmap(imageBackBuffer);
          // Saving to disk using local time
          MIDlet1.rmsManager.saveFile("file:///"+MIDlet1.rmsManager.fastsavePath+MIDlet1.rmsManager.getDateTimeName()+".bmp", MIDlet1.rmsManager.screenshot.baos.toByteArray(), false);
        }
        else
        if(keyCode==Canvas.KEY_NUM0)
        {
            if(type==TYPE_INFO3)
            {
                SplashScreen.snapMap=!SplashScreen.snapMap;
                SplashScreen.xoffsetMap=SplashScreen.yoffsetMap=0;
            }
            else
            if(type==TYPE_INFO7)
            {
                SplashScreen.snapTrack=!SplashScreen.snapTrack;
                SplashScreen.xoffsetTrack=SplashScreen.yoffsetTrack=0;
            }
        }
        else
        if (keyCode == Canvas.KEY_STAR)
        {
          tb = new TextBox("Mark waypoint", "", 80, TextField.ANY);
          tb.addCommand(CommandManager.okCommand);
          tb.addCommand(CommandManager.cancelCommand);
          tb.setCommandListener(this);
          MIDlet1.screenManager.pushScreen(tb);
          return;
        }
        else
        if (keyCode == Canvas.KEY_POUND)
        {
          // Вводим код операции
          tb = new TextBox("Fast code", "", 3, TextField.NUMERIC);
          tb.addCommand(CommandManager.okCommand);
          tb.addCommand(CommandManager.cancelCommand);
          tb.addCommand(CommandManager.codesCommand);
          tb.setCommandListener(this);
          MIDlet1.screenManager.pushScreen(tb);
          return;
        }
        else
        /*
        if (keyCode == Canvas.KEY_POUND)
        {
          MIDlet1.screenManager.pushScreen(new ListForm("Observables", ListForm.TYPE_OBSERVABLES));
        }
        else
        if (keyCode == Canvas.KEY_NUM0)
        {
          //SpotMessage
          showSpotMessages();
          return;
        }
        else*/
            gameActions(getGameAction(keyCode));
        //
        repaint();
        break;
    }
  }

  private void drawCursor(Graphics g, int x, int y, String name)
  {
    // Cursor
    if (colorBlack)
      g.setColor(0);
    else
      g.setColor(0xff, 0x00, 0x00);
    //
    int len=3;
    if(isNokia208() || isSeries60())
        len=5;
    g.drawLine( x - len, y, x + len, y);
    g.drawLine( x , y - len, x, y + len);
    // Рисуем текст
    if(name!=null && name.length()>0)
    {
      g.setColor(0xa0, 0x0, 0x0);
      g.drawString(name, x, y - 5, Graphics.HCENTER | Graphics.BOTTOM);
    }
  }

  private void drawCompassArrow(Graphics g, double angle, int width, int lineColor, int fillColor)
  {
    int w2=getWidth()/2;
    int h2=getHeight()/2;
    //
    int x1,y1,x2,y2,x,y;
    // Компас
    if(isNokia208()) 
    {
        x = (int) (Math.sin(Math.toRadians(angle)) * 65) + w2;
        y = h2 + 6 - (int) (Math.cos(Math.toRadians(angle)) * 65);
    } else
    if(isSeries60()) 
    {
        x = (int) (Math.sin(Math.toRadians(angle)) * 55) + w2;
        y = h2 + 6 - (int) (Math.cos(Math.toRadians(angle)) * 55);
    }
    else 
    {
        x = (int) (Math.sin(Math.toRadians(angle)) * 40) + w2;
        y = h2 + 6 - (int) (Math.cos(Math.toRadians(angle)) * 40);
    }
    x1 = (int) (Math.sin(Math.toRadians(angle - 90.0)) * width) + w2;
    y1 = h2+6 - (int) (Math.cos(Math.toRadians(angle - 90.0)) * width);
    x2 = (int) (Math.sin(Math.toRadians(angle + 90.0)) * width) + w2;
    y2 = h2+6 - (int) (Math.cos(Math.toRadians(angle + 90.0)) * width);
    //
    g.setColor(fillColor);
    g.fillTriangle(x1, y1, x2, y2, x, y);
  }

  private void drawDirectionArrow(Graphics g, int w2, int h2, double angle, int width)
  {
    int x1,y1,x2,y2,x,y;
    // Окно с треком и карта
    x = (int) (Math.sin(Math.toRadians(angle)) * 15) + w2;
    y = h2 - (int) (Math.cos(Math.toRadians(angle)) * 15);
    x1 = (int) (Math.sin(Math.toRadians(angle - 20.0)) * width) + w2;
    y1 = h2 - (int) (Math.cos(Math.toRadians(angle - 20.0)) * width);
    x2 = (int) (Math.sin(Math.toRadians(angle + 20.0)) * width) + w2;
    y2 = h2 - (int) (Math.cos(Math.toRadians(angle + 20.0)) * width);
    // Cursor
    if (colorBlack)
      g.setColor(0);
    else
      g.setColor(0xff, 0x00, 0x00);
    g.drawLine(w2, h2, x, y);
    g.drawArc(x-2, y-2, 4, 4, 0, 360);
  }

  private void drawTrack(Graphics g, 
                            int basex,
                            int basey,
                            double latitude, 
                            double scalelat, 
                            double longitude, 
                            double scalelon, 
                            double offsetAngle, 
                            Vector points,
                            int color, 
                            boolean thick)
  {
    if(points==null)
      return;
    // Масштаб по умолчанию 1 пиксел = 0.0001 градуса
    // Центр текущее положение
    // Ставим точки трека и рисуем линии
    int x, y, x1, y1;
    x1=y1=0;
    g.setColor(color);
    for (int i = 0; i < points.size(); i++)
    {
      Point p=(Point)points.elementAt(i);
      //
      rotate((p.Longitude-longitude)*scalelon, (p.Latitude-latitude)*scalelat, offsetAngle);
      x=(int)((double)basex+tmpX);
      y=(int)((double)basey-tmpY);
      //
      g.drawArc(x-1, y-1, 2, 2, 0, 360);
      if(i>0)
      {
        Point prev=(Point)points.elementAt(i-1);
        if(MIDlet1.rmsManager.breakTrackDelay==0L ||
           Math.abs(p.checkDifference(prev))<MIDlet1.rmsManager.breakTrackDelay)
           g.drawLine(x, y, x1, y1);
        //
        if(thick)
        {
            g.drawLine(x-1, y,  x1-1, y1);
            g.drawLine(x+1, y,  x1+1, y1);
            g.drawLine(x, y-1,  x1, y1-1);
            g.drawLine(x, y+1,  x1, y1+1);
        }
      }
      x1=x;
      y1=y;
    }
  }

  private void drawWaypoints(Graphics g, 
                            int basex,
                            int basey,
                            double latitude, 
                            double scalelat, 
                            double longitude, 
                            double scalelon, 
                            double offsetAngle,
                            Vector points,
                            int color, 
                            boolean showTitle)
  {
      // Точки пользователя
        for (int i = 0; i < points.size(); i++)
        {
          Point p=(Point)points.elementAt(i);
          //
          rotate((p.Longitude-longitude)*scalelon, (p.Latitude-latitude)*scalelat, offsetAngle);
          int x=(int)((double)basex+tmpX);
          int y=(int)((double)basey-tmpY);
          //
          g.setColor(0xff, 0xff, 0xff);
          g.fillTriangle(x-2, y+3, x+4, y+3, x+1, y-2);
          //
          g.setColor(color);
          g.fillTriangle(x-3, y+2, x+3, y+2, x, y-3);
          // Если надо рисуем картинку
          if(p.Category>0)
            //g.drawRegion(imagePOI, 16*(p.Category-1), 0, 16, 16, Sprite.TRANS_NONE, x, y, Graphics.HCENTER|Graphics.TOP);
              g.drawImage(MIDlet1.rmsManager.categories[p.Category].image, x, y, Graphics.HCENTER|Graphics.TOP);
          // Рисуем текст
          if(showTitle)
          {
            g.setColor(0xff, 0xff, 0xff);
            g.drawString(p.Name, x+1, y-1, Graphics.HCENTER|Graphics.BOTTOM);
            g.setColor(0x0, 0x0, 0xa0);
            g.drawString(p.Name, x, y-2, Graphics.HCENTER|Graphics.BOTTOM);
          }
        }
  }

  private void drawTargetPoint(Graphics g, 
                            int basex,
                            int basey,
                            double latitude, 
                            double scalelat, 
                            double longitude, 
                            double scalelon, 
                            double offsetAngle,
                            Point p)
  {
        g.setColor(0xff, 0, 0);
        //
        rotate((p.Longitude-longitude)*scalelon, (p.Latitude-latitude)*scalelat, offsetAngle);
        int x=(int)((double)basex+tmpX);
        int y=(int)((double)basey-tmpY);
        g.drawLine(x,y,basex,basey);
  }
  
  private boolean isNokia208()
  {
    return (getWidth()>=208 && getHeight()>=208);
  }

  private boolean isSeries60()
  {
    return (getWidth()>=176 && getHeight()>=176);
  }

  private void drawVectorMap(Graphics g, double latitude, double longitude, double scalelat, double scalelon)
  {
    if(MIDlet1.rmsManager.vectorMap==null)
      return;
    //
    int w2=getWidth()/2;
    int h2=getHeight()/2;
    //
    for(int i=0; i<MIDlet1.rmsManager.vectorMap.items.size(); i++)
    {
      VectorMapItem vmi=(VectorMapItem)MIDlet1.rmsManager.vectorMap.items.elementAt(i);
      if(vmi.data==null ||
          vmi.x==null ||
          vmi.y==null)
        continue;
      //
      for(int j=0; j<vmi.x.length; j++)
      {
        vmi.x[j]=(int)((double)w2+(vmi.data[j*2+1]-longitude)*scalelon);
        vmi.y[j]=(int)((double)h2-(vmi.data[j*2]-latitude)*scalelat);
      }
      //
      g.setColor(0);
      switch(vmi.region)
      {
      case VectorMapItem.REGION_POI:
        g.drawLine(vmi.x[0]-2, vmi.y[0], vmi.x[0]+2, vmi.y[0]);
        g.drawLine(vmi.x[0], vmi.y[0]-2, vmi.x[0], vmi.y[0]+2);
        g.drawString(vmi.name, vmi.x[0], vmi.y[0]-2, Graphics.HCENTER|Graphics.BOTTOM);
        break;
      case VectorMapItem.REGION_CITY:
        g.drawArc(vmi.x[0]-2,vmi.y[0]-2,4,4,0,360);
        g.drawString(vmi.name, vmi.x[0]+2, vmi.y[0]-2, Graphics.LEFT|Graphics.BOTTOM);
        break;
      case VectorMapItem.REGION_POLYLINE:
        for(int j=1; j<vmi.x.length; j++)
          g.drawLine(vmi.x[j-1], vmi.y[j-1], vmi.x[j], vmi.y[j]);
        break;
      case VectorMapItem.REGION_POLYGON:
        int color=0xFFFFFF;
        if((vmi.type>0x00 && vmi.type<0x04) || vmi.type==0x0e)
            color=0xa0a0a0;
        else
        if((vmi.type>0x03 && vmi.type<0x08) || vmi.type==0x0c || vmi.type==0x13 || vmi.type==0x19)
            color=0x808080;
        else
        if(vmi.type>0x07 && vmi.type<0x0c)
            color=0xFF0000;
        else
        if(vmi.type>0x07 && vmi.type<0x0c)
            color=0xFF0000;
        else
        if((vmi.type>0x13 && vmi.type<0x17) || (vmi.type>0x1d && vmi.type<0x21) || vmi.type==0x0d || vmi.type==0x50)
            color=0x80FF80;
        else
        if(vmi.type==0x1a || vmi.type==0x4e || vmi.type==0x4f || vmi.type==0x52)
            color=0x20FF20;
        else
        if(vmi.type>0x27 && vmi.type<0x4a)
            color=0x0000A0;
        //    
        g.setColor(color);
        for(int j=0; j<vmi.x.length; j+=3)
        {
          //g.drawLine(vmi.x[j], vmi.y[j], vmi.x[j+1], vmi.y[j+1]);
          //g.drawLine(vmi.x[j+2], vmi.y[j+2], vmi.x[j+1], vmi.y[j+1]);
          //g.drawLine(vmi.x[j], vmi.y[j], vmi.x[j+2], vmi.y[j+2]);
          g.fillTriangle(vmi.x[j], vmi.y[j], vmi.x[j + 1], vmi.y[j + 1],
                         vmi.x[j+2], vmi.y[j+2]);
        }
        break;
      }
    }
  }

  protected void paint(Graphics gdst)
  {
    if(imageBackBuffer==null)
        imageBackBuffer=Image.createImage(getWidth(), getHeight());
    //
    Graphics g=imageBackBuffer.getGraphics();
    Font flarge = Font.getFont(Font.FACE_PROPORTIONAL, (isSeries60()||isNokia208()?Font.STYLE_BOLD:Font.STYLE_PLAIN),
                          Font.SIZE_LARGE);
    Font fsmall = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN,
                          Font.SIZE_SMALL);
    Font fmedium = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD,
                         Font.SIZE_MEDIUM);
    // По умолчанию большой шрифт
    Font baseFont=flarge;
    if(flarge.getHeight()>18 && !isNokia208())
    {
        if(fmedium.getHeight()>18)
            baseFont=fsmall; 
        else
            baseFont=fmedium; 
    }
    //
    g.setFont(baseFont);
    int yFont=baseFont.getHeight();
    //
    g.setColor(218, 244, 244);
    g.fillRect(0, 0, getWidth(), getHeight());
    //
    int w2=getWidth()/2;
    int h2=getHeight()/2;
    //
    switch (type)
    {
      case TYPE_SPLASH:
        // На любое разрешение
        if(image!=null)
        {
          if(getHeight()>image.getHeight())
            g.drawImage(image, getWidth() / 2, (getHeight()-image.getHeight())/2, Graphics.HCENTER | Graphics.TOP);
          else
            g.drawImage(image, getWidth() / 2, 0, Graphics.HCENTER | Graphics.TOP);
        }
        else
        {
            g.setColor(0);
            g.drawString("UNTEH MobiTrack",  getWidth()/2, 50, Graphics.HCENTER|Graphics.TOP);
        }
        break;
      case TYPE_MESSAGE:
        //g.setColor(backColor);
        //g.fillRect(0, 0, getWidth(), getHeight());
        //
        if(image!=null)
          g.drawImage(image, getWidth()/2, 0, Graphics.HCENTER|Graphics.TOP);
        //
        g.setColor(textColor);
        Vector v=Util.parseString(message, "|", true);
        if(v!=null)
        {
            for(int i=0; i<v.size(); i++)
                g.drawString((String)v.elementAt(i), 5, 5+i*g.getFont().getHeight()*3/2, Graphics.TOP|Graphics.LEFT);
        }
        break;
      case TYPE_INFO1:
        // Выравниваем по центру
        g.setColor(0);
        if(isNokia208())
        {
            // Axis
            g.drawLine(w2-90, h2, w2+90, h2);
            g.drawLine(w2, h2-90, w2, h2+90);
            // Circles
            g.drawArc(w2-90,h2-90,180,180,0,360);
            g.drawArc(w2-45,h2-45,90,90,0,360);
        }
        else
        if(isSeries60())
        {
            // Axis
            g.drawLine(w2-72, h2, w2+72, h2);
            g.drawLine(w2, h2-72, w2, h2+72);
            // Circles
            g.drawArc(w2-72,h2-72,144,144,0,360);
            g.drawArc(w2-36,h2-36,72,72,0,360);
        }
        else
        {
            // Axis
            g.drawLine(w2-60, h2, w2+60, h2);
            g.drawLine(w2, h2-60, w2, h2+60);
            // Circles
            g.drawArc(w2-60,h2-60,120,120,0,360);
            g.drawArc(w2-30,h2-30,60,60,0,360);
            // Clear place for symbols W E N S
            g.setColor(218, 244, 244);
            g.fillRect(w2-64,h2-4,8,8);
            g.fillRect(w2+56,h2-4,8,8);
            g.fillRect(w2-4,h2-64,8,8);
            g.fillRect(w2-4,h2+56,8,8);
        }
        //
        g.setFont(fmedium);
        g.setColor(0);
        if(isNokia208())
        {
            g.drawString("W", w2-104, h2-fmedium.getHeight()/2, Graphics.LEFT|Graphics.TOP);
            g.drawString("E", w2+104, h2-fmedium.getHeight()/2, Graphics.RIGHT|Graphics.TOP);
            g.drawString("N", w2, h2-104, Graphics.HCENTER|Graphics.TOP);
            g.drawString("S", w2, h2+104, Graphics.HCENTER|Graphics.BOTTOM);
        }
        else
        if(isSeries60())
        {
            g.drawString("W", w2-75, h2-fmedium.getHeight()/2, Graphics.RIGHT|Graphics.TOP);
            g.drawString("E", w2+75, h2-fmedium.getHeight()/2, Graphics.LEFT|Graphics.TOP);
            g.drawString("N", w2, h2-75, Graphics.HCENTER|Graphics.BOTTOM);
            g.drawString("S", w2, h2+75, Graphics.HCENTER|Graphics.TOP);
        }
        else
        {
            g.drawString("W", w2-64, h2-fmedium.getHeight()/2, Graphics.LEFT|Graphics.TOP);
            g.drawString("E", w2+60, h2-fmedium.getHeight()/2, Graphics.RIGHT|Graphics.TOP);
            g.drawString("N", w2, h2-64, Graphics.HCENTER|Graphics.TOP);
            g.drawString("S", w2, h2+64, Graphics.HCENTER|Graphics.BOTTOM);            
        }
        //
        boolean modeWAAS=false;
        g.setFont(fsmall);
        Vector sats=MIDlet1.locationManager.getSattelites();
        if(sats!=null)
        {
            for(int i=0; i<sats.size(); i++)
            {
              Sattelite sat=(Sattelite)sats.elementAt(i);
              if(sat!=null)
              {
                int r;
                if(isNokia208())
                    r=90-sat.height;
                else
                if(isSeries60())
                    r=72-sat.height*8/10;
                else
                    r=60-sat.height*2/3;
                //
                int xcenter=(int)(Math.sin(Math.toRadians(sat.azimuth))*r+w2);
                int ycenter=(int)(h2-Math.cos(Math.toRadians(sat.azimuth))*r);
                // Внутренний круг
                if(sat.power<=0 || sat.power>100)
                  g.setColor(0xff,0xff,0xff);
                else
                  g.setColor(198-sat.power*2, 57+sat.power*2, 0);
                //
                if(isNokia208())
                    g.fillArc(xcenter-12,ycenter-12,25,25,0,360);
                else
                if(isSeries60())
                    g.fillArc(xcenter-10,ycenter-10,20,20,0,360);
                else
                    g.fillArc(xcenter-7,ycenter-7,15,15,0,360);
                // Внешняя рамка
                if(sat.active &&
                   sat.power>0 &&
                   sat.power<=100)
                {
                  g.setColor(0xff, 0xff, 0);
                  if(sat.number>32)
                    modeWAAS=true;
                }
                else
                  g.setColor(0);
                //
                if(isNokia208())
                    g.drawArc(xcenter-12,ycenter-12,24,24,0,360);
                else
                if(isSeries60())
                    g.drawArc(xcenter-10,ycenter-10,20,20,0,360);
                else
                    g.drawArc(xcenter-7,ycenter-7,14,14,0,360);
                //
                g.drawString(""+sat.number, xcenter+1, ycenter-fsmall.getHeight()/2+1, Graphics.TOP|Graphics.HCENTER);
              }
            }
        }
        // тип связи
        if(MIDlet1.locationManager.getValid())
        {
          int bottomLine=(isSeries60()||isNokia208()?getHeight()-1:h2+63);
          //
          if(modeWAAS)
              g.drawImage(imageWAAS, getWidth()-1, bottomLine, Graphics.BOTTOM | Graphics.RIGHT);
          else
          {
              switch (MIDlet1.locationManager.getPositionFix())
              {
                case 1:
                  g.drawImage(imageSPS, getWidth()-1, bottomLine, Graphics.BOTTOM | Graphics.RIGHT);
                  break;
                case 2:
                  g.drawImage(imageDGPS, getWidth()-1, bottomLine, Graphics.BOTTOM | Graphics.RIGHT);
                  break;
                case 3:
                  g.drawImage(imagePPS, getWidth()-1, bottomLine, Graphics.BOTTOM | Graphics.RIGHT);
                  break;
              }
          }
          //
          switch (MIDlet1.locationManager.getSatelliteMode())
          {
            case 2:
              g.drawImage(image2D, 1, bottomLine, Graphics.BOTTOM | Graphics.LEFT);
              break;
            case 3:
              g.drawImage(image3D, 1, bottomLine, Graphics.BOTTOM | Graphics.LEFT);
              break;
          }
        }
        //
        break;
      case TYPE_INFO2:
        // Выравниваем по центру
        if(isNokia208() || isSeries60())
        {
            // Для любого разрешения, выравнивание по верхней границе
            g.setColor(0xff, 0xff, 0xff);
            g.fillRect(0, 0, getWidth(), yFont);
            g.setColor(0xFF, 0xA0, 0x00);
            g.fillRect(0, yFont, getWidth(), 5);
            g.setColor(178, 204, 204);
            g.fillRect(0, yFont+5, getWidth(), yFont+4);
            g.fillRect(0, 3*yFont+13, getWidth(), yFont+4);
            g.fillRect(0, 5*yFont+21, getWidth(), yFont+4);
            g.setColor(0);
            g.drawString(".: Navigation",getWidth()/2,1,Graphics.TOP|Graphics.HCENTER);
            //
            int textcolor=0;
            if(!MIDlet1.locationManager.getValid())
                textcolor=0x808080;
            g.setColor(textcolor);
            {
              g.drawString("Lat: " + MIDlet1.locationManager.getLatitudeString(), getWidth() / 2,
                           yFont+7, Graphics.TOP | Graphics.HCENTER);
              g.drawString("Lon: " + MIDlet1.locationManager.getLongitudeString(), getWidth() / 2,
                           2*yFont+11, Graphics.TOP | Graphics.HCENTER);
              //
              if(MIDlet1.locationManager.getSatelliteMode()!=3)
                // Не 3D
                g.setColor(0xff, 0, 0);
              g.drawString("Alt: "+MIDlet1.locationManager.getAltitude(false)+" "+MIDlet1.rmsManager.getDistanceName(false), getWidth()/2, 3*yFont+15, Graphics.TOP | Graphics.HCENTER);
              g.setColor(textcolor);
              // Выводим локальное время
              g.drawString(MIDlet1.locationManager.getTime(true), getWidth()/2, 4*yFont+19, Graphics.TOP | Graphics.HCENTER);
              if(MIDlet1.locationManager.getPrecesionHDOP()>6.0)
                g.setColor(0xff, 0, 0);
              else
              if(MIDlet1.locationManager.getPrecesionHDOP()>4.0)
                g.setColor(0xC0, 0xC0, 0);
              else
                g.setColor(0, 0xA0, 0);
              g.drawString(""+MIDlet1.locationManager.getPrecesionHDOP(), w2*3/2, 5*yFont+23, Graphics.TOP | Graphics.HCENTER);
              g.setColor(textcolor);
              // Выводим локальную дату
              g.drawString(MIDlet1.locationManager.getDate(true), w2/2, 5*yFont+23, Graphics.TOP | Graphics.HCENTER);
            }
            /*
            else
            {
              g.drawString("Searching...", getWidth()/2, 2*yFont+11, Graphics.TOP | Graphics.HCENTER);
              g.drawString(MIDlet1.gpsManager.visible, getWidth()/2, 4*yFont+19, Graphics.TOP | Graphics.HCENTER);
            }*/
        }
        else
        {
            int yoffset=0;
            if(image!=null)
            {
              if(getHeight()>image.getHeight())
                yoffset=(getHeight()-image.getHeight())/2;
              g.drawImage(image, getWidth() / 2, yoffset, Graphics.HCENTER | Graphics.TOP);
            }
            //
            g.setColor(0xFF, 0xA0, 0x00);
            g.fillRect(w2-59, 15+yoffset, 118, 3);
            //
            int textcolor=0;
            if(!MIDlet1.locationManager.getValid())
                textcolor=0x808080;
            g.setColor(textcolor);
            {
              if(isSeries60())
              {
                g.drawString(MIDlet1.locationManager.getLatitudeString(), getWidth() / 2,
                             20 + yoffset, Graphics.TOP | Graphics.HCENTER);
                g.drawString(MIDlet1.locationManager.getLongitudeString(), getWidth() / 2,
                             40 + yoffset, Graphics.TOP | Graphics.HCENTER);
              }
              else
              {
                g.drawString("Lat: " + MIDlet1.locationManager.getLatitudeString(), getWidth() / 2,
                             20 + yoffset, Graphics.TOP | Graphics.HCENTER);
                g.drawString("Lon: " + MIDlet1.locationManager.getLongitudeString(), getWidth() / 2,
                             40 + yoffset, Graphics.TOP | Graphics.HCENTER);
              }
              //
              if(MIDlet1.locationManager.getSatelliteMode()!=3)
                // Не 3D
                g.setColor(0xff, 0, 0);
              g.drawString("Alt: "+MIDlet1.locationManager.getAltitude(false)+" "+MIDlet1.rmsManager.getDistanceName(false), getWidth()/2, 60+yoffset, Graphics.TOP | Graphics.HCENTER);
              g.setColor(textcolor);
              // Выводим локальное время            
              g.drawString(MIDlet1.locationManager.getTime(true), getWidth()/2, 80+yoffset, Graphics.TOP | Graphics.HCENTER);
              if(MIDlet1.locationManager.getPrecesionHDOP()>6.0)
                g.setColor(0xff, 0, 0);
              else
              if(MIDlet1.locationManager.getPrecesionHDOP()>4.0)
                g.setColor(0xC0, 0xC0, 0);
              else
                g.setColor(0, 0xA0, 0);
              g.drawString(""+MIDlet1.locationManager.getPrecesionHDOP(), w2+32, 105+yoffset, Graphics.TOP | Graphics.HCENTER);
              g.setColor(textcolor);
              // Выводим локальную дату
              g.drawString(MIDlet1.locationManager.getDate(true), w2-32, 105+yoffset, Graphics.TOP | Graphics.HCENTER);
            }
            /*
            else
            {
              g.drawString("Searching...", getWidth()/2, 50+yoffset, Graphics.TOP | Graphics.HCENTER);
              g.drawString(MIDlet1.gpsManager.visible, getWidth()/2, 70+yoffset, Graphics.TOP | Graphics.HCENTER);
            }*/
        }        
        //
        //g.drawString("...", getWidth()-1, getHeight()-1, Graphics.BOTTOM | Graphics.RIGHT);
        //
        break;
      case TYPE_INFO3:
        // для любого разрешения
        int xCenter=getWidth()/2;
        int yCenter = getHeight() / 2;
        //
        if (MIDlet1.rmsManager.imageMap != null)
        {
          //MIDlet1.locationManager.setDefaultLatitude(MIDlet1.rmsManager.mapRect.startLatitude);
          //MIDlet1.locationManager.setDefaultLongitude(MIDlet1.rmsManager.mapRect.startLongitude);
          //
          double lat=MIDlet1.locationManager.getLatitude();
          double lon=MIDlet1.locationManager.getLongitude();
          //
          int x = (int) ( (lon - MIDlet1.rmsManager.mapRect.startLongitude) * MIDlet1.rmsManager.imageMap.getWidth() / MIDlet1.rmsManager.mapRect.widthLongitude);
          int bx = -x + xCenter;
          if (bx > 0) bx = 0;
          if (bx < getWidth() - MIDlet1.rmsManager.imageMap.getWidth()) bx = getWidth() -
              MIDlet1.rmsManager.imageMap.getWidth();
          //
          int y = MIDlet1.rmsManager.imageMap.getHeight() -
              (int) ( (lat - MIDlet1.rmsManager.mapRect.startLatitude) * MIDlet1.rmsManager.imageMap.getHeight() / MIDlet1.rmsManager.mapRect.heightLatitude);
          int by = -y + yCenter;
          if (by > 0) by = 0;
          if (by < getHeight() - MIDlet1.rmsManager.imageMap.getHeight()) by = getHeight() -
              MIDlet1.rmsManager.imageMap.getHeight();
          //
          g.drawImage(MIDlet1.rmsManager.imageMap, bx-xoffsetMap, by-yoffsetMap, Graphics.LEFT | Graphics.TOP);
          //
          double dx=(double)MIDlet1.rmsManager.imageMap.getWidth()/(double)MIDlet1.rmsManager.mapRect.widthLongitude;
          double dy=(double)MIDlet1.rmsManager.imageMap.getHeight()/(double)MIDlet1.rmsManager.mapRect.heightLatitude;
          // Рисуем маршрут поверх карты
          if(MIDlet1.rmsManager.route!=null && MIDlet1.rmsManager.showRouteMap)
              drawTrack(g, 
                            bx-xoffsetMap, by-yoffsetMap,
                            MIDlet1.rmsManager.mapRect.startLatitude+MIDlet1.rmsManager.mapRect.heightLatitude, dy, 
                            MIDlet1.rmsManager.mapRect.startLongitude, dx, 
                            0, MIDlet1.rmsManager.route, 0x00ff00, true);
          // Рисуем трек поверх карты
          if(MIDlet1.rmsManager.currentTrack!=null && MIDlet1.rmsManager.showTrackMap)
              drawTrack(g, 
                            bx-xoffsetMap, by-yoffsetMap,
                            MIDlet1.rmsManager.mapRect.startLatitude+MIDlet1.rmsManager.mapRect.heightLatitude, dy, 
                            MIDlet1.rmsManager.mapRect.startLongitude, dx, 
                            0, MIDlet1.rmsManager.currentTrack, 0x0000ff, false);
          // Рисуем направление до выбранной точки
          Point point=MIDlet1.rmsManager.getRoutePoint();
          if(MIDlet1.rmsManager.currentPoint!=null)
            point=MIDlet1.rmsManager.currentPoint;          
          if(point!=null && MIDlet1.rmsManager.showTargetMap)
          {
                drawTargetPoint(g, 
                                x+bx-xoffsetMap, y+by-yoffsetMap, 
                                lat, dy, 
                                lon, dx, 
                                0, point);
          }
          // Рисуем waypoints
          if(MIDlet1.rmsManager.showWaypointsMap)
          {
              g.setFont(fsmall);
              drawWaypoints(g, 
                        bx-xoffsetMap, by-yoffsetMap, 
                        MIDlet1.rmsManager.mapRect.startLatitude+MIDlet1.rmsManager.mapRect.heightLatitude, dy, 
                        MIDlet1.rmsManager.mapRect.startLongitude, dx, 
                        0,
                        MIDlet1.rmsManager.points,
                        0x00ff00,
                        MIDlet1.rmsManager.showWaypointsTitleMap);
              if(MIDlet1.rmsManager.route!=null && MIDlet1.rmsManager.showRouteMap)
              {
                drawWaypoints(g, 
                        bx-xoffsetMap, by-yoffsetMap, 
                        MIDlet1.rmsManager.mapRect.startLatitude+MIDlet1.rmsManager.mapRect.heightLatitude, dy, 
                        MIDlet1.rmsManager.mapRect.startLongitude, dx, 
                        0,
                        MIDlet1.rmsManager.route,
                        0xffff00,
                        MIDlet1.rmsManager.showWaypointsTitleMap);                  
              }
          }
          //
          MIDlet1.rmsManager.makeZoomImage(MIDlet1.rmsManager.zoomMode);
          if(MIDlet1.rmsManager.imageZoom!=null)
            g.drawImage(MIDlet1.rmsManager.imageZoom, x+bx-MIDlet1.rmsManager.leftZoom-xoffsetMap, y+by-MIDlet1.rmsManager.topZoom-yoffsetMap, Graphics.LEFT | Graphics.TOP);
          // Если попадаем в карту, то рисуем курсор
          if(x>=0 && x<MIDlet1.rmsManager.imageMap.getWidth() &&
                  y>=0 && y<MIDlet1.rmsManager.imageMap.getHeight())
          {
            // Рисуем вектор скорости
            if(MIDlet1.locationManager.getSpeed(false)>0.0)
              drawDirectionArrow(g, x+bx-xoffsetMap, y+by-yoffsetMap, MIDlet1.locationManager.getCourse(), 13);
            drawCursor(g, x+bx-xoffsetMap, y+by-yoffsetMap, "");
            //g.drawString("X:"+bx+";"+x+" Y:"+by+";"+y,0,0, Graphics.TOP|Graphics.LEFT);
            outOfMap=false;
          }
          else
          {
            g.setColor(0xff0000);
            g.drawRect(1,1, getWidth()-3, getHeight()-3);
            outOfMap=true;
            //
            if(MIDlet1.rmsManager.autoLoadMap && !MIDlet1.rmsManager.pleaseWait)
            {
                //mess1="Find map...";
                loadRightMap(MIDlet1.locationManager.getLatitude(), MIDlet1.locationManager.getLongitude());
            }                
          }
        }
        else
        {
          g.setColor(0x80, 0, 0);
          String message="Select map";
          if(MIDlet1.rmsManager.pleaseWait)
              message="Please wait...";
          //
          g.drawString(message, getWidth() / 2, getHeight() / 2,
                       Graphics.HCENTER | Graphics.BOTTOM);
        }
        //
        if(!snapMap)
            g.drawImage(imagePanArrow, getWidth()/2, getHeight(), Graphics.HCENTER|Graphics.BOTTOM);
        g.drawImage(imageArrow, 1, 1, Graphics.LEFT|Graphics.TOP);
        g.setFont(fmedium);
        g.setColor(0xffffff);
        g.drawString(MIDlet1.rmsManager.zoomMode+"%", getWidth(), getHeight(),
                     Graphics.RIGHT | Graphics.BOTTOM);
        g.setColor(0);
        g.drawString(MIDlet1.rmsManager.zoomMode+"%", getWidth()-1, getHeight()-1,
                     Graphics.RIGHT | Graphics.BOTTOM);
        //
        /*
        g.setColor(0xff0000);
        g.drawString(mess1, 0, 0, Graphics.LEFT|Graphics.TOP);
        g.drawString(mess2, 0, 30, Graphics.LEFT|Graphics.TOP);
        g.drawString(mess3, 0, 60, Graphics.LEFT|Graphics.TOP);
         */
        //
        break;
      case TYPE_INFO4:
        // Для любого разрешения, выравнивание по верхней границе        
        g.setColor(0xff, 0xff, 0xff);
        g.fillRect(0, 0, getWidth(), yFont);
        g.setColor(0xFF, 0xA0, 0x00);
        g.fillRect(0, yFont, getWidth(), 5);
        g.setColor(178, 204, 204);
        g.fillRect(0, yFont+5, getWidth(), yFont+4);
        g.fillRect(0, 3*yFont+13, getWidth(), yFont+4);
        g.fillRect(0, 5*yFont+21, getWidth(), yFont+4);
        g.setColor(0);
        //
        g.drawString("Information",getWidth()/2,1,Graphics.TOP|Graphics.HCENTER);
        switch(infoMode)
        {
            case 0:
                g.drawString("Track>>",1,yFont+7,Graphics.TOP|Graphics.LEFT);
                if(MIDlet1.rmsManager.currentTrack!=null)
                {
                  g.drawString(MIDlet1.rmsManager.currentTrack.name,1,2*yFont+11,Graphics.TOP|Graphics.LEFT);
                  g.drawString("Length: "+MIDlet1.rmsManager.currentTrack.getLength()+" "+MIDlet1.rmsManager.getDistanceName(true),1,3*yFont+15,Graphics.TOP|Graphics.LEFT);
                  //
                  if(MIDlet1.rmsManager.isFullTrack())
                    g.setColor(0xff, 0, 0);
                  g.drawString("Points: "+MIDlet1.rmsManager.currentTrack.size(),1,4*yFont+19,Graphics.TOP|Graphics.LEFT);
                }                
                break;
            case 1:
                g.drawString("Network>>",1,yFont+7,Graphics.TOP|Graphics.LEFT);
                g.drawString("Last code: "+MIDlet1.netManager.lastCode,1,2*yFont+11,Graphics.TOP|Graphics.LEFT);
                g.drawString("Success: "+MIDlet1.netManager.success,1,3*yFont+15,Graphics.TOP|Graphics.LEFT);
                g.drawString("Failed: "+MIDlet1.netManager.failed,1,4*yFont+19,Graphics.TOP|Graphics.LEFT);
                g.drawString(MIDlet1.netManager.lastMessage,1,5*yFont+23,Graphics.TOP|Graphics.LEFT);
                break;
        }
        g.drawImage(imageArrow, 1, 1, Graphics.LEFT|Graphics.TOP);
        //
        break;
      case TYPE_INFO5:
        // Для любого разрешения, выравнивание по центру
        g.setColor(0);
        double course=MIDlet1.locationManager.getCourse();
        double offsetAngle=0.0;
        if(MIDlet1.rmsManager.autoMapRotation)
          offsetAngle=course;
        //
        String courseString="" + course;
        int widthString=g.getFont().stringWidth(courseString);
        //
        if(isNokia208())
        {
          g.drawString("" + course, getWidth() / 2, h2-80-yFont,
                       Graphics.HCENTER | Graphics.TOP);
          g.setColor(58, 84, 84);
          g.fillArc(w2 - 85, h2 - 79, 170, 170, 0, 360);
          g.setColor(0xFF, 0xA0, 0x00);
          g.drawArc(w2 - 85, h2 - 79, 170, 170, 0, 360);
          g.drawArc(w2 - 65, h2 - 59, 130, 130, 0, 360);
        }
        else
        if(isSeries60())
        {
          g.drawString("" + course, getWidth() / 2, h2 - 84,
                       Graphics.HCENTER | Graphics.TOP);
          g.setColor(58, 84, 84);
          g.fillArc(w2 - 70, h2 - 64, 140, 140, 0, 360);
          g.setColor(0xFF, 0xA0, 0x00);
          g.drawArc(w2 - 70, h2 - 64, 140, 140, 0, 360);
          g.drawArc(w2 - 55, h2 - 49, 110, 110, 0, 360);
        }
        else
        {
          g.drawString("" + course, getWidth() / 2, h2 - 64,
                       Graphics.HCENTER | Graphics.TOP);
          g.setColor(58, 84, 84);
          g.fillArc(w2 - 50, h2 - 44, 100, 100, 0, 360);
          g.setColor(0xFF, 0xA0, 0x00);
          g.drawArc(w2 - 50, h2 - 44, 100, 100, 0, 360);
          g.drawArc(w2 - 40, h2 - 34, 80, 80, 0, 360);
        }
        //
        for(int i=0; i<360; i+=30)
        {
          int r=45;
          if(isNokia208())
              r=75;
          else
          if(isSeries60())
              r=63;
          //
          int x=(int)(Math.sin(Math.toRadians(i+offsetAngle))*r)+w2;
          int y=(int)(Math.cos(Math.toRadians(i+offsetAngle))*r)+h2+6;
          //
          g.setColor(0xff, 0xff, 0);
          switch(i)
          {
            case 0:
              g.drawString("S", x, y-yFont/2, Graphics.HCENTER|Graphics.TOP);
              break;
            case 90:
              g.drawString("E", x, y-yFont/2, Graphics.HCENTER|Graphics.TOP);
              break;
            case 180:
              g.drawString("N", x, y-yFont/2, Graphics.HCENTER|Graphics.TOP);
              break;
            case 270:
              g.drawString("W", x, y-yFont/2, Graphics.HCENTER|Graphics.TOP);
              break;
            default:
              g.setColor(0xff, 0xff, 0xff);
              g.drawLine(x-2, y, x+2, y);
              g.drawLine(x, y-2, x, y+2);
          }
        }
        //
        this.drawCompassArrow(g, course-offsetAngle, 10, 0x00404040, 0x00ffffff);
        //
        Point point=MIDlet1.rmsManager.getRoutePoint();
        if(MIDlet1.rmsManager.currentPoint!=null)
            point=MIDlet1.rmsManager.currentPoint;
        if(point!=null)
        {
          g.setColor(0xff, 0, 0);
          //
          double hdg=Util.getPointHeading(new Point(MIDlet1.locationManager.getLatitude(), MIDlet1.locationManager.getLongitude(), 0.0), point);
          if(hdg!=Double.NaN)
          {
            drawCompassArrow(g, hdg-offsetAngle, 7, 0x00404040, 0x00ff0000);
            //
            g.setFont(fsmall);
            int diff=(int)Math.ceil((hdg-course)*10.0);
            double correction;
            if((diff>=-1800 && diff<0) || diff>1800)
            {                
                if(diff<0)
                    correction=-diff/10.0;
                else
                    correction=(3600-diff)/10.0;
                //
                if(isNokia208())
                  g.drawString(correction+"< ", (getWidth()-widthString)/2, h2-80,
                               Graphics.RIGHT | Graphics.BOTTOM);
                else
                if(isSeries60())
                  g.drawString(correction+"< ", (getWidth()-widthString)/2, h2 - 84 + yFont,
                               Graphics.RIGHT | Graphics.BOTTOM);
                else
                  g.drawString(correction+"< ", (getWidth()-widthString)/2, h2 - 64 + yFont,
                               Graphics.RIGHT | Graphics.BOTTOM);
            }
            else
            {
                if(diff<0)
                    correction=(3600+diff)/10.0;
                else
                    correction=diff/10.0;
                if(isNokia208())
                  g.drawString(" >" + correction, (getWidth()+widthString)/2, h2-80,
                               Graphics.LEFT | Graphics.BOTTOM);
                else
                if(isSeries60())
                  g.drawString(" >" + correction, (getWidth()+widthString)/2, h2 - 84 + yFont,
                               Graphics.LEFT | Graphics.BOTTOM);
                else
                  g.drawString(" >" + correction, (getWidth()+widthString)/2, h2 - 64 + yFont,
                               Graphics.LEFT | Graphics.BOTTOM);                
            }
            g.setFont(baseFont);
          }
          double val=Util.distance(MIDlet1.locationManager.getLatitude(), MIDlet1.locationManager.getLongitude(), point.Latitude, point.Longitude);
          //
          switch(waypointMode)
          {
          case 0:
            // Вычисляем расстояние
            v=Util.getPointDistance(new Point(MIDlet1.locationManager.getLatitude(), MIDlet1.locationManager.getLongitude(), MIDlet1.locationManager.getAltitude(true)), point);
            Double distance=(Double)v.elementAt(1);
            g.drawString((String)v.elementAt(0), getWidth(), (isSeries60()||isNokia208()?getHeight():h2+64)-baseFont.getHeight(), Graphics.RIGHT|Graphics.BOTTOM);
            g.drawString(distance.toString(), getWidth(), (isSeries60()||isNokia208()?getHeight():h2+64), Graphics.RIGHT|Graphics.BOTTOM);
            // Вычисляем курс
            if(hdg==Double.NaN)
                g.drawString("---", 0, (isSeries60()?getHeight():h2+64), Graphics.LEFT|Graphics.BOTTOM);
            else
                g.drawString(""+hdg, 0, (isSeries60()?getHeight():h2+64), Graphics.LEFT|Graphics.BOTTOM);
            break;
          case 1:
            // Пишем название точки
            // Расстояние
            // Оставшееся время
            g.setColor(0xff, 0xff, 0x80);
            g.fillRect(0, getHeight()-3*g.getFont().getHeight(), getWidth(), 3*g.getFont().getHeight());
            g.setColor(0x80, 0xff, 0x80);
            g.drawLine(2, getHeight()-3*g.getFont().getHeight(),  getWidth()-2, getHeight()-3*g.getFont().getHeight());
            //
            g.setColor(0xff, 0, 0);
            Image im=MIDlet1.rmsManager.categories[point.Category].image;
            if(im==null)
                g.drawString(">"+point.Name, 0, getHeight()-2*g.getFont().getHeight(), Graphics.LEFT|Graphics.BOTTOM);
            else
            {
                g.drawImage(im, 0, getHeight()-2*g.getFont().getHeight(), Graphics.LEFT|Graphics.BOTTOM);
                g.drawString(point.Name, 17, getHeight()-2*g.getFont().getHeight(), Graphics.LEFT|Graphics.BOTTOM);
            }
            v=Util.getDistanceFromKM(val);
            g.drawString(""+(Double)v.elementAt(1)+" "+(String)v.elementAt(0), getWidth()/2, getHeight()-g.getFont().getHeight(), Graphics.HCENTER|Graphics.BOTTOM);
            g.drawString(Util.getETA(val, MIDlet1.locationManager.getSpeed(false)), getWidth()/2, getHeight(), Graphics.HCENTER|Graphics.BOTTOM);
            //            
            break;
          case 2:
          case 3:
            g.setColor(0xff, 0xff, 0x80);
            g.fillRect(0, getHeight()-g.getFont().getHeight()*3, getWidth(), g.getFont().getHeight()*3);
            g.setColor(0x80, 0xff, 0x80);
            g.drawLine(2, getHeight()-3*g.getFont().getHeight(),  getWidth()-2, getHeight()-3*g.getFont().getHeight());
            g.setColor(0xff, 0, 0);
            //
            if(MIDlet1.rmsManager.routePointIndex==-1 || waypointMode==3)
            {
                // Для waypoint
                // Название точки
                im=MIDlet1.rmsManager.categories[point.Category].image;
                if(im==null)
                    g.drawString(point.Name, 0, getHeight()-2*g.getFont().getHeight(), Graphics.LEFT|Graphics.BOTTOM);
                else
                {
                    g.drawImage(im, 0, getHeight()-2*g.getFont().getHeight(), Graphics.LEFT|Graphics.BOTTOM);
                    g.drawString(point.Name, 17, getHeight()-2*g.getFont().getHeight(), Graphics.LEFT|Graphics.BOTTOM);                    
                }
                // Широта
                g.drawString(point.getLatitude(), getWidth()/2, getHeight()-g.getFont().getHeight(), Graphics.HCENTER|Graphics.BOTTOM);
                // Долгота
                g.drawString(point.getLongitude(), getWidth()/2, getHeight(), Graphics.HCENTER|Graphics.BOTTOM);
            }
            else
            {
                Point endpoint=(Point)MIDlet1.rmsManager.route.lastElement();
                // Для Route
                // Название последней точки маршрута
                im=MIDlet1.rmsManager.categories[point.Category].image;
                if(im==null)
                    g.drawString("+"+endpoint.Name, 0, getHeight()-2*g.getFont().getHeight(), Graphics.LEFT|Graphics.BOTTOM);
                else
                {
                    g.drawImage(im, 0, getHeight()-2*g.getFont().getHeight(), Graphics.LEFT|Graphics.BOTTOM);
                    g.drawString(endpoint.Name, 17, getHeight()-2*g.getFont().getHeight(), Graphics.LEFT|Graphics.BOTTOM);                                        
                }
                // Расстояние
                val+=(endpoint.Distance-point.Distance);
                v=Util.getDistanceFromKM(val);
                g.drawString(""+(Double)v.elementAt(1)+" "+(String)v.elementAt(0), getWidth()/2, getHeight()-g.getFont().getHeight(), Graphics.HCENTER|Graphics.BOTTOM);
                // Время
                g.drawString(Util.getETA(val, MIDlet1.locationManager.getSpeed(false)), getWidth()/2, getHeight(), Graphics.HCENTER|Graphics.BOTTOM);
            }
            //
            break;
          }
        }
        //
        g.setColor(0xff, 0xff, 0xff);
        if(isNokia208())
          g.fillArc(w2-32,h2-26,64,64,0,360);
        else
        if(isSeries60())
          g.fillArc(w2-25,h2-19,50,50,0,360);
        else
          g.fillArc(w2-20,h2-14,40,40,0,360);
        //
        g.setColor(0);
        if(isNokia208())
          g.drawArc(w2-32,h2-26,64,64,0,360);
        else
        if(isSeries60())
          g.drawArc(w2-25,h2-19,50,50,0,360);
        else
          g.drawArc(w2-20,h2-14,40,40,0,360);
        //
        g.drawString(""+MIDlet1.locationManager.getSpeed(true), getWidth()/2, h2+11-yFont, Graphics.HCENTER|Graphics.TOP);
        g.setFont(fsmall);
        g.drawString(MIDlet1.rmsManager.getSpeedName(), getWidth()/2+1, h2+13, Graphics.HCENTER|Graphics.TOP);
        //
        g.drawImage(imageArrow, 1, 1, Graphics.LEFT|Graphics.TOP);
        //
        break;
      case TYPE_INFO6:      
        //
        if(MIDlet1.rmsManager.currentTrack.size()>1)
        {
            g.setColor(0xff,0xff,0xA0);
            g.fillRect(0, fsmall.getHeight(), getWidth(), getHeight()-2*fsmall.getHeight());
            //
            double dx = MIDlet1.rmsManager.currentTrack.getLength()/getWidth();
            Point p=(Point)MIDlet1.rmsManager.currentTrack.elementAt(0);
            Point miny = p;
            Point maxy = p;
            double distance=0.0;
            for(int i=0; i<MIDlet1.rmsManager.currentTrack.size(); i++)
            {
                p=(Point)MIDlet1.rmsManager.currentTrack.elementAt(i);
                //
                if(p.Altitude>maxy.Altitude)
                    maxy=p;
                if(p.Altitude<miny.Altitude)
                    miny=p;
                //
                if(i>0)
                {
                  Point prev = (Point)MIDlet1.rmsManager.currentTrack.elementAt(i-1);
                  distance+=Util.distance(prev.Latitude, prev.Longitude, p.Latitude, p.Longitude);
                }
                p.Distance=distance;
            }
            double dy = (maxy.Altitude-miny.Altitude)/(getHeight()-2*fsmall.getHeight());
            g.setColor(0x0000ff);
            for(int i=1; i<MIDlet1.rmsManager.currentTrack.size(); i++)
            {
                Point p1=(Point)MIDlet1.rmsManager.currentTrack.elementAt(i-1);
                Point p2=(Point)MIDlet1.rmsManager.currentTrack.elementAt(i);
                int x1=(int)Math.floor(p1.Distance/dx);
                int y1=(int)Math.floor((p1.Altitude-miny.Altitude)/dy);
                int x2=(int)Math.floor(p2.Distance/dx);
                int y2=(int)Math.floor((p2.Altitude-miny.Altitude)/dy);
                g.drawLine(x1,getHeight()-fsmall.getHeight()-y1,x2,getHeight()-fsmall.getHeight()-y2);
            }
            //
            g.setFont(fsmall);
            g.setColor(0xffffff);
            g.drawString(MIDlet1.rmsManager.currentTrack.getLength()+" "+MIDlet1.rmsManager.getDistanceName(true), getWidth()/2, getHeight(), Graphics.BOTTOM|Graphics.HCENTER);
            g.drawString(maxy.getAltitude()+" "+MIDlet1.rmsManager.getDistanceName(false), 2, fsmall.getHeight()+1, Graphics.TOP|Graphics.LEFT);
            g.drawString(miny.getAltitude()+" "+MIDlet1.rmsManager.getDistanceName(false), 2, getHeight(), Graphics.BOTTOM|Graphics.LEFT);
            //
            g.setColor(0);
            // Distance
            g.drawString(MIDlet1.rmsManager.currentTrack.getLength()+" "+MIDlet1.rmsManager.getDistanceName(true), getWidth()/2-1, getHeight()-1, Graphics.BOTTOM|Graphics.HCENTER);
            g.drawLine(0,getHeight()-fsmall.getHeight(),getWidth(),getHeight()-fsmall.getHeight());
            g.drawLine(0,getHeight()-fsmall.getHeight(),15,getHeight()-fsmall.getHeight()-5);
            g.drawLine(getWidth(),getHeight()-fsmall.getHeight(),getWidth()-15,getHeight()-fsmall.getHeight()-5);
            //
            g.drawString(maxy.getAltitude()+" "+MIDlet1.rmsManager.getDistanceName(false), 1, fsmall.getHeight(), Graphics.TOP|Graphics.LEFT);
            g.drawString(miny.getAltitude()+" "+MIDlet1.rmsManager.getDistanceName(false), 1, getHeight()-1, Graphics.BOTTOM|Graphics.LEFT);
        }
        else
        {
          g.setColor(0x80, 0, 0);
          g.drawString("Elevation: no data", getWidth() / 2, getHeight() / 2,
                       Graphics.HCENTER | Graphics.BOTTOM);            
        }
        //
        break;
      case TYPE_INFO7:
        // Определяем масштаб
        Scale s=null;
        switch(MIDlet1.rmsManager.typeUnit)
        {
            case Util.UNIT_ENGLISH:
                s=(Scale)MIDlet1.rmsManager.englishScales.elementAt(scaleMode);
                break;
            case Util.UNIT_METRIC:
                s=(Scale)MIDlet1.rmsManager.metricScales.elementAt(scaleMode);
                break;
            case Util.UNIT_NAUTICAL:
                s=(Scale)MIDlet1.rmsManager.nauticalScales.elementAt(scaleMode);
                break;
        }
        // Длина в метрах одного градуса по широте постоянна
        double dlat=6356750*Math.PI/180.0;
        // Длина одного градуса по широте в пикселах
        double scalelat=(dlat/s.Length)*20.0;
        // Текущие широта и долгота
        double curlatitude=MIDlet1.locationManager.getLatitude();
        double curlongitude=MIDlet1.locationManager.getLongitude();
        // По долготе меняется в зависимости от широты
        double dlon=Math.cos(Math.toRadians(curlatitude))*6378135*Math.PI/180.0;
        // Длина одного градуса по долготе в пикселах
        double scalelon=(dlon/s.Length)*20.0;
        // Поворот карты
        course=MIDlet1.locationManager.getCourse();
        offsetAngle=0.0;
        if(MIDlet1.rmsManager.autoMapRotation)
          offsetAngle=Math.toRadians(course);
        // Пишем курс сверху
        g.setColor(0);
        g.drawString("" + course, getWidth() / 2, 0,
                     Graphics.HCENTER | Graphics.TOP);
        // Подпись к waypoints
        g.setFont(fsmall);
        // Векторные карты пока не отрисовываются
        drawVectorMap(g, curlatitude, curlongitude, scalelat, scalelon);
        // Наблюдение за другими
        /*
        if(MIDlet1.gpsManager.currentObservable!=null)
        {
            // Выбран объект наблюдения
            curlatitude=MIDlet1.gpsManager.currentObservable.getLatitude();
            curlongitude=MIDlet1.gpsManager.currentObservable.getLongitude();
            //
            dlon=Math.cos(Math.toRadians(curlatitude))*6378135*Math.PI/180.0;
            scalelon=(dlon/s.Length)*20.0;
            // Если выбран наблюдаемый объект, то надо нарисовать себя
            int x=(int)((double)w2+(MIDlet1.gpsManager.getLongitude()-curlongitude)*scalelon);
            int y=(int)((double)h2-(MIDlet1.gpsManager.getLatitude()-curlatitude)*scalelat);
            drawCursor(g, x, y, "self");
        }
        // Чужие треки
        for(int i=0; i<MIDlet1.rmsManager.observables.size(); i++)
        {
          Observable o = (Observable) MIDlet1.rmsManager.observables.elementAt(i);
          if (o.visible)
          {
            if(o.showTrack)
              drawTrack(g, curlatitude, scalelat, curlongitude, scalelon, offsetAngle, o.track);
            //
            int x=(int)((double)w2+(o.getLongitude()-curlongitude)*scalelon);
            int y=(int)((double)h2-(o.getLatitude()-curlatitude)*scalelat);
            drawCursor(g, x, y, o.name);
          }
        }*/
        // Наш маршрут
        if(MIDlet1.rmsManager.route!=null && MIDlet1.rmsManager.showRouteTrack)
          drawTrack(g, 
                    w2-xoffsetTrack, h2-yoffsetTrack, 
                    curlatitude, scalelat, 
                    curlongitude, scalelon, 
                    offsetAngle, MIDlet1.rmsManager.route, 0x00ff00, true);
        // Наш трек
        if(MIDlet1.rmsManager.currentTrack!=null && MIDlet1.rmsManager.showTrackTrack)
          drawTrack(g, 
                    w2-xoffsetTrack, h2-yoffsetTrack, 
                    curlatitude, scalelat, 
                    curlongitude, scalelon, 
                    offsetAngle, MIDlet1.rmsManager.currentTrack, 0x0000ff, false);
        //
        if(MIDlet1.gpsManager.currentObservable==null)
        {
            // Не выбран объект наблюдения
            // Рисуем линию от нас до выбранной waypoint
            point=MIDlet1.rmsManager.getRoutePoint();
            if(MIDlet1.rmsManager.currentPoint!=null)
                point=MIDlet1.rmsManager.currentPoint;
            if(point!=null && MIDlet1.rmsManager.showTargetTrack)
            {
                drawTargetPoint(g, 
                    w2-xoffsetTrack, h2-yoffsetTrack, 
                    curlatitude, scalelat, 
                    curlongitude, scalelon, 
                    offsetAngle, point);
            }
            // Рисуем вектор скорости
            if(MIDlet1.locationManager.getSpeed(false)>0.0)
                drawDirectionArrow(g, w2-xoffsetTrack, h2-yoffsetTrack, course-Math.toDegrees(offsetAngle), 13);
        }        
        // Рисуем waypoints
        if(MIDlet1.rmsManager.showWaypointsTrack)
        {
            drawWaypoints(g, 
                        w2-xoffsetTrack, h2-yoffsetTrack, 
                        curlatitude, scalelat, 
                        curlongitude, scalelon, 
                        offsetAngle, 
                        MIDlet1.rmsManager.points,
                        0x00ff00,
                        MIDlet1.rmsManager.showWaypointsTitleTrack);
            if(MIDlet1.rmsManager.route!=null && MIDlet1.rmsManager.showRouteTrack)
            {
                drawWaypoints(g, 
                        w2-xoffsetTrack, h2-yoffsetTrack, 
                        curlatitude, scalelat, 
                        curlongitude, scalelon, 
                        offsetAngle, 
                        MIDlet1.rmsManager.route,
                        0xffff00,
                        MIDlet1.rmsManager.showWaypointsTitleTrack);                
            }
        }
        // Рисуем текущий выбранный объект
        drawCursor(g, w2-xoffsetTrack, h2-yoffsetTrack, "");
        // Рисуем масштаб
        g.setColor(0);
        g.setFont(fsmall);
        g.drawString(s.Name, getWidth(), getHeight(), Graphics.RIGHT|Graphics.BOTTOM);
        g.drawLine(getWidth()-22, getHeight()-fsmall.getHeight()-2, getWidth()-2, getHeight()-fsmall.getHeight()-2);
        g.drawLine(getWidth()-22, getHeight()-fsmall.getHeight()-4, getWidth()-22, getHeight()-fsmall.getHeight());
        g.drawLine(getWidth()-2, getHeight()-fsmall.getHeight()-4, getWidth()-2, getHeight()-fsmall.getHeight());
        //
        if(!snapTrack)
            g.drawImage(imagePanArrow, getWidth()/2, getHeight(), Graphics.HCENTER|Graphics.BOTTOM);
        g.drawImage(imageArrow, 1, 1, Graphics.LEFT|Graphics.TOP);
        //
        break;
      case TYPE_INFO8:
        // Для любого разрешения, выравнивание по верхней границе
        g.setColor(0xff, 0xff, 0xff);
        g.fillRect(0, 0, getWidth(), yFont);
        g.setColor(0xFF, 0xA0, 0x00);
        g.fillRect(0, yFont, getWidth(), 5);
        g.setColor(178, 204, 204);
        g.fillRect(0, yFont+5, getWidth(), yFont+4);
        g.fillRect(0, 3*yFont+13, getWidth(), yFont+4);
        g.setColor(0);
        g.drawString(".: Trip",getWidth()/2,1,Graphics.TOP|Graphics.HCENTER);
        //
        switch(tripMode)
        {
          case 0:
            // Скорости
            g.drawString("Cur.V: "+MIDlet1.locationManager.getSpeed(true)+" "+MIDlet1.rmsManager.getSpeedName(),1,yFont+7,Graphics.TOP|Graphics.LEFT);
            g.drawString("Max.V: "+MIDlet1.rmsManager.trip.getMaxSpeed()+" "+MIDlet1.rmsManager.getSpeedName(),1,2*yFont+11,Graphics.TOP|Graphics.LEFT);
            g.drawString("Avg.V: "+MIDlet1.rmsManager.trip.getAvgSpeed()+" "+MIDlet1.rmsManager.getSpeedName(),1,3*yFont+15,Graphics.TOP|Graphics.LEFT);
            g.drawString("Total Avg.V: "+MIDlet1.rmsManager.trip.getTotalAvgSpeed()+" "+MIDlet1.rmsManager.getSpeedName(),1,4*yFont+19,Graphics.TOP|Graphics.LEFT);
            break;
          case 1:
            // Пути
            g.drawString("Trip Odom: "+MIDlet1.rmsManager.trip.getTripOdometerDistance()+" "+MIDlet1.rmsManager.getDistanceName(true),1,yFont+7,Graphics.TOP|Graphics.LEFT);
            g.drawString("Moving T: "+MIDlet1.rmsManager.trip.getMovingTime(),1,2*yFont+11,Graphics.TOP|Graphics.LEFT);
            g.drawString("Overall T: "+MIDlet1.rmsManager.trip.getTotalTime(),1,3*yFont+15,Graphics.TOP|Graphics.LEFT);
            g.drawString("Odometer: "+MIDlet1.rmsManager.trip.getOdometerDistance()+" "+MIDlet1.rmsManager.getDistanceName(true),1,4*yFont+19,Graphics.TOP|Graphics.LEFT);
            break;
          case 2:
            // Ускорение
            g.drawString("Cur.A: "+MIDlet1.rmsManager.trip.getAcceleration()+" "+MIDlet1.rmsManager.getAccelerationName(),1,yFont+7,Graphics.TOP|Graphics.LEFT);
            g.drawString("Max.A: "+MIDlet1.rmsManager.trip.getMaxAcceleration()+" "+MIDlet1.rmsManager.getAccelerationName(),1,2*yFont+11,Graphics.TOP|Graphics.LEFT);
            g.drawString("Min.A: "+MIDlet1.rmsManager.trip.getMinAcceleration()+" "+MIDlet1.rmsManager.getAccelerationName(),1,3*yFont+15,Graphics.TOP|Graphics.LEFT);
            break;
          case 3:
            // Высота
            g.drawString("Cur.Alt: "+MIDlet1.rmsManager.trip.getAltitude()+" "+MIDlet1.rmsManager.getDistanceName(false),1,yFont+7,Graphics.TOP|Graphics.LEFT);
            g.drawString("Max.Alt: "+MIDlet1.rmsManager.trip.getMaxAltitude()+" "+MIDlet1.rmsManager.getDistanceName(false),1,2*yFont+11,Graphics.TOP|Graphics.LEFT);
            g.drawString("Min.Alt: "+MIDlet1.rmsManager.trip.getMinAltitude()+" "+MIDlet1.rmsManager.getDistanceName(false),1,3*yFont+15,Graphics.TOP|Graphics.LEFT);
            break;
          default:
            break;
        }
        //
        g.drawImage(imageArrow, 1, 1, Graphics.LEFT|Graphics.TOP);
        //
        break;
    }
    //
    if(type!=TYPE_SPLASH && type!=TYPE_MESSAGE)
    {
      // рисуем восемь прямоугольников
      for(int i=0; i<8; i++)
      {
        g.setColor(0xff, 0xff, 0xff);
        g.fillRect(getWidth()-4, 25+8*i, 3, 6);
        g.setColor(0x00, 0x00, 0xff);
        if(type-TYPE_INFO1==i)
          g.fillRect(getWidth()-4, 25+8*i, 3, 6);
        else
          g.drawRect(getWidth()-4, 25+8*i, 3, 6);
      }
      //
      if(!MIDlet1.locationManager.getValid())
        g.drawImage(imageNoData, getWidth()-1, 1, Graphics.RIGHT|Graphics.TOP);
      //
      if(keyLocked)
        g.drawImage(imageKeylock, getWidth()-1, getHeight()-1, Graphics.RIGHT|Graphics.BOTTOM);
      /*
      g.setFont(f3);
      g.setColor(0xff, 0, 0);
      g.drawString("DEMO", 1, getHeight()*3/4, Graphics.LEFT|Graphics.BOTTOM);
      */
    }
    //
    gdst.drawImage(imageBackBuffer, 0, 0, Graphics.LEFT|Graphics.TOP);
  }

  protected void pointerPressed(int x, int y)
  {
    switch (type)
    {
      case TYPE_SPLASH:
        dismiss();
        break;
    }
  }

  protected void showNotify()
  {    
    timer=new Timer();
    isVisible=true;
    //
    switch (type)
    {
      case TYPE_SPLASH:
        timer.schedule(new CountDown(), 5000);
        break;
      case TYPE_INFO1:
      case TYPE_INFO2:
      case TYPE_INFO3:
      case TYPE_INFO4:
      case TYPE_INFO5:
      case TYPE_INFO6:
      case TYPE_INFO7:
      case TYPE_INFO8:        
        timer.schedule(new CountDown(), 0, 1000);
        break;
    }
  }

  protected void hideNotify()
  {
    isVisible=false;
    //
    if(timer!=null)
    {
        timer.cancel();
        timer=null;
    }
  }
  
  private void dismiss()
  {
    image = null;
    if(timer!=null)
    {
        timer.cancel();
        timer=null;
    }
    //
    switch (type)
    {
      case TYPE_SPLASH:
        //
        MIDlet1.screenManager.replaceScreen(new ListForm("Mode", ListForm.TYPE_MAINMENU, null));
        // Проверка на отладке
        String info=RmsManager.getDebugInfo();
        if(info!=null && info.length()>0)
        {
          MIDlet1.screenManager.pushScreen(new InfoForm("Last error", info, null));
          RmsManager.clearDebugInfo();
        }
        //
        break;
      case TYPE_INFO1:
      case TYPE_INFO2:
      case TYPE_INFO3:
      case TYPE_INFO4:
      case TYPE_INFO5:
      case TYPE_INFO6:
      case TYPE_INFO7:
      case TYPE_INFO8:
        MIDlet1.netManager.close();
        //
        if(ListForm.demoTimer!=null)
        {
          ListForm.demoTimer.cancel();
          ListForm.demoTimer=null;
        }
        else
        {
          if(MIDlet1.bluetoothManager!=null)
          {
            if (MIDlet1.bluetoothManager.State == BluetoothManager.STATE_CONNECTED)
              MIDlet1.bluetoothManager.CloseConnection();
          }
          if(MIDlet1.commManager!=null)
          {
            if (MIDlet1.commManager.State == CommManager.STATE_CONNECTED)
              MIDlet1.commManager.CloseConnection();
          }
        }
        //
        MIDlet1.screenManager.popScreen(MIDlet1.screenManager.size()-1, null);
        //
        break;
    }
  }

  public void commandAction(Command command, Displayable displayable)
  {
    if(displayable==tb)
    {
      if(command==CommandManager.okCommand)
      {
          if(tb.getMaxSize()==80)
          {
              // Append waypoint
              Point p=new Point(MIDlet1.locationManager.getLatitude(), MIDlet1.locationManager.getLongitude(), MIDlet1.locationManager.getAltitude(true));
              p.Name=tb.getString();
              // New waypoint!
              p.appendWaypoint=true;
              MIDlet1.screenManager.pushScreen(new ListForm("Category", ListForm.TYPE_CATEGORY, p));
          }
          else
          if(tb.getMaxSize()==3)
          {
              // Enter command code
              MIDlet1.screenManager.popScreen();
              try
              {
                  int code=Integer.parseInt(tb.getString());
                  if(!executeOperation(code))
                  {
                    MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Unknown code", null));
                    return;
                  }
              }
              catch(Exception ex)
              {
                  MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Wrong code", null));
                  return;
              }
          }
          else
          if(tb.getMaxSize()==30)
          {
              // Send text SMS with position
              String number=tb.getString();
              if(MIDlet1.rmsManager.smsManager.sendText(number, 0, MIDlet1.locationManager.getPositionMessage()))
              {
                  MIDlet1.screenManager.popScreen();
                  return;
              }
          }
          else
          if(tb.getMaxSize()==31)
          {
              try
              {
                  // Send binary SMS with position
                  String number=tb.getString();
                  Point p=new Point(MIDlet1.locationManager.getLatitude(), MIDlet1.locationManager.getLongitude(), MIDlet1.locationManager.getAltitude(true));
                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  DataOutputStream dos = new DataOutputStream(baos);
                  // Waypoint tag
                  dos.writeByte(1);
                  dos.writeByte(p.Category);
                  dos.writeDouble(p.Latitude);
                  dos.writeDouble(p.Longitude);
                  dos.writeDouble(p.Altitude);
                  dos.close();
                  baos.close();
                  //
                  if(MIDlet1.rmsManager.smsManager.send(number, MIDlet1.rmsManager.DESTINATION_SMS_PORT, baos.toByteArray()))
                  {
                    MIDlet1.screenManager.popScreen();
                    return;
                  }
              }
              catch(IOException ex)
              {                  
              }
          }
      }
      else
      if(command==CommandManager.cancelCommand)
      {
          MIDlet1.screenManager.popScreen();
      }
      else
      if(command==CommandManager.codesCommand)
      {
          Vector v=new Vector();
          v.addElement("Fast codes");
          v.addElement("SMS");
          v.addElement("1 - send text SMS with current position, speed and course\n2 - send current position via SMS to another user of the MobiTrackPRO as waypoint");
          v.addElement("Load");
          v.addElement("10 - pixel maps (*.map, *.gmi)\n11 - routes (*.rte, *.kmr)\n12 - waypoints (*.wpt, *.kml)\n13 - online Google map\n14 - online Google satellite map\n15 - online Virtual Earth map\n16 - online Virtual Earth satellite map");
          v.addElement("Fast save");
          v.addElement("70 - track\n71 - waypoints\n72 - KML for the Google Earth\n73 - summary");
          v.addElement("Clear");
          v.addElement("80 - all (track and trip computer)\n81 - track\n82 - trip\n83 - odometer\n84 - acceleration\n85 - altitude\n86 - map cache");
          v.addElement("Settings");
          v.addElement("90 - common\n91 - map <3>\n92 - track <7>\n93 - network\n94 - route\n95 - hardware");
          MIDlet1.screenManager.pushScreen(new InfoForm(v, true));
      }
      return;
    }
    else
    if(displayable==spotForm)
    {
      if(command==CommandManager.sendCommand)
      {
        SettingsForm sf=new SettingsForm(SettingsForm.TYPE_SEND_MESSAGE, null);
        MIDlet1.screenManager.pushScreen(sf);
      }
      else
      if(command==CommandManager.receiveCommand)
      {
        SplashScreen mc = new SplashScreen(0, "Receiving...", 0x00ffffff, null, false);
        MIDlet1.screenManager.replaceScreen(mc);
        MIDlet1.netManager.receiveSpotMessage(10);
      }
      else
      if(command==CommandManager.backCommand)
      {
        MIDlet1.screenManager.popScreen();
      }
      return;
    }
    //
    if(type==TYPE_INFO1 ||
       type==TYPE_INFO2 ||
       type==TYPE_INFO3 ||
       type==TYPE_INFO4 ||
       type==TYPE_INFO5 ||
       type==TYPE_INFO6 ||
       type==TYPE_INFO7 ||
       type==TYPE_INFO8)
    {
      if(command==CommandManager.disconnectCommand)
      {
        MIDlet1.rmsManager.closeNMEA();
        dismiss();
      }
      else
      if(command==CommandManager.loadCommand)
      {
          MIDlet1.screenManager.pushScreen(new ListForm("Load", ListForm.TYPE_LOAD, null));
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
      if(command==CommandManager.selectPointCommand)
      {
        MIDlet1.screenManager.pushScreen(new ListForm("Waypoints", ListForm.TYPE_POINTS, null));
      }
      else
      if(command==CommandManager.routeCommand)
      {
          // Route
          MIDlet1.screenManager.pushScreen(new ListForm("Route", ListForm.TYPE_ROUTE, null));
      }
      else
      if(command==CommandManager.lockCommand)
      {
          keyLocked=!keyLocked;
          Util.playSound(Util.SOUND_KEYLOCK);
      }
      else
      if(command==CommandManager.clearCommand)
      {
          MIDlet1.screenManager.pushScreen(new ListForm("Clear", ListForm.TYPE_CLEAR, null));
      }
      else
      if(command==CommandManager.sendTrackCommand)
      {
        MIDlet1.netManager.sendTrack();
      }
      else
      if(command==CommandManager.selectMapCommand)
      {
        MIDlet1.screenManager.pushScreen(new ListForm("Maps", ListForm.TYPE_MAPS, null));
      }
      else
      if(command==CommandManager.helpCommand)
      {
        MIDlet1.screenManager.pushScreen(new InfoForm("Help", helpMessage, null));
      }
      else
      if(command==CommandManager.hideCommand)
      {
        // Убрать мидлет с экрана
        // Актуально для SonyEricsson
        MIDlet1.screenManager.setCurrent(null);
      }
    }
  }
  

  public void Update(Object data)
  {
    if(data!=null)
    {
        if(data instanceof Point)
        {
          Point p=(Point)data;
          if(p.appendWaypoint)
          {              
              p.appendWaypoint=false;
              if(!MIDlet1.rmsManager.appendWaypoint(p, false))
              {                  
                  MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Waypoint with name '"+p.Name+"' already exists", null));
                  p=null;
                  return;
              }
              //
              MIDlet1.rmsManager.smartSaveRMS(RmsManager.RMS_SETTINGS);
          }
          else
            // Переключаем на компас
            MIDlet1.updateMainWindow(SplashScreen.TYPE_INFO5);
        }
        else
        if(data instanceof Vector)
            showSpotMessages();    
        else
        if(data instanceof String)
            MIDlet1.rmsManager.processFile((String)data);
    }
  }

  /**
   * keyRepeated
   *
   * @param int0 int
   * @todo Implement this javax.microedition.lcdui.Canvas method
   */
  protected void keyRepeated(int keyCode)
  {
      if(type==TYPE_INFO3 && keyCode==Canvas.KEY_NUM3)
      {
          if(!MIDlet1.rmsManager.pleaseWait)
          {
              if(MIDlet1.rmsManager.mapType==RmsManager.MAP_INTERNAL)
              {
                  loadRightMap(MIDlet1.locationManager.getLatitude(), MIDlet1.locationManager.getLongitude());
              }
              else
              if(outOfMap)
              {
                  // Загружаем карту
                  if(MIDlet1.rmsManager.mapType==RmsManager.MAP_GOOGLE_MAP)
                      MIDlet1.netManager.sendGoogleMap();
                  else
                  if(MIDlet1.rmsManager.mapType==RmsManager.MAP_GOOGLE_SAT)
                      MIDlet1.netManager.sendGoogleSat();
                  else
                  if(MIDlet1.rmsManager.mapType==RmsManager.MAP_VEARTH_MAP)              
                      MIDlet1.netManager.sendVEarthMap();
                  else
                  if(MIDlet1.rmsManager.mapType==RmsManager.MAP_VEARTH_SAT)
                      MIDlet1.netManager.sendVEarthSat();                  
              }
          }
      }
      else
          gameActions(getGameAction(keyCode));
      //
      repaint();      
  }

  private class CountDown extends TimerTask
  {
    public void run()
    {
      /*
      if(System.currentTimeMillis()-startTick>1800000L)
      {
        MIDlet1.screenManager.pushScreen(new InfoForm("DEMO", "Sorry, but the time limit for DEMO version has been reached. The MobiTrack will be closed immediately.", null));
      }*/
      //
          long curtime = System.currentTimeMillis();
          //
          if(curtime-lastFlash>18000L)
          {
              lastFlash = curtime;
//#if NokiaGUI
//#               // OFF
//#               if(MIDlet1.rmsManager.backlightLevel==0)
//#                 DeviceControl.setLights(0, 100);
//#               else
//#                 DeviceControl.setLights(0, 0);
//#               // ON
//#               DeviceControl.setLights(0, MIDlet1.rmsManager.backlightLevel);
//#endif
          }
      //
      switch(type)
      {
      case TYPE_SPLASH:
        dismiss();
        break;
      case TYPE_INFO1:
      case TYPE_INFO2:
      case TYPE_INFO3:
      case TYPE_INFO4:
      case TYPE_INFO5:
      case TYPE_INFO6:
      case TYPE_INFO7:
      case TYPE_INFO8:
        if(isVisible)
        {
            colorBlack=!colorBlack;
            repaint();
        }
        // Получение данных о других объектах
        /*
        for(int i=0; i<MIDlet1.rmsManager.observables.size(); i++)
        {
          Observable o = (Observable) MIDlet1.rmsManager.observables.elementAt(i);
          o.Tick();
        }*/
        //
        break;
      }
    }
  }

  public void rotate(double x, double y, double angle)
  {
    if(angle==0.0)
    {
      tmpX=x;
      tmpY=y;
      return;
    }
    // Определяем длину радиус-вектора
    double r=Math.sqrt(x*x+y*y);
    // Текущий угол
    double a=Float11.atan2(y, x);
    a+=angle;
    //
    tmpY=r*Math.sin(a);
    tmpX=r*Math.cos(a);
  }

   protected void keyReleased(int keyCode)
   {
       if(keyLocked)
            return;
       //
       int gameAction=getGameAction(keyCode);
       if(type==TYPE_INFO3 && (gameAction==Canvas.LEFT || gameAction==Canvas.RIGHT) && snapMap)
       {
          // Загружаем карту
          if(MIDlet1.rmsManager.mapType==RmsManager.MAP_GOOGLE_MAP)
              MIDlet1.netManager.sendGoogleMap();
          else
          if(MIDlet1.rmsManager.mapType==RmsManager.MAP_GOOGLE_SAT)
              MIDlet1.netManager.sendGoogleSat();
          else
          if(MIDlet1.rmsManager.mapType==RmsManager.MAP_VEARTH_MAP)              
              MIDlet1.netManager.sendVEarthMap();
          else
          if(MIDlet1.rmsManager.mapType==RmsManager.MAP_VEARTH_SAT)
              MIDlet1.netManager.sendVEarthSat();
       }
   }

  private void loadRightMap(double latitude, double longitude)
  {
      tmpLatitude = latitude;
      tmpLongitude = longitude;
      MIDlet1.rmsManager.pleaseWait = true;
      //
      MIDlet1.screenManager.call(this);
      //Thread t=new Thread(this);
      //t.start();
  }
  
  public void run()
  {
      double minsize=Double.MAX_VALUE;
      int resultIndex=-1;
      // DEBUG
      //mess1 = ""+latitude;
      //mess2 = ""+longitude;
      // Ищем среди карт описанных в JAD подходящую
      // наименьшего размера
      for(int i=0; i<MIDlet1.rmsManager.mapRects.size(); i++)
      {
          Object obj=MIDlet1.rmsManager.mapRects.elementAt(i);
          if(obj==null)
              continue;
          Rect rect=(Rect)obj;
          //
          if(rect.isInside(tmpLongitude, tmpLatitude))
          {
              if(rect.widthLongitude+rect.heightLatitude<minsize)
              {
                  resultIndex=i;
                  //mess3="Map: "+i;
                  minsize=rect.widthLongitude+rect.heightLatitude;
              }
          }
      }
      //
      //mess3="Found: "+resultIndex;
      if(resultIndex>=0)
      {
          MIDlet1.rmsManager.selectMap(resultIndex+1);
      }
      //
      MIDlet1.rmsManager.pleaseWait = false;
  }
}
