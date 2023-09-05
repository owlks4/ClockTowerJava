package com.clockTower.main;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Hashtable;

import javax.swing.SwingUtilities;

public class KeyInput extends KeyAdapter {
	
	public Hashtable<String,Integer> keyBindings = new Hashtable<String,Integer>();
	public Hashtable<Integer,Boolean> keyHeld = new Hashtable<Integer,Boolean>();
	
	public static Point mousePosition;
	
	public KeyInput() {
		
		//initialise key bindings
		keyBindings.put("CameraLeft", 37);	//left key by default
		keyBindings.put("CameraUp", 38);	//up key by default
		keyBindings.put("CameraRight", 39);	//right key by default
		keyBindings.put("CameraDown", 40);	//down key by default
		
		keyBindings.put("MenuLeft", 37);	//left key by default
		keyBindings.put("MenuUp", 38);		//up key by default
		keyBindings.put("MenuRight", 39);	//right key by default
		keyBindings.put("MenuDown", 40);	//down key by default
		
		keyBindings.put("MenuSelect", 32);	//spacebar by default
		keyBindings.put("MenuSubmit", 10);	//enter key by default
		
		
		
		//initialise the 'is the key current held' booleans
		for (String key : keyBindings.keySet()) {
			keyHeld.put(keyBindings.get(key), false);
		}
	}
	
	public void keyPressed(KeyEvent e) {
	
		int key = e.getKeyCode();
		
		
		keyHeld.put(key, true);
		
		
	}
	
	public void tick() {
		
		//UPDATE MOUSE
		
		if (Game.window.frame.isShowing()) {
			mousePosition = MouseInfo.getPointerInfo().getLocation();
			SwingUtilities.convertPointFromScreen(mousePosition, Game.window.frame.getContentPane());	//convert mouse pos to be relative to game window
			}

		
		//IF KEY IS HELD
		

		boolean ServerStartTemp = keyHeld.get(keyBindings.get("CameraUp"));
		boolean ClientStartTemp = keyHeld.get(keyBindings.get("CameraDown"));
		
		if (ServerStartTemp && ((Game.server == null) || (Game.server != null && !Game.server.running))) {
			loader.StartServer();
		}
		
		if(ClientStartTemp && ((Game.client == null) || (Game.client != null && !Game.client.active))) {
			loader.StartClient();
		}
		
	}
	
	public void keyReleased(KeyEvent e) {
		
		int key = e.getKeyCode();
		
		keyHeld.put(key, false);
	}
}