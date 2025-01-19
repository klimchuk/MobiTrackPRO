package henson.midp.Model;

import henson.midp.View.*;
import henson.midp.*;
import java.io.*;
import java.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class VectorMap
{
  public Vector items=new Vector();

  public VectorMap()
  {
  }

  public boolean Open(InputStream is)
  {
    items.removeAllElements();
    //
    boolean result=true;
    try
    {      
      DataInputStream dis=new DataInputStream(is);
      //
      int size=dis.readInt();
      for(int i=0; i<size; i++)
      {
        VectorMapItem vmi=new VectorMapItem();
        vmi.load(dis);
        items.addElement(vmi);
      }
      //
      dis.close();
      //is.close();
    }
    catch (IOException ex)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't load map", null));
      result=false;
    }
    catch (OutOfMemoryError ex)
    {
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Not enough memory to load map", null));
      result=false;
    }
    return result;
  }
}
