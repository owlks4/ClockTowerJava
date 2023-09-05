package com.clockTower.main;

import java.awt.Color;

import java.awt.Graphics;
import java.awt.Point;
import java.io.File;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.apache.commons.io.FilenameUtils;

import com.clockTower.fileUtilities.ADOScript;
import com.clockTower.fileUtilities.MidiStream;
import com.clockTower.fileUtilities.PTNfile;
import com.clockTower.fileUtilities.PYXfile;
import com.clockTower.online.Client;
import com.clockTower.online.Server;
import com.clockTower.CT.*;
import com.clockTower.Utility.MIDIPlayer;
import com.clockTower.Utility.Utility;
 
public class loader
	{
	public static ADOScript mainADO;
	
	public static LinkedList<returnPoint> returnStack = new LinkedList<returnPoint>();
	public static LinkedList<conditional> conditionalStack = new LinkedList<conditional>();
	
	public static LinkedList<CTBackground> backgroundSlots = new LinkedList<CTBackground>();	//where backgrounds live until they are spawned

	public static LinkedList<CTSprite> backgroundSpriteSlots = new LinkedList<CTSprite>();		//backgrounds spawned as sprites
	public static LinkedList<CTSprite> spriteSlots = new LinkedList<CTSprite>();				//sprites
	
	public static LinkedList<CTSprite> backgroundMaps = new LinkedList<CTSprite>();            //for backgrounds loaded with MAPLOAD
	
	public static LinkedList<CTEvent> events = new LinkedList<CTEvent>();
	
	public static LinkedList<CTAudio> soundEffects = new LinkedList<CTAudio>();
	
	public static LinkedList<Color[]> palettes = new LinkedList<Color[]>(); 
	public static short activePalette = 0;

	public static MidiStream music;
	
	public static LinkedList<PTNfile> PTNfiles = new LinkedList<PTNfile>();
	public static LinkedList<PYXfile> PYXfiles = new LinkedList<PYXfile>();
	
	public static boolean BGwaiting = false;

	public static LinkedList<CTMark> marks = new LinkedList<CTMark>();

	static CTSprite cursor;

	static boolean faWaitCarriesOver = false;
	
	static byte[] midiMuter = new byte[]{77, 84, 104, 100, 0, 0, 0, 6, 0, 1, 0, 1, 1, (byte) 224, 77, 84, 114, 107, 0, 0, 0, 58, 0, (byte) 255, 88, 4, 4, 2, 24, 8, 0, (byte) 255, 89, 2, 0, 0, 0, (byte) 255, 81, 3, 7, (byte) 161, 32, 0, (byte) 176, 121, 0, 0, (byte) 192, 0, 0, (byte) 176, 7, 0, 0, 10, 64, 0, 91, 0, 0, 93, 0, 0, (byte) 255, 33, 1, 0, 0, (byte) 144, 64, 80, (byte) 129, 99, 64, 0, 1, (byte) 255, 47, 0};
	
	public static LinkedList<MapScrollInstance> currentMapScrolls = new LinkedList<MapScrollInstance>();
	
	public static int previousBGAnimIndex = -1; //so that it can be told to stop animating when the new one starts
	
	
	
	public enum ConditionalType{
	IF,
	WHILE
	}
	
	//these three are for getting memory values from the inspector:
	public static int bankToGetFrom;
	public static short valueToGetInMemory;
	public static boolean PrintMemoryValue;
	
	
	public static boolean mouseUpWaiting;
	public static long mouseUpStartTime;

	public static Thread currentADOThread;

	public static CTSprite player;
	
	
	public static void tick(){
		if (PrintMemoryValue){
				PrintMemoryValue = false;
				System.out.println(CTMemory.EvaluateInt16Bank((short)((bankToGetFrom << 12) + valueToGetInMemory)));
			}
			
		if (CTMemory.userControl && cursor != null){
			cursor.myTransform.position = KeyInput.mousePosition;
		}

		if (mouseUpWaiting && System.currentTimeMillis() < mouseUpStartTime + 500) {
			mouseUpWaiting = false;
			CTMemory.SetVar((short)-4096, (short)0);	//0xF000
		}
		
		
		//TELLING SPRITES ETC TO TICK
		
		for (int i = 0; i < backgroundMaps.size(); i++) {
			CTSprite bg = backgroundMaps.get(i);
			if (bg != null) {
				bg.tick();}
		}
				
		for (int i = 0; i < spriteSlots.size(); i++) {
			CTSprite spr = spriteSlots.get(i);
			if (spr != null) {
				spr.tick();}
		}

		for (int i = 0; i < backgroundSpriteSlots.size(); i++) {
			CTSprite bg = backgroundSpriteSlots.get(i);
			if (bg != null) {
				bg.tick();}
		}
		//MAPSCROLL STUFF
		
		for (int m = currentMapScrolls.size() - 1; m >= 0; m--) {
			
			MapScrollInstance scroll = currentMapScrolls.get(m);
			
			BGwaiting = true;
			
			if(System.currentTimeMillis() < scroll.startTime + scroll.scrollTime){
				Camera.transform.position.x = Math.round(Utility.Lerp(scroll.startingPos.x,scroll.endingPos.x,(float)(System.currentTimeMillis()-scroll.startTime)/(scroll.scrollTime)));
				Camera.transform.position.y = Math.round(Utility.Lerp(scroll.startingPos.y,scroll.endingPos.y,(float)(System.currentTimeMillis()-scroll.startTime)/(scroll.scrollTime)));
			}
			else {
				currentMapScrolls.remove(m);
				BGwaiting = false;
			}
		}

		//END MAPSCROLL STUFF
		
		if (currentADOThread != null) {
			synchronized(currentADOThread) {
			currentADOThread.notify();
			}
		}
	}
	
	public static void render (Graphics g, Camera cam) {
		
		for (int i = 0; i < backgroundMaps.size(); i++) {
			CTSprite bg = backgroundMaps.get(i);
			if (bg != null) {
				bg.render(g, cam);}
		}
				
		for (int i = 0; i < spriteSlots.size(); i++) {
			CTSprite spr = spriteSlots.get(i);
			if (spr != null) {
				spr.render(g, cam);}
		}
		
		g.setColor(Game.backgroundColor);
		g.fillRect(0, 0, Game.GetAdjustedDimension(CTUI.leftBorderPos), Game.HEIGHT);
		g.fillRect(0, 0, Game.WIDTH, Game.GetAdjustedDimension(CTUI.topBorderPos));
		g.fillRect(0, Game.GetAdjustedDimension(CTUI.lowerBorderPos), Game.WIDTH, Game.HEIGHT);
		g.fillRect(Game.GetAdjustedDimension(CTUI.rightBorderPos), 0, Game.WIDTH, Game.HEIGHT);
	
		
		for (int i = 0; i < backgroundSpriteSlots.size(); i++) {
			CTSprite bg = backgroundSpriteSlots.get(i);
			if (bg != null) {
				bg.render(g, cam);}
		}
		
		//cursor.render(g, cam);
	}
	
	public static void click() {
		
			CTMemory.SetVar((short)-4096, (short)1);	//0xF000
			
			if (CTMemory.userControl) {
				
				if (player != null) {
					
					//player walk to clicked position

					short dirAtEnd = 1;
					
					int destXPos = (KeyInput.mousePosition.x + Camera.transform.position.x) * (Game.DEFAULT_HEIGHT / Game.HEIGHT);
					
					if (player.myTransform.position.x > destXPos) {
						dirAtEnd = -1;
					}
					
					int animIndex = 0;
					
					//only initiate the walk if the player is not already walking, but make an exception for when they're already walking and want to change direction
					if (!player.busy || (player.busy && player.myTransform.flipping.x != dirAtEnd)) {
						player.sprWalkX(destXPos, destXPos, dirAtEnd, (short)0);
					}
				}
			}
	}
	
	public static void releaseClick(){
			mouseUpStartTime = System.currentTimeMillis();
			mouseUpWaiting = true;
	}
	
	public void OnApplicationQuit(){
		music.Stop();
		MIDIPlayer.sequencer.close();
        MIDIPlayer.sequencer = null;
	}

	public static void Start(){
		
		Game.FRAME_RATE = 60;
		music = new MidiStream();
		music.filebytes = midiMuter;	//sets all channels to note off
		music.SetVolume(33000);
		music.Play(false);
		music.Stop();
		
		mainADO = new ADOScript();
		
		if (!new File(Game.GAME_ROOT_DIR).exists()) {
			JOptionPane.showMessageDialog(null, "You need to add a folder called 'game' next to the jar file,\ncontaining the Clock Tower for Windows 95 game files!"); 
			return;
		}
		
		mainADO.LoadFromPath(FilenameUtils.concat(Game.GAME_ROOT_DIR,"SCE/CT.ADO"));					//Load the ADO
		
		Thread.currentThread().setPriority(10);
		
		File d = new File(FilenameUtils.concat(Game.GAME_ROOT_DIR,"DATA"));
		
		for (File f : Utility.GetFilesRecursive(d,".ptn"))
			{	//Load all PTN files so they can be referenced later
				PTNfile newPTN = new PTNfile();
				newPTN.LoadFromPath(f.getAbsolutePath());
				PTNfiles.add(newPTN);
			}
		
		for (File f : Utility.GetFilesRecursive(d,".pyx"))
		{	//Load all PYX files so they can be referenced later
			PYXfile newPYX = new PYXfile();
			newPYX.LoadFromPath(f.getAbsolutePath());
			PYXfiles.add(newPYX);
		}
			
		
		//27 is game start
		//28 is quick start
				
		ExecuteADOFunction executeADO = new ExecuteADOFunction(mainADO,(short)28,0);
		executeADO.start();	//Start executing the ADO instructions
		}
	
		public static void StartServer() {
			if (Game.server == null) {
				Game.server = new Server(9500);
				Game.server.start();
				}
		}
		
		public static void StartClient() {
			if (Game.isServer) {
				System.out.println("You can't connect as a client because you are already the host!");
				return;
			}
			
			if (Game.client == null) {
				Game.client = new Client("localhost",9500,"NewPlayer");
				}
		}
		
		public static void InitialiseCursor(){
				int targetSlot = 995;
				short X =  0;
				short Y =  0;
				short layer =  90;
				int animationIndex = 0;
				int animGroupNumber = 53;
				
				boolean animateOrNot = true;
				short startingFrame =  0;

				cursor =  new CTSprite();

				CTAnimation newAnimation = new CTAnimation (animGroupNumber,animationIndex,startingFrame);
				cursor.LoadSpriteDataIntoSprite(newAnimation);
				cursor.mySlotID = targetSlot;
				cursor.layer = layer;
				
				cursor.myTransform.position = new Point(X,Y);
				cursor.isSprite = true;
				cursor.loop = true;
				
				if (animateOrNot){
					cursor.numLoops = 1;
				}else{
					cursor.numLoops = 0;	//and now we make sure it gets the correct offset, because it will never go through the loop and get it the regular way.
					cursor.initializeNonAnimatingSprite();
					}
		}

	public static void AddMapScroll(short targetPosX,short targetPosY,short scrollSpeed){
		loader.BGwaiting = true;
		currentMapScrolls.add(new MapScrollInstance(targetPosX,targetPosY,scrollSpeed));
		}
	}