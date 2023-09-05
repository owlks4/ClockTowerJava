package com.clockTower.online;

import java.awt.image.BufferedImage;

import com.clockTower.CT.CTSprite;

public class ClientInfo {

	public int ID;
	public String name;
	public BufferedImage avatar = null;
	public CTSprite myPlayer = null;
	
	public boolean receivedMessage = false;
	
	public ClientInfo(int _ID, String _name) {
		ID = _ID;
		name = _name;
	}
}