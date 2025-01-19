/*
 * Category.java
 *
 * Created on 22 јпрель 2006 г., 8:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package henson.midp.Model;

import javax.microedition.lcdui.Image;

/**
 *
 * @author Administrator
 */
public class Category 
{
    public String name="";
    public Image image=null;
    public String palette="";
    public int x;
    public int y;
    /** Creates a new instance of Category */
    public Category(String name, Image image, int offsetx, String palette, int x, int y) 
    {
        this.name=name;
        if(image!=null)
        {
            int[] bigimage=new int[256];
            image.getRGB(bigimage, 0, 16, offsetx, 0, 16, 16);
            this.image=Image.createRGBImage(bigimage, 16, 16, true);
        }
        this.palette=palette;
        this.x=x;
        this.y=y;
    }
    
    public String getKML()
    {
      StringBuffer sb=new StringBuffer();
      sb.append("<styleUrl>root://styleMaps#default+nicon=0x307+hicon=0x318</styleUrl>");
      sb.append("<Style><IconStyle><Icon><href>root://icons/"+palette+"</href>");
      sb.append("<x>"+x+"</x><y>"+y+"</y><w>32</w><h>32</h>");
      sb.append("</Icon></IconStyle></Style>");
      return sb.toString();
    }
}
