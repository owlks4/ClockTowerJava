package com.clockTower.CT;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.apache.commons.io.*;

import com.clockTower.Utility.Utility;
import com.clockTower.fileUtilities.BitConverter;
import com.clockTower.main.Game;


public class CTAnimatedBitmap //or ABM
{
    int size;

    boolean isPSX = false;
    
    int numBufferedImagePlanes;
    short BPP; //bits per pixel; usually 8
    CompressionMethod compressionMethod;

    public LinkedList<ClockTowerBufferedImage> bufferedImages = new LinkedList<ClockTowerBufferedImage>();

    public enum CompressionMethod { 
    COMPRESSION_NONE()
    }

    public void LoadFromPath(String path) {

    	File f  = new File(path);

        byte[] filebytes = null;
		try {
			filebytes = FileUtils.readFileToByteArray(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        int pos = 0;
    	
            int magic = BitConverter.ToUInt16(filebytes,pos); //skip magic ("AB")
            pos+=2; //ignore the fact that it's stored as an int, it's a ushort in reality, so +=2 is correct.
            
            
            if (magic != 0x4241) //"AB"
                {
                JOptionPane.showMessageDialog(null, "'" + path + "' is not a valid ABM file; it did not have the correct file magic ('AB').");
                return;
                }

            size = BitConverter.ToInt32(filebytes,pos);
            pos+=4;

            pos+=4;    //skip reserved

            int startOfBufferedImageData = BitConverter.ToInt32(filebytes,pos);
            pos+=4;

            //DIB header

            pos+=4;
            numBufferedImagePlanes = BitConverter.ToUInt16(filebytes,pos);
            pos+=2;
            
            BPP =  BitConverter.ToInt16(filebytes,pos);
            pos+=2;
            
            int compressionMethodAsNumber = BitConverter.ToInt32(filebytes,pos);
            pos += 4;
            
            switch (compressionMethodAsNumber) {
            
            case 0:
            	compressionMethod = CompressionMethod.COMPRESSION_NONE;
            	break;
            default:
            	 JOptionPane.showMessageDialog(null, "Unknown compression method "+compressionMethodAsNumber);
            	break;
            }

            int numBufferedImages = BitConverter.ToUInt16(filebytes,pos);
            pos+=2;  //ignore the fact that it's stored as an int, it's a ushort in reality, so +=2 is correct.
            
            bufferedImages = new LinkedList<ClockTowerBufferedImage>();

            for (int i = 0; i < numBufferedImages; i++){   //read the metadata for each BufferedImage
                ClockTowerBufferedImage newBufferedImage = new ClockTowerBufferedImage();

                newBufferedImage.width = BitConverter.ToUInt16(filebytes,pos); pos+=2;
                newBufferedImage.height = BitConverter.ToUInt16(filebytes,pos); pos+=2;
                newBufferedImage.offsetX = BitConverter.ToInt16(filebytes,pos); pos+=2;
                newBufferedImage.offsetY = BitConverter.ToInt16(filebytes,pos); pos+=2;
                newBufferedImage.offsetInABM = BitConverter.ToInt32(filebytes,pos); pos+=4;

                bufferedImages.add(newBufferedImage);
                }

            for (int i = 0; i < numBufferedImages; i++) //now read the actual BufferedImage data for each BufferedImage
                {
                pos = startOfBufferedImageData + bufferedImages.get(i).offsetInABM;

                bufferedImages.get(i).pixels = new int[bufferedImages.get(i).width * bufferedImages.get(i).height];

                for (int p = bufferedImages.get(i).pixels.length - 1; p >= 0; p--){ //write the data into the array backwards
                    bufferedImages.get(i).pixels[p] = filebytes[pos]; pos++;
                    }
                }
            
        }
    
    public void LoadTMCFromPath(String path)  //load from PSX TIM collection instead
    	{
    	isPSX = true;
    	
	    path = FilenameUtils.concat(Game.GAME_ROOT_DIR, path);
	
	    byte[] filebytes = null;
		int pos = 0;
		
		try {
			filebytes = FileUtils.readFileToByteArray((new File(path)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	        if (BitConverter.ToInt16(filebytes, pos) != 0x4D54) //   "TM�."
	            {
	            JOptionPane.showMessageDialog(null, path + " is not a valid TIM file (file magic did not match).");
	            return;
	            }
	        
	        pos+=2;
	
	        bufferedImages = new LinkedList<ClockTowerBufferedImage>();
	
	        int numbufferedImages = BitConverter.ToUInt16(filebytes, pos); pos+=2;
	
	        int totalsize = BitConverter.ToInt32(filebytes, pos); pos+=4;
	
	        int offset_of_bufferedImage_data = BitConverter.ToInt32(filebytes, pos); pos+=4;
	        int size_of_bufferedImage_data = BitConverter.ToInt32(filebytes, pos); pos+=4;
	
	        int offset_of_UNK_section_1 = BitConverter.ToInt32(filebytes, pos); pos+=4;
	        int size_of_UNK_section_1 = BitConverter.ToInt32(filebytes, pos); pos+=4;
	
	        int offset_of_UNK_section_2 = BitConverter.ToInt32(filebytes, pos); pos+=4;
	        int size_of_UNK_section_2 = BitConverter.ToInt32(filebytes, pos); pos+=4;
	
	        int[] startPositionsInBufferedImageData = new int[numbufferedImages];
	        int[] dataSizes = new int[numbufferedImages];
	        
	        for (int i = 0; i < numbufferedImages; i++)
	        {
	            ClockTowerBufferedImage newFrame = new ClockTowerBufferedImage();
	            newFrame.width = BitConverter.ToUInt16(filebytes, pos); pos+=2;
	            newFrame.height = BitConverter.ToUInt16(filebytes, pos); pos+=2;
	            newFrame.offsetX = BitConverter.ToUInt16(filebytes, pos); pos+=2;
	            newFrame.offsetY = BitConverter.ToUInt16(filebytes, pos); pos+=2;
	
	            startPositionsInBufferedImageData[i] = BitConverter.ToInt32(filebytes, pos); pos+=4;
	            dataSizes[i] = BitConverter.ToInt32(filebytes, pos); pos+=4;
	
	            bufferedImages.add(newFrame);
	        }
	
	        //palette
	        
	        Color[] palette = new Color[256];
	
	        for (int i = 0; i < 256; i++)
	        {
	            palette[i] = Utility.GetRGB555Colour(BitConverter.ToInt16(filebytes,pos)); pos+=2;
	        }
	    
	        //bufferedImage data
	        
	        for(int i = 0; i < bufferedImages.size(); i++)
	        {
	        	ClockTowerBufferedImage img = bufferedImages.get(i);
	        	img.pixels = new int[dataSizes[i]];
	        	
	        	for (int p = 0; p < dataSizes[i]; p++) {
	        		img.pixels[p] = filebytes[offset_of_bufferedImage_data + startPositionsInBufferedImageData[i] + p];
	        	}
	        }
	        
	        UpdateBufferedImages(palette);								
	    }

        public void UpdateBufferedImages(Color[] palette) {

        	if (isPSX) {
        		for (ClockTowerBufferedImage target : bufferedImages)
    	        {
        		   int pos = 0;

    	           target.tex = new BufferedImage(target.width, target.height, BufferedImage.TRANSLUCENT);
    	           
    	            for (int y = 0; y < target.height; y++)
    	            {
    	                for (int x = 0; x < target.width; x++)
    	                {
    	                    short potentialPixel = (short)target.pixels[pos]; pos++;
    	
    	                    if (potentialPixel == 0x00){            //a value of 0x00 indicates that we are going to pad with transparency for a number of bytes. The number is given in the byte directly following the 0x00.
    	                        int padAmount = target.pixels[pos]; pos++;
    	                        for (int pad = 0; pad < padAmount; pad++){
    	                            target.tex.setRGB(x, y, palette[potentialPixel].getRGB());
    	
    	                            if (pad < padAmount - 1) { x++; }
    	
    	                            if (x >= target.width){
    	                                y++;
    	                                x = 0;
    	                            }
    	                        }
    	                    }
    	                    else            //otherwise, just get the corresponding colour from the palette
    	                    {
    	                        target.tex.setRGB(x, y, palette[potentialPixel].getRGB());
    	                    }
    	                }
    	            }
    	        }	
        	}
        	else {
        		
	            for (ClockTowerBufferedImage frame : bufferedImages)
	            {
	                frame.tex = new BufferedImage(frame.width,frame.height,BufferedImage.TRANSLUCENT);
	
	                for (int y = 0; y < frame.height; y++)
	                    {
	                    for (int x = 0; x < frame.width; x++)
	                        {			
	                        if (palette == null || palette.length == 0)
	                            {
	                            int greyLevel = (frame.pixels[(frame.width * y) + x]);
	                            frame.tex.setRGB(((frame.width - 1 ) - x), y, new Color(greyLevel, greyLevel, greyLevel, 255).getRGB());
	                            }
	                        else
	                            {
	                            frame.tex.setRGB(((frame.width - 1 ) - x), y, palette[frame.pixels[(frame.width * y) + x]&0xFF].getRGB());
	                            }
	                        }
	                    }
	            }      
        	}
        }
}