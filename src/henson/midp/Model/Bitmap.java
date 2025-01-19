package henson.midp.Model;

import javax.microedition.lcdui.*;
import java.io.*;

public class Bitmap
{
    //--- Private constants
    private final static int BITMAPFILEHEADER_SIZE = 14;
    private final static int BITMAPINFOHEADER_SIZE = 40;

    //--- Private variable declaration

    //--- Bitmap file header
    private byte bitmapFileHeader [] = new byte [14];
    private byte bfType [] = {(byte)'B', (byte)'M'};
    private int bfSize = 0;
    private int bfReserved1 = 0;
    private int bfReserved2 = 0;
    private int bfOffBits = BITMAPFILEHEADER_SIZE + BITMAPINFOHEADER_SIZE;

    //--- Bitmap info header
    private byte bitmapInfoHeader [] = new byte [40];
    private int biSize = BITMAPINFOHEADER_SIZE;
    private int biWidth = 0;
    private int biHeight = 0;
    private int biPlanes = 1;
    private int biBitCount = 24;
    private int biCompression = 0;
    private int biSizeImage = 0x030000;
    private int biXPelsPerMeter = 0x0;
    private int biYPelsPerMeter = 0x0;
    private int biClrUsed = 0;
    private int biClrImportant = 0;

    //--- Bitmap raw data
    public int bitmap [];

    public ByteArrayOutputStream baos=null;

    //--- Default constructor
    public Bitmap()
    {
    }

    public void createBitmap(Image image)
    {
      int[] data=new int[image.getWidth()*image.getHeight()];
      image.getRGB(data, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
      this.save(data, image.getWidth(), image.getHeight());
    }

    /*
     * The saveMethod is the main method of the process. This method
     * will call the convertImage method to convert the memory image to
     * a byte array; method writeBitmapFileHeader creates and writes
     * the bitmap file header; writeBitmapInfoHeader creates the
     * information header; and writeBitmap writes the image.
     *
     */
    private void save (int[] imagePix, int parWidth, int parHeight) {

        try
        {
            convertImage (imagePix, parWidth, parHeight);
            baos=new ByteArrayOutputStream();
            writeBitmapFileHeader ();
            writeBitmapInfoHeader ();
            writeBitmap ();
            baos.close();
        }
        catch (Exception saveEx)
        {
            saveEx.printStackTrace ();
        }
    }


    /*
     * convertImage converts the memory image to the bitmap format (BRG).
     * It also computes some information for the bitmap info header.
     *
     */
    private boolean convertImage (int[] imagePix, int parWidth, int parHeight) {

        int pad;
        bitmap = imagePix;
        pad = (4 - ((parWidth * 3) % 4)) * parHeight;
        biSizeImage = ((parWidth * parHeight) * 3) + pad;
        bfSize = biSizeImage + BITMAPFILEHEADER_SIZE +
            BITMAPINFOHEADER_SIZE;
        biWidth = parWidth;
        biHeight = parHeight;

        return (true);
    }

    /*
     * writeBitmap converts the image returned from the pixel grabber to
     * the format required. Remember: scan lines are inverted in
     * a bitmap file!
     *
     * Each scan line must be padded to an even 4-byte boundary.
     */
    private void writeBitmap ()
    {
        int size;
        int value;
        int j;
        int i;
        int rowCount;
        int rowIndex;
        int lastRowIndex;
        int pad;
        int padCount;
        byte rgb [] = new byte [3];

        size = (biWidth * biHeight) - 1;
        pad = 4 - ((biWidth * 3) % 4);

        //The following bug correction will cause the bitmap to be unreadable by
        //GIMP.  It must be there for the bitmap to be readable by most other
        //graphics packages.
        if (pad == 4){ // <==== Bug correction
            pad = 0;
        }// <==== Bug correction


        rowCount = 1;
        padCount = 0;
        rowIndex = size - biWidth;
        lastRowIndex = rowIndex;

        try {
            // The following three lines of code are a correction supplied
            // by Alin Arsu, Feb 2003.  The original code set the top-right
            // pixel in the image to black, and also shifted the bottom row
            // of the image by one pixel.
            // The original code was the following two lines:
            // for (j = 0; j < size; j++) {
            //     value = bitmap [rowIndex];
            // This is replaced by the three lines that appear next.
            for (j = 0; j < size+1; j++) {
                if (j<biWidth) { value = bitmap [rowIndex+1]; }
                else           { value = bitmap [rowIndex]; }

                rgb [0] = (byte) (value & 0xFF);
                rgb [1] = (byte) ((value >> 8) & 0xFF);
                rgb [2] = (byte) ((value >> 16) & 0xFF);
                baos.write(rgb);
                if (rowCount == biWidth) {
                    padCount += pad;
                    for (i = 1; i <= pad; i++)
                    {
                        baos.write (0x00);
                    }
                    rowCount = 1;
                    rowIndex = lastRowIndex - biWidth;
                    lastRowIndex = rowIndex;
                }
                else
                    rowCount++;
                rowIndex++;
            }

                                //--- Update the size of the file
            bfSize += padCount - pad;
            biSizeImage += padCount - pad;
        }
        catch (Exception wb) {
            wb.printStackTrace ();
        }

    }

    /*
     * writeBitmapFileHeader writes the bitmap file header to the file.
     *
     */
    private void writeBitmapFileHeader () {

        try {
            baos.write (bfType);
            baos.write (intToDWord (bfSize));
            baos.write (intToWord (bfReserved1));
            baos.write (intToWord (bfReserved2));
            baos.write (intToDWord (bfOffBits));

        }
        catch (Exception wbfh) {
            wbfh.printStackTrace ();
        }

    }

    /*
     *
     * writeBitmapInfoHeader writes the bitmap information header
     * to the file.
     *
     */

    private void writeBitmapInfoHeader () {

        try {
            baos.write (intToDWord (biSize));
            baos.write (intToDWord (biWidth));
            baos.write (intToDWord (biHeight));
            baos.write (intToWord (biPlanes));
            baos.write (intToWord (biBitCount));
            baos.write (intToDWord (biCompression));
            baos.write (intToDWord (biSizeImage));
            baos.write (intToDWord (biXPelsPerMeter));
            baos.write (intToDWord (biYPelsPerMeter));
            baos.write (intToDWord (biClrUsed));
            baos.write (intToDWord (biClrImportant));
        }
        catch (Exception wbih) {
            wbih.printStackTrace ();
        }

    }


    /*
     *
     * intToWord converts an int to a word, where the return
     * value is stored in a 2-byte array.
     *
     */
    private byte [] intToWord (int parValue) {

        byte retValue [] = new byte [2];

        retValue [0] = (byte) (parValue & 0x00FF);
        retValue [1] = (byte) ((parValue >> 8) & 0x00FF);

        return (retValue);

    }

    /*
     *
     * intToDWord converts an int to a double word, where the return
     * value is stored in a 4-byte array.
     *
     */
    private byte [] intToDWord (int parValue) {

        byte retValue [] = new byte [4];
        retValue [0] = (byte) (parValue & 0x00FF);
        retValue [1] = (byte) ((parValue >> 8) & 0x000000FF);
        retValue [2] = (byte) ((parValue >> 16) & 0x000000FF);
        retValue [3] = (byte) ((parValue >> 24) & 0x000000FF);

        return (retValue);

    }

}
