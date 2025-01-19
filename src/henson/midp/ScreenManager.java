package henson.midp;

import java.util.*;
import javax.microedition.lcdui.*;
import henson.midp.View.*;

public class ScreenManager extends Stack
{
  /** ���������� ����� ������� */
  private Display display=null;
  //
  public ScreenManager(Display display)
  {
    this.display=display;
  }
  
  public void call(Runnable rrr)
  {
      this.display.callSerially(rrr);
  }

  public void pushScreen(Displayable disp)
  {
    // ������ �������
    display.setCurrent(disp);
    // ��������� � ����
    super.push(disp);
  }

  public void replaceScreen(Displayable disp)
  {
    // �������� ������� ����� �����
    display.setCurrent(disp);
    //
    super.pop();
    super.push(disp);
  }
  
  public void setCurrent(Displayable disp)
  {
      display.setCurrent(disp);
  }

  public Displayable getTopWindow()
  {
    //return display.getCurrent();
    if(super.empty())
      return null;
    else
      return (Displayable)super.peek();
  }

  public void popScreen(int count, Object data)
  {
    Displayable disp = null;
    // ������� ������� �����
    for(int i=0; i<count; i++)
    {
      super.pop();
      //
      if (super.empty())
        // ����� �� ����������
        MIDlet1.quitApp();
      else
        // ������ �������
        disp = (Displayable)super.peek();
    }
    //
    if(disp!=null)
    {
      display.setCurrent(disp);
      if (disp instanceof IUpdate)
      {
        IUpdate iupdate = (IUpdate) disp;
        iupdate.Update(data);
      }
    }
  }

  public void popScreen()
  {
    popScreen(1, null);
  }

  public void flashLight(int duration)
  {
    if(display!=null)
      display.flashBacklight(duration);
  }
  
  public void vibra(int duration)
  {
      if(display!=null)
          display.vibrate(duration);
  }
}
