package com.clockTower.CT;

import java.awt.image.BufferedImage;

public class ClockTowerBufferedImage {
    public ClockTowerBufferedImage(BufferedImage _bufferedImage, short _width, short _height, int _offsetX, int _offsetY) {
    	 width = _width;
         height = _height;
         offsetX = _offsetX;
         offsetY = _offsetY;
         tex = _bufferedImage;
	}

	public ClockTowerBufferedImage() {
	}

	public int width;
    public int height;
    public int offsetX;
    public int offsetY;
    public int offsetInABM;

    public int[] pixels = new int[0];
    
    public BufferedImage tex;
}