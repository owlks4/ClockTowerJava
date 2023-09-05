package com.clockTower.fileUtilities.PSXfiletypes;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.clockTower.Utility.Utility;
import com.clockTower.fileUtilities.BitConverter;
import com.clockTower.main.Game;

    public class TIMimage
    {
        short BPP = 0;

        short activePaletteIndex = 0;

        public BufferedImage BufferedImage = null;

        public void LoadFromPath(String path, int paletteIndex)
        {
            path = FilenameUtils.concat(Game.GAME_ROOT_DIR, path);

            byte[] filebytes = null;
    		int pos = 0;
    		
    		try {
    			filebytes = FileUtils.readFileToByteArray((new File(path)));
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
            
                if (BitConverter.ToInt32(filebytes, pos) != 0x10)
                {
                    JOptionPane.showMessageDialog(null, path + " is not a valid TIM file! (File magic was not correct)");
                    return;
                }

                pos += 4;
                
                int version = BitConverter.ToInt32(filebytes, pos); pos += 4;

                boolean has_palette = false;

                switch (version & 0x07)
                {
                    case 0: //4BPP
                        BPP = 4;
                        break;
                    case 1: //8BPP
                        BPP = 8;
                        break;
                    case 2: //16BPP
                        BPP = 16; 
                        break;
                    case 3: //24BPP
                        BPP = 24;
                        break;
                    case 4: //MIXED FORMAT
                        BPP = 0xFF;
                        break;
                }

                if ((version & 0x08) == 0x08)
                {
                    has_palette = true;
                }

                LinkedList<Color[]> palettes = new LinkedList<Color[]>();

                if (has_palette)
                {
                    int paletteSizePlus12 = BitConverter.ToInt32(filebytes, pos); pos+=4;

                    short paletteOrgX = BitConverter.ToInt16(filebytes, pos); pos+=2;
                    short paletteOrgY = BitConverter.ToInt16(filebytes, pos); pos+=2;

                    int numColoursInPalette = BitConverter.ToUInt16(filebytes, pos); pos+=2;
                    int numPalettes = BitConverter.ToUInt16(filebytes, pos); pos+=2;

                    while (paletteIndex >= numPalettes)
                    {
                        paletteIndex--;
                    }

                    activePaletteIndex = (byte)paletteIndex;

                    for (int i = 0; i < numPalettes; i++)
                    {
                        Color[] newPalette = new Color[numColoursInPalette];

                        for (int j = 0; j < numColoursInPalette; j++)
                        {
                            newPalette[j] = Utility.GetRGB555Colour(BitConverter.ToInt16(filebytes, pos));
                            pos+=2;
                        }

                        palettes.add(newPalette);
                    }
                }

                int BufferedImageDataSizePlus12 = BitConverter.ToInt32(filebytes, pos); pos+=4;

                short BufferedImageOrgX = BitConverter.ToInt16(filebytes, pos); pos+=2;
                short BufferedImageOrgY = BitConverter.ToInt16(filebytes, pos); pos+=2;

                int trueBufferedImageWidth = BitConverter.ToUInt16(filebytes, pos); pos+=2;   //the BufferedImage width if you wrote it out with 16BPP colours
                int BufferedImageHeight = BitConverter.ToUInt16(filebytes, pos); pos+=2;

                //BufferedImage data follows

                int widthInPixels = 0;

                BufferedImage = new BufferedImage(1,1,Transparency.TRANSLUCENT);

                switch (BPP) {
                    case 4:
                        widthInPixels = trueBufferedImageWidth * 4;
                        BufferedImage = new BufferedImage(widthInPixels, BufferedImageHeight,Transparency.TRANSLUCENT);

                        for (int y = 0; y < BufferedImageHeight; y++)
                        {
                            for (int x = 0; x < widthInPixels; x++)
                            {
                                BufferedImage.setRGB(x,y,palettes.get(activePaletteIndex)[filebytes[pos] & 0x0F].getRGB());
                                pos++;
                                x++;
                                pos--;
                                BufferedImage.setRGB(x, y, palettes.get(activePaletteIndex)[filebytes[pos] >>> 4].getRGB());
                                pos++;
                            }
                        }
                        break;
                    case 8:
                        widthInPixels = trueBufferedImageWidth * 2;
                        BufferedImage = new BufferedImage(widthInPixels, BufferedImageHeight, Transparency.TRANSLUCENT);
                        for (int y = 0; y < BufferedImageHeight; y++)
                        {
                            for (int x = 0; x < widthInPixels; x++)
                            {
                                BufferedImage.setRGB(x, y, palettes.get(activePaletteIndex)[filebytes[pos]].getRGB());
                                pos++;
                            }
                        }
                        break;
                    case 16:
                        widthInPixels = trueBufferedImageWidth;
                        BufferedImage = new BufferedImage(widthInPixels, BufferedImageHeight,Transparency.TRANSLUCENT);
                        for (int y = 0; y < BufferedImageHeight; y++)
                        {
                            for (int x = 0; x < widthInPixels; x++)
                            {
                                BufferedImage.setRGB(x, y, Utility.GetRGB555Colour(BitConverter.ToInt16(filebytes,pos)).getRGB());
                                pos+=2;
                            }
                        }
                        break;
                    case 24:
                        widthInPixels = Math.round((float)trueBufferedImageWidth / 1.5f);
                        BufferedImage = new BufferedImage(widthInPixels, BufferedImageHeight,Transparency.TRANSLUCENT);
                        for (int y = 0; y < BufferedImageHeight; y++)
                        {
                            for (int x = 0; x < widthInPixels; x++)
                            {
                                byte R = filebytes[pos]; pos++;
                                byte G = filebytes[pos]; pos++;
                                byte B = filebytes[pos]; pos++;

                                BufferedImage.setRGB(x, y, new Color(R,G,B,255).getRGB());
                            }
                        }
                        break;
                    }
        }
    }