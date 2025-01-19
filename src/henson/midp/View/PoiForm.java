package henson.midp.View;

import javax.microedition.lcdui.*;
import henson.midp.*;
import henson.midp.Model.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PoiForm extends Form implements CommandListener
{
  TextField tf=null;
  TextField tf2=null;
  TextField tf3=null;
  TextField tf4=null;
  TextField tf5=null;
  //
  Point point=null;
  //
  public PoiForm(Point p)
  {
    super("Waypoint");

    tf=new TextField("Name", (p==null?"":p.Name), 80, TextField.ANY);
    this.append(tf);
    int latitude=(int)p.Latitude;
    tf2 = new TextField("Latitude, degrees", (p==null?"":""+latitude), 3, TextField.NUMERIC);
    this.append(tf2);
    double latitude2=round(Math.abs((p.Latitude-(double)latitude)*60.0));
    tf3 = new TextField("Latitude, minutes", (p==null?"":""+latitude2), 6, TextField.DECIMAL);
    this.append(tf3);
    int longitude=(int)p.Longitude;
    tf4 = new TextField("Longitude, degrees", (p==null?"":""+longitude), 4, TextField.NUMERIC);
    this.append(tf4);
    double longitude2=round(Math.abs((p.Longitude-(double)longitude)*60.0));
    tf5 = new TextField("Longitude, minutes", (p==null?"":""+longitude2), 6, TextField.DECIMAL);
    this.append(tf5);
    //
    point=p;
    //
    setCommandListener(this);
    addCommand(CommandManager.okCommand);
    addCommand(CommandManager.cancelCommand);
  }
  
  private double round(double arg)
  {
      double d=Math.floor(arg*1000.0)/1000.0;
      if((arg-d)*10000>=5.0)
          return Math.floor((d+0.001)*1000.0)/1000.0;
      else
          return d;
  }

  public void commandAction(Command command, Displayable displayable)
  {
    if(command==CommandManager.okCommand)
    {
      if(tf.getString()==null ||
          tf2.getString()==null ||
          tf3.getString()==null ||
          tf4.getString()==null ||
          tf5.getString()==null)
            return;
      // Записываем изменения      
      point.Name=tf.getString();
      if(point.Name.length()==0 ||
         tf2.getString().length()==0 ||
         tf4.getString().length()==0)
        return;
      //
      double lathour=Double.parseDouble(tf2.getString());
      double latmin=0.0;
      if(tf3.getString().length()>0)
        latmin=Double.parseDouble(tf3.getString());
      //
      if(lathour>=0.0)
        point.Latitude=lathour+latmin/60.0;
      else
        point.Latitude=lathour-latmin/60.0;
      //
      double lonhour=Double.parseDouble(tf4.getString());
      double lonmin=0.0;
      if(tf5.getString().length()>0)
        lonmin=Double.parseDouble(tf5.getString());
      if(lonhour>=0.0)
        point.Longitude=lonhour+lonmin/60.0;
      else
        point.Longitude=lonhour-lonmin/60.0;
      //
      MIDlet1.rmsManager.smartSaveRMS(RmsManager.RMS_SETTINGS);
      //
      MIDlet1.screenManager.pushScreen(new ListForm("Category", ListForm.TYPE_CATEGORY, point));
      //
      //MIDlet1.screenManager.popScreen(1, point);
    }
    else
      MIDlet1.screenManager.popScreen();
  }
}
