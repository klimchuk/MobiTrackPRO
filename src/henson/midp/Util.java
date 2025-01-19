package henson.midp;

import javax.microedition.lcdui.*;
import java.util.*;
import java.io.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;

import henson.midp.Model.*;
import henson.midp.View.*;

public class Util
{
  // Отношения единиц измерения
  public static double KM2MI=0.62137119;
  public static double KM2NM=0.5399568;
  public static double KT2KM=1.852;
  public static double M2FT=3.2808399;
  public static double MS2KMH=3.600;
  // Единицы измерения
  public static final int UNIT_ENGLISH=0;
  public static final int UNIT_METRIC=1;
  public static final int UNIT_NAUTICAL=2;  
  // Звуки
  public static final int SOUND_VALID=1;
  public static final int SOUND_INVALID=2;
  public static final int SOUND_NETWORK=3;
  public static final int SOUND_ERROR=4;
  public static final int SOUND_KEYLOCK=5;
  public static final int SOUND_NEXTPOINT=6;
  public static final int SOUND_INCOMING=7;
  public static final int SOUND_ENDROUTE=8;
  public static final int SOUND_SMS=9;

  public static String removeControlSymbols(String s, int maxLength)
  {
      StringBuffer sb=new StringBuffer();
      for(int i=0; i<s.length(); i++)
      {
          char ch=s.charAt(i);
          if(Character.isDigit(ch) || 
                  Character.isLowerCase(ch) ||
                  Character.isUpperCase(ch))
              sb.append(ch);
      }
      String result=sb.toString();
      int len=result.length();
      if(len>maxLength)
          return result.substring(len-maxLength, len);
      else
          return result;
  }
  
  public static Image makeImage(String filename)
  {
    Image image = null;
    try
    {
      image = Image.createImage(filename);
    }
    catch (IOException ex)
    {
      image=null;
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Can't load map "+filename, null));
    }
    catch (OutOfMemoryError ex)
    {
      image=null;
      MIDlet1.screenManager.pushScreen(new InfoForm("Error", "Not enough memory to load map "+filename, null));
    }
    return image;
  }

  public static double distance(double lat1, double lon1, double lat2, double lon2)
  {
    double R=6371.0;
    double dLat  = Math.toRadians(lat2 - lat1);
    double dLong = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLong/2) * Math.sin(dLong/2);
    double c = 2 * Float11.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
  }

  public static double heading(double lat1, double lon1, double lat2, double lon2)
  {
    double dLat  = Math.toRadians(lat2 - lat1);
    double dLong = Math.toRadians(lon2 - lon1);
    double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLong/2) * Math.sin(dLong/2);
    double d = 2 * Float11.atan2(Math.sqrt(a), Math.sqrt(1-a));
    double heading=Float11.acos((Math.sin(Math.toRadians(lat2)) - Math.sin(Math.toRadians(lat1)) * Math.cos(d)) / (Math.sin(d) * Math.cos(Math.toRadians(lat1))));
    if (Math.sin(dLong) < 0.0)
      heading=Math.PI*2-heading;
    return heading;
  }
  
  public static String withLeadZero(long n)
  {
      if(n<10L)
          return "0"+n;
      else
          return ""+n;
  }

/*
  public static double normalize(double angle, int level)
  {
    while(angle<0)
      angle+=level;
    while(angle>=level)
      angle-=level;
    return angle;
  }

  public static String toHourMin(double val)
  {
    if(val==Double.MIN_VALUE)
      return "--:--";
    if(val==Double.MAX_VALUE)
      return "++:++";
    //
    int hour=(int)Math.floor(val);
    int min=(int)((val-hour)*60.0);
    return hour+":"+(min<10?"0"+min:""+min);
  }

  public static void LoadStringVector(Vector v, DataInputStream dis) throws IOException
  {
    v.removeAllElements();
    int count=dis.readInt();
    for(int i=0; i<count; i++)
      v.addElement(dis.readUTF());
  }

  public static void SaveStringVector(Vector v, DataOutputStream dos) throws IOException
  {
    dos.writeInt(v.size());
    for(int i=0; i<v.size(); i++)
      dos.writeUTF((String)v.elementAt(i));
  }
*/
  public static int smartRead(InputStream is, byte[] data, int len) throws IOException
  {
    int bytes=0;
    int offset=0;
    //
    while (true)
    {
      bytes = is.read(data, offset, len - offset);
      if (bytes == -1 || offset >= len)
        break;
      offset += bytes;
    }
    //
    return offset;
  }
/*
  public static byte[] httpCall(String url) throws IOException
  {
    byte[] buf = null;
    //
    HttpConnection conn=(HttpConnection) Connector.open(url);
    conn.setRequestMethod(HttpConnection.GET);
    //conn.setRequestProperty("Content-Type", "application/octet-stream");
    conn.setRequestProperty("Connection", "close");
    //
    int rc = conn.getResponseCode();
    if (rc == HttpConnection.HTTP_OK)
    {
      InputStream is = conn.openInputStream();
      int len = (int) conn.getLength();
      if (len > 0)
      {
        buf = new byte[len];
        if(smartRead(is, buf, len)!=len)
          buf=null;
      }
      else
      {
        int bytes;
        int offset=0;
        byte[] data=new byte[8192];
        while (true)
        {
          bytes = is.read();
          if (bytes == -1 || offset>=8192)
            break;
          data[offset++]=(byte)bytes;
        }
        if(offset>0)
        {
          buf=new byte[offset];
          System.arraycopy(data, 0, buf, 0, offset);
        }
      }
    }
    //
    return buf;
  }

   public static String extractTextFromHTML(String HTMLBody)
   {
     String s = HTMLBody.toUpperCase();
     int indexBegin = s.indexOf("<BODY");
     indexBegin = s.indexOf(">", indexBegin + 5);
     int indexEnd = s.indexOf("</BODY>");
     String x = s.substring(indexBegin + 1, indexEnd);
     //
     int pos = 0;
     byte tag = 0;
     StringBuffer sb = new StringBuffer();
     while (pos < x.length())
     {
       if (tag == 0 && x.charAt(pos) == '<')
         tag = 1;
       if (tag == 0 && x.charAt(pos) == '&')
         tag = 2;
         //
       if (tag == 0)
         sb.append(x.charAt(pos));
         //
       if (tag == 1 && x.charAt(pos) == '>')
         tag = 0;
       if (tag == 2 && x.charAt(pos) == ';')
         tag = 0;
         //
       pos++;
    }
    //
    String result=sb.toString();
    return result.trim();
  }

*/
  /*
  private static int indexOneOf(String src, String chars)
  {
      int index=Integer.MAX_VALUE;
      for(int i=0; i<chars.length(); i++)
      {
          int pos=src.indexOf(""+chars.charAt(i));
          if(pos!=-1 && pos<index)
              index=pos;
      }
      if(index==Integer.MAX_VALUE)
        return -1;
      else
        return index;
  }*/
  
  public static Vector parseString(String s, String delim, boolean trim)
  {
    Vector v=new Vector();
    int pos=0;
    while(pos<s.length())
    {
      int index=s.indexOf(delim, pos);
      if(index==-1)
      {
        String sn=new String(s.substring(pos));
        if(trim)
          v.addElement(sn.trim());
        else
          v.addElement(sn);
        break;
      }
      else
      {
        String sn=new String(s.substring(pos, index));
        if(trim)
          v.addElement(sn.trim());
        else
          v.addElement(sn);
        pos=index+1;
      }
    }
    //
    if(s.endsWith(","))
        v.addElement("");
    //
    return v;
  }

  public static boolean isRegistered()
  {
    StringBuffer sb=new StringBuffer();
    // Конвертируем одно в другое
    for(int i=0; i<MIDlet1.rmsManager.userName.length(); i++)
    {
      char ch=MIDlet1.rmsManager.userName.charAt(i);
    }
    //
    return MIDlet1.rmsManager.userKey.equals(sb.toString());
  }

  public static void playSound(int type)
  {
    if (!MIDlet1.instance.rmsManager.useSound)
      return;
    //
    try
    {
      switch (type)
      {
        case SOUND_INVALID:
          Manager.playTone(74, 500, 80);
          Thread.sleep(500);
          Manager.playTone(80, 1000, 80);
          //Manager.playTone(ToneControl.C4, 1000, 100);
          break;
        case SOUND_VALID:          
          Manager.playTone(80, 500, 80);
          Thread.sleep(500);
          Manager.playTone(74, 500, 80);
          //Manager.playTone(ToneControl.C4+2, 100, 80);
          break;
        case SOUND_NETWORK:
          Manager.playTone(68, 500, 80);
          //Manager.playTone(ToneControl.C4, 100, 80);
          break;
        case SOUND_ERROR:
          Manager.playTone(88, 1000, 100);
          break;
        case SOUND_KEYLOCK:
	  Manager.playTone(80, 500, 100);
          //Manager.playTone(ToneControl.C4+7, 100, 80);
          break;
        case SOUND_NEXTPOINT:
          Manager.playTone(80, 500, 100);
          break;
        case SOUND_INCOMING:
          Manager.playTone(74, 200, 80);
          break;
        case SOUND_ENDROUTE:
          Manager.playTone(80, 1000, 100);
          break;
        case SOUND_SMS:
          Manager.playTone(74, 200, 100);
          try{ Thread.sleep(100); }
          catch(InterruptedException ex) { }
          Manager.playTone(74, 200, 100);
          try{ Thread.sleep(100); }
          catch(InterruptedException ex) { }
          Manager.playTone(74, 200, 100);
          break;
      }
    }
    catch (MediaException ex)
    {
      // Ошибку со звуком игнорируем
      //MIDlet1.screenManager.pushScreen(new InfoForm("Media error", ex.getMessage(), null));
    }
    catch(InterruptedException e)
    {
    }     
     /*
      byte tempo = 30;
       byte d = 8;

       byte C4 = ToneControl.C4;;
       byte D4 = (byte)(C4 + 2);
       byte E4 = (byte)(C4 + 4);
       byte F4 = (byte)(C4 + 5);
       byte G4 = (byte)(C4 + 7);
       byte rest = ToneControl.SILENCE;

       byte[] mySequence = {
        ToneControl.VERSION, 1,
        ToneControl.TEMPO, tempo,
        ToneControl.BLOCK_START, 0,  // starting A part
        C4,d, F4,d, F4,d, C4,d, F4,d, F4,d, C4,d, F4,d,
        ToneControl.BLOCK_END, 0,   // ending A part
        ToneControl.BLOCK_START, 1,  // starting B part
        C4,d, E4,d, E4,d, C4,d, E4,d, E4,d, C4,d, E4,d,
        ToneControl.BLOCK_END, 1,   // ending B part
        ToneControl.PLAY_BLOCK, 0,  // playing A part
        ToneControl.PLAY_BLOCK, 1,  // playing A part
        ToneControl.PLAY_BLOCK, 0,  // playing A part
       };

       try{
        Player p = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
        p.realize();
        ToneControl c = (ToneControl)p.getControl("ToneControl");
        c.setSequence(mySequence);
        p.start();
       } catch (IOException ioe) {
       } catch (MediaException me) {}*/
  }

  public static String toHex(byte b)
  {
    String s=Integer.toHexString((int)b);
    int len=s.length();
    if(len>2)
      return s.substring(len-2);
    while(s.length()<2)
      s="0"+s;
    return s;
  }

/*
  public static Image resize(int[] pixels, int width, int height, int scale)
  {
    int newW = width*scale/100;
    int newH = height*scale/100;
    int index;
    int[] newbuffer = new int[newH * newW];
    int loc, oldloc = 0;
    //long before = System.currentTimeMillis();
    for (int i = 0; i < newH; i++) {
      for (int j = 0; j < newW; j++) {
        loc = i * newW + j;
        index = (int) (i * height / newH) * width
            + (int) (j * width / newW);
        //System.arraycopy(orig,index,newbuffer,loc,1);
        newbuffer[loc] = pixels[index];
      }
    }
    //System.out.println("time:"+(System.currentTimeMillis()-before));
    Image newImage = Image.createRGBImage(newbuffer, newW, newH, true);
    newbuffer = null;
    System.gc(); //
    return newImage;
  }*/

  public static Image resizeImage(int[] pixels, int width, int height, int scale, boolean fast)
  {
      /// init sizes of images
      int oldW = width;
      int oldH = height;
      int newW = oldW*scale/100;
      int newH = oldH*scale/100;

      /// grab oldImage -> int[] // в мидлете по другому
      //int[] pixels = new int[oldW * oldH];
      //image.getRGB(pixels, 0, 0, 0, 0, oldW, oldH);
      /// resize code /not optimized/
      int[] lines = new int[newW * oldH];
      int[] columns = new int[newW * newH];

      if (fast)
      {
          ///Быстрый алгоритм
        if (newW < oldW) {
          for (int k = 0; k < oldH; k++) { // trough all lines
            int i = k * oldW; // index in old pix
            int j = k * newW; // index in new pix
            int part = newW;
            int addon = 0, r = 0, g = 0, b = 0, a = 0;
            for (int m = 0; m < newW; m++) { ///OPTI ijm!!! need???
              int total = oldW;
              int R = 0, G = 0, B = 0, A = 0;
              if (addon != 0) {
                R = r * addon;
                G = g * addon;
                B = b * addon;
                A = a * addon;
                total -= addon;
              }
              while (0 < total) {
                a = (pixels[i] >> 24) & 0xff;
                r = (pixels[i] >> 16) & 0xff;
                g = (pixels[i] >> 8) & 0xff;
                b = pixels[i++] & 0xff;
                if (total > part) {
                  R += r * part;
                  G += g * part;
                  B += b * part;
                  A += a * part;
                }
                else {
                  R += r * total;
                  G += g * total;
                  B += b * total;
                  A += a * total;
                  addon = part - total;
    ///set new pixel
                  lines[j++] = ( (R / oldW) << 16) | ( (G / oldW) << 8) |
                      (B / oldW) | ( (A / oldW) << 24); // A??
                }
                total -= part;
              }
            }
          }
        }
        else { /// newW > oldW
          int part = oldW;
          for (int k = 0; k < oldH; k++) { // trough all lines
            int i = k * oldW; // index in old pix
            int j = k * newW; // index in new pix
            int total = 0;
            int r = 0, g = 0, b = 0, a = 0;
            for (int m = 0; m < newW; m++) {
              int R = 0, G = 0, B = 0, A = 0;
              if (total >= part) {
                R = r * part;
                G = g * part;
                B = b * part;
                A = a * part;
                total -= part;
              }
              else {
                if (0 != total) {
                  R = r * total;
                  G = g * total;
                  B = b * total;
                  A = a * total;
                }
                a = (pixels[i] >> 24) & 0xff;
                r = (pixels[i] >> 16) & 0xff;
                g = (pixels[i] >> 8) & 0xff;
                b = pixels[i++] & 0xff;
                int addon = part - total;
                R += r * addon;
                G += g * addon;
                B += b * addon;
                A += a * addon;
                total = newW - addon;
              }
    ///set new pixel
              lines[j++] = ( (R / oldW) << 16) | ( (G / oldW) << 8) |
                  (B / oldW) | ( (A / oldW) << 24); // A??
            }
          }
        }
    /// проходим по столбцам
        if (newH < oldH) {
          for (int k = 0; k < newW; k++) { // trough columns
            int i = k; // index in lines pix
            int j = k; // index in new pix
            int part = newH;
            int addon = 0, r = 0, g = 0, b = 0, a = 0;
            for (int m = 0; m < newH; m++) {
              int total = oldH;
              int R = 0, G = 0, B = 0, A = 0;
              if (addon != 0) {
                R = r * addon;
                G = g * addon;
                B = b * addon;
                A = a; //*addon;
                total -= addon;
              }
              while (0 < total) {
    //            a = (lines[i] >> 24) & 0xff;// may no rotate
                a = lines[i] & 0xff000000;
                r = (lines[i] >> 16) & 0xff;
                g = (lines[i] >> 8) & 0xff;
                b = lines[i] & 0xff;
                i += newW;
                if (total > part) {
                  R += r * part;
                  G += g * part;
                  B += b * part;
                  A += a; //*part;
                }
                else {
                  R += r * total;
                  G += g * total;
                  B += b * total;
                  A += a; //*total;
                  addon = part - total;
    ///set new pixel
                  if (0 != A)
                    columns[j] = ( (R / oldH) << 16) | ( (G / oldH) << 8) |
                        (B / oldH) | 0xff000000; // A??
                  else
                    columns[j] = 0; //((R/oldH)<<16)|((G/oldH)<<8)|(B/oldH); // A??
                  j += newW;
                }
                total -= part;
              }
            }
          }
        }
        else {
          int part = oldH;
          for (int k = 0; k < newW; k++) { // trough all lines
            int i = k; // index in old pix
            int j = k; // index in new pix
            int total = 0;
            int r = 0, g = 0, b = 0, a = 0;
            for (int m = 0; m < newH; m++) {
              int R = 0, G = 0, B = 0, A = 0;
              if (total >= part) {
                R = r * part;
                G = g * part;
                B = b * part;
                A = a; //*part;
                total -= part;
              }
              else {
                if (0 != total) {
                  R = r * total;
                  G = g * total;
                  B = b * total;
                  A = a; //*total;
                }
    //            a = (lines[i] >> 24) & 0xff;// may no rotate
                a = lines[i] & 0xff000000;
                r = (lines[i] >> 16) & 0xff;
                g = (lines[i] >> 8) & 0xff;
                b = lines[i] & 0xff;
                i += newW;
                int addon = part - total;
                R += r * addon;
                G += g * addon;
                B += b * addon;
                A += a; //*addon;
                total = newH - addon;
              }
    ///set new pixel
              if (0 != A)
                columns[j] = ( (R / oldH) << 16) | ( (G / oldH) << 8) |
                    (B / oldH) | 0xff000000; // A??
              else
                columns[j] = 0; //((R/oldH)<<16)|((G/oldH)<<8)|(B/oldH);

              j += newW;
            }
          }
        }
      }
    ///медленный алгоритм (зато простой)
      else { //not fast
        for (int k = 0; k < oldH; k++) {
          int id = k * oldW;
          int a = (pixels[id] >> 24) & 0xff,
              r = (pixels[id] >> 16) & 0xff,
              g = (pixels[id] >> 8) & 0xff,
              b = pixels[id] & 0xff,
              t = newW;
          for (int i = 0; i < newW; i++) {
            int A = 0, R = 0, G = 0, B = 0;
            for (int j = 0; j < oldW; j++) {
              A += a;
              R += r;
              G += g;
              B += b;
              t--;
              if (0 == t) {
                id++;
                t = newW;
                if (id < oldW * oldH) {
                  a = (pixels[id] >> 24) & 0xff;
                  r = (pixels[id] >> 16) & 0xff;
                  g = (pixels[id] >> 8) & 0xff;
                  b = pixels[id] & 0xff;
                }
              }
            } /// A???
            lines[i + k * newW] = ( (R / oldW) << 16) | ( (G / oldW) << 8) |
                (B / oldW) | ( (A / oldW) << 24); // A??
          }
        }
    // проходим по столбцам
        for (int k = 0; k < newW; k++) {
          int id = k; // upper line
          int a = (lines[id] >> 24) & 0xff,
              r = (lines[id] >> 16) & 0xff,
              g = (lines[id] >> 8) & 0xff,
              b = lines[id] & 0xff,
              t = newH;
          for (int i = 0; i < newH; i++) {
            int A = 0, R = 0, G = 0, B = 0;
            for (int j = 0; j < oldH; j++) {
              A += a;
              R += r;
              G += g;
              B += b;
              t--;
              if (0 == t) {
                id += newW;
                t = newH;
                if (id < newW * oldH) {
                  a = (lines[id] >> 24) & 0xff;
                  r = (lines[id] >> 16) & 0xff;
                  g = (lines[id] >> 8) & 0xff;
                  b = lines[id] & 0xff;
                }
              }
            } /// A???
            columns[i * newW + k] = ( (R / oldH) << 16) | ( (G / oldH) << 8) |
                (B / oldH) | ( (A / oldW) << 24); // A??
          }
        }
      }
      //
      return Image.createRGBImage(columns, newW, newH, false);
  }

  /*
  public static String encodeURL(String url)
  {
   String newurl = "";
   int urllen = url.length();
   for(int i = 0; i < urllen; ++i) {
     char c = url.charAt(i);
     if(((c >= 'a') && (c <= 'z'))
       || ((c >= 'A') && (c <= 'Z'))
        || ((c >= '0') && (c <= '9'))
       || (c == '.') || (c == '-')
       || (c == '*') || (c == '_')
       || (c == '/') || (c == '~')) {
       newurl += c;
     }
     else if(c == ' ') {
       newurl += '+';
     }
     else {
       newurl += encodeChar(c);
     }
   }
   return newurl;
 }

  public static String encodeChar(char c)
  {
    String encchar = "%";
    encchar += Integer.toHexString((c / 16) % 16).toUpperCase();
    encchar += Integer.toHexString(c % 16).toUpperCase();
    return encchar;
  }*/

  public static void wait100()
  {
    try
    {
        Thread.sleep(100L);
    }
    catch(InterruptedException ex)
    {
    }      
  }

  public static Vector getDistanceFromKM(double distance)
  {
    double coeff=1;
    String unit="km";
    switch(MIDlet1.rmsManager.typeUnit)
    {
        case UNIT_ENGLISH:
            // km -> mi
            coeff=Util.KM2MI;
            unit="mi";
            break;
        case UNIT_NAUTICAL:
            // km -> nm
            coeff=Util.KM2NM;
            unit="nm";
            break;
    }
    double d=distance*coeff;
    if(d<1.0)
    {
        switch(MIDlet1.rmsManager.typeUnit)
        {
            case UNIT_ENGLISH:
                // mi -> ft
                d=1000*Util.M2FT*d/Util.KM2MI;
                unit="ft";
                break;
            case UNIT_METRIC:
                // km -> m
                d*=1000;
                unit="m";
                break;
            case UNIT_NAUTICAL:
                // nm -> ft
                d=1000*Util.M2FT*d/Util.KM2NM;
                unit="ft";
                break;
        }
    }
    //
    d=Math.ceil(d*10.0)/10.0;
    //
    Vector v=new Vector();
    v.addElement(new String(unit));
    v.addElement(new Double(d));
    return v;    
  }
  
  public static Vector getPointDistance(Point fromPoint, Point toPoint) 
  {
      if(fromPoint==null || toPoint==null)
          return null;
      //
      double val=Util.distance(fromPoint.Latitude, fromPoint.Longitude, toPoint.Latitude, toPoint.Longitude);
      //
      return getDistanceFromKM(val);
  }
  
  public static double getPointHeading(Point fromPoint, Point toPoint) 
  {
      if(fromPoint==null || toPoint==null)
          return 0.0;
      //
      double val=Math.toDegrees(Util.heading(fromPoint.Latitude, fromPoint.Longitude, toPoint.Latitude, toPoint.Longitude));
      return Math.ceil(val*10.0)/10.0;
  }
  // Расстояние в километрах
  // Скорость в узлах!
  public static String getETA(double distance, double speed) 
  {
      if(speed<1.0)
          return "--:--:--";
      // переводим в км/ч
      double val=speed*Util.KT2KM;
      // время в секундах
      double t=(distance/val)*3600;
      int h=(int)Math.floor(t/3600.0);
      int m=(int)Math.floor((t-h*3600.0)/60.0);
      int s=(int)Math.floor(t-h*3600.0-m*60.0);
      return h+":"+Util.withLeadZero(m)+":"+Util.withLeadZero(s);
  }

    public static String URLencode(String s)
    {
            if (s!=null) {
                    StringBuffer tmp = new StringBuffer();
                    int i=0;
                    try {
                            while (true) {
                                    int b = (int)s.charAt(i++);
                                    if ((b>=0x30 && b<=0x39) || (b>=0x41 && b<=0x5A) || (b>=0x61 && b<=0x7A)) {
                                            tmp.append((char)b);
                                    }
                                    else {
                                            tmp.append("%");
                                            if (b <= 0xf) tmp.append("0");
                                            tmp.append(Integer.toHexString(b));
                                    }
                            }
                    }
                    catch (Exception e) {}
                    return tmp.toString();
            }
            return null;
    }

    public static String readLine(InputStream in) throws IOException 
    {
        // This is not efficient.
        StringBuffer line = new StringBuffer();
        int i;
        while ((i = in.read()) != -1) 
        {
          char c = (char)i;
          if (c == '\n' || c=='\r') 
              break;
          line.append(c);
        }
        if(i==-1) return null;
        return line.toString();
    }
}
