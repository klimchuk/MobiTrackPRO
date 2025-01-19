package henson.midp.View;

import javax.microedition.lcdui.*;
import henson.midp.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class InfoForm extends Form implements CommandListener
{
  StringItem si=null;
  int flags=StringItem.LAYOUT_2|StringItem.LAYOUT_LEFT|StringItem.LAYOUT_SHRINK|StringItem.LAYOUT_NEWLINE_AFTER;

  public InfoForm(Vector v, boolean supressEmpty)
  {
    super("Info");
    // Set up this Displayable to listen to command events
    setCommandListener(this);
    // add the Exit command
    addCommand(CommandManager.closeCommand);
    //
    String header=(String)v.elementAt(0);
    setTitle(header);
    //
    for(int i=0; i<(v.size()-1)/2; i++)
    {
      String title=(String)v.elementAt(i*2+1);
      String text=(String)v.elementAt(i*2+2);
      if(!supressEmpty || text.length()>0)
      {
        si = new StringItem(title + "\n", text, StringItem.PLAIN);
        si.setLayout(flags);
        this.append(si);
      }
    }
  }

  public InfoForm(String title, String text, String imageName)
  {
    super("Info");
    // Set up this Displayable to listen to command events
    setCommandListener(this);
    // add the Exit command
    addCommand(CommandManager.closeCommand);
    //
    if(imageName!=null)
    {
      Image image=Util.makeImage(imageName);
      if(image!=null)
        this.append(image);
    }
    //
    si=new StringItem(title+"\n", text, StringItem.PLAIN);
    si.setLayout(flags);
    this.append(si);
  }

  public InfoForm(String title, String text)
  {
    super("Info");
    // Set up this Displayable to listen to command events
    setCommandListener(this);
    // add the Exit command
    addCommand(CommandManager.yesCommand);
    addCommand(CommandManager.noCommand);
    //
    si=new StringItem(title+"\n", text, StringItem.PLAIN);
    si.setLayout(flags);
    this.append(si);
  }

  public void commandAction(Command command, Displayable displayable)
  {
    if(command==CommandManager.closeCommand)
      MIDlet1.screenManager.popScreen();
    else
    if(command==CommandManager.yesCommand)
      MIDlet1.screenManager.popScreen(1, "Yes");
    else
    if(command==CommandManager.noCommand)
      MIDlet1.screenManager.popScreen();
  }
}
