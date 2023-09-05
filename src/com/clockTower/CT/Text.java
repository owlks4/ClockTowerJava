package com.clockTower.CT;

import java.awt.Color;

import com.clockTower.Utility.Transform;

public class Text{
	Transform transform = new Transform();
	Color color;
	String message; //the message to be displayed
	String text; //the current state of display of the message (e.g. may be halfway complete)
	boolean displayingMessage = false;
	public int currentPosInText;
	public long nextLetterAppearTime;
	public short speed;
	public Color highlight;
	
	public void destroy() {
		
	}
}