package com.clockTower.CT;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import org.apache.commons.io.*;

import com.clockTower.fileUtilities.BitConverter;

    public class CTTileMap       //as seen in .MAP files
    {
        public int width;
        public int height;

        public Tile[] tiles = new Tile[0];

        public class Tile {
            public int tileID;
        }

        public void LoadFromPath(String path)
        {
        	File f = new File(path);
        	
        	 byte[] filebytes = null;
 			try {
 				filebytes = FileUtils.readFileToByteArray(f);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
        	int pos = 0;
        	
                width = BitConverter.ToInt16(filebytes,pos);
                pos+=2;
                height = BitConverter.ToInt16(filebytes,pos);
                pos+=2;

                tiles = new Tile[width * height];

                for (int i = 0; i < width * height; i++)
                    {
                    Tile newTile = new Tile();
                    newTile.tileID = BitConverter.ToUInt16(filebytes,pos);
                    pos+=2;
                    tiles[i] = newTile;
                    }
        }

        public BufferedImage MakeBufferedImageFromTileset(String TilesetPath)
        {
        
        if (tiles.length == 0) {
            JOptionPane.showMessageDialog(null, "That tilemap has no tiles at the moment; did you forget to use LoadFromPath() on it?");
            return null;
            }

        BufferedImage originalBufferedImage = null;
		try {
			originalBufferedImage = ImageIO.read(new File(TilesetPath));
		} catch (IOException e) {
			e.printStackTrace();
		}

        BufferedImage output = new BufferedImage(width*16,height*16, BufferedImage.TRANSLUCENT);

        for (int i = 0; i < tiles.length; i++){

            int topLeftCornerSourceX = 0;
            int topLeftCornerSourceY = 0;

            for (int j = 0; j < tiles[i].tileID; j++) //navigate to the top left corner of the source area
                {
                topLeftCornerSourceX += 16;

                if (topLeftCornerSourceX >= originalBufferedImage.getWidth()){
                    topLeftCornerSourceX = 0;
                    topLeftCornerSourceY += 16;
                    }
                }

            int topLeftCornerDestX = 0;
            int topLeftCornerDestY = 0;

            for (int j = 0; j < i; j++) //navigate to the top left corner of the destination area
            {
                topLeftCornerDestX += 16;

                if (topLeftCornerDestX >= output.getWidth()){
                    topLeftCornerDestX = 0;
                    topLeftCornerDestY += 16;
                }
            }

            for (int y = 0; y < 16; y++)        //now copy the tile
                {
                for (int x = 0; x < 16; x++)    
                    {
                    output.setRGB(topLeftCornerDestX + x, topLeftCornerDestY + y, originalBufferedImage.getRGB(topLeftCornerSourceX + x, topLeftCornerSourceY + y));
                    }                
                }
            }
        return output;
    }
    }
