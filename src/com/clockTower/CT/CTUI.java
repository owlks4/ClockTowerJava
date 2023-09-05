package com.clockTower.CT;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.LinkedList;

import com.clockTower.main.Camera;
import com.clockTower.main.Game;
import com.clockTower.main.Window;
import com.clockTower.main.loader;
 
public class CTUI
	{		
		//TEXT STUFF:
		
			public static short messageSpeed;
			public static Color textColour = Color.white;
			public static Color highlightColour = Color.black;
		
			public static short topLeftX;
			public static short topLeftY;
			public static short bottomRightX;
			public static short bottomRightY;
			public static short unk1;
			
			public static short unk2;
			public static short unk3;
			public static short unk4;
			public static short unk5;
			public static short unk6;
			public static short unk7;
			public static short unk8;
			public static short unk9;
			
			public static LinkedList<Text> currentlyDisplayedTexts = new LinkedList<Text>();
			
			public static boolean displayingAnyMessage;
			
			public static boolean skipToEnd;
			
			public static int leftBorderPos = 100;	//not including the additional black bars caused by custom resolutions
			public static int rightBorderPos = Game.DEFAULT_WIDTH - leftBorderPos;
			public static int topBorderPos = 32;
			public static int lowerBorderPos = 304;
			
		//FADE STUFF:

			static FadeType fadeType;
			static int fadeSpeed = 1;
			static boolean firstHalfOfClearScreenDone;
		
		
		//BORDER STUFF:
		
		public static void SetBorder(float _rightBorderPos, float _lowerBorderPos){		//not including the additional black bars caused by custom resolutions
			rightBorderPos = leftBorderPos + Math.round(_rightBorderPos);
			lowerBorderPos = Math.round(_lowerBorderPos);
		}
		

		public static void ShowMessage(String _message){
		
			Text currentText = new Text();
			
			currentText.message = _message;
			
			currentText.transform.position = new Point(topLeftX, topLeftY);
			currentText.color = textColour;
			currentText.highlight = highlightColour;
			currentText.speed = messageSpeed;
			currentText.text = "";
			
			currentText.nextLetterAppearTime = System.currentTimeMillis();
			currentText.currentPosInText = 0;
			
			currentText.displayingMessage = true;
			displayingAnyMessage = true;
			currentlyDisplayedTexts.add(currentText);
		}
		
		public static void tick(){
			
			
			//TEXT DISPLAY START
			
			int numTextsDisplaying = 0;
			for (int i = 0; i < currentlyDisplayedTexts.size(); i++) {
				Text currentText = currentlyDisplayedTexts.get(i);
				if (currentText.displayingMessage){
					numTextsDisplaying++;
					displayingAnyMessage = true;

					if (skipToEnd){	//print the message all at once and do not do any further displaying
						skipToEnd = false;
						currentText.text = currentText.message;
						currentText.displayingMessage = false;
					}
					else if (System.currentTimeMillis() > currentText.nextLetterAppearTime) {
						if (currentText.message.charAt(currentText.currentPosInText) != '\n'){ //don't wait during a line break char
							currentText.nextLetterAppearTime = System.currentTimeMillis() + (currentText.speed*30); //messageSpeed is probably in frames or something, but oh well
						}
						
						currentText.text += currentText.message.charAt(currentText.currentPosInText);
						currentText.currentPosInText++;
						
						if (currentText.currentPosInText >= currentText.message.length()) {
							currentText.displayingMessage = false;
						}
					}
				}
			}
			
			if (numTextsDisplaying == 0) {
				displayingAnyMessage = false;
			}
			
			//TEXT DISPLAY END
		}
		
		public static void drawText(Text currentText, Graphics g, Camera cam) {
			
			String[] lines = currentText.text.split("\n");
			
			int x = topLeftX; int y = topLeftY;
			
			g.setColor(currentText.color);
			
			int fontHeight = g.getFontMetrics().getHeight();
			
			for (int l = 0; l < lines.length; l++) {
		        g.drawString(lines[l], Window.blackBarSize + Game.GetAdjustedDimension(x),Game.GetAdjustedDimension(y) + ((l + 1) * fontHeight));
				}
		}
		
		public static void render(Graphics g, Camera cam) {
				
			//TEXT DISPLAY START
			
			for (int i = 0; i < currentlyDisplayedTexts.size(); i++) {
				drawText(currentlyDisplayedTexts.get(i),g,cam);
				}
			
			//TEXT DISPLAY END
			
	//FADE PROCESSING START
			
			int curAlpha = Game.fadeColor.getAlpha();

			if(CTMemory.currentlyFading) {
				
				switch (fadeType) {
					case In:
						if(curAlpha > 0){
							Game.fadeColor = new Color(Game.fadeColor.getRed(),Game.fadeColor.getGreen(),Game.fadeColor.getBlue(),curAlpha -= fadeSpeed);
							}
						else {
							CTMemory.currentlyFading = false;
							}
						break;
					case ClearScreen:
						if (firstHalfOfClearScreenDone) {
							if(curAlpha > 0){
								Game.fadeColor = new Color(Game.fadeColor.getRed(),Game.fadeColor.getGreen(),Game.fadeColor.getBlue(),curAlpha -= fadeSpeed);
								}
							else {
								CTMemory.currentlyFading = false;
								}
						} else {	
							if(curAlpha < 255){
								Game.fadeColor = new Color(Game.fadeColor.getRed(),Game.fadeColor.getGreen(),Game.fadeColor.getBlue(),curAlpha += fadeSpeed);
								}
							else {
								for (int i = loader.spriteSlots.size() - 1; i >= 0; i--){
									if (loader.spriteSlots.get(i).markForDeletion && !loader.spriteSlots.get(i).immuneToDeletion){
										loader.spriteSlots.remove(i);}
									}
								for (int i = loader.backgroundSpriteSlots.size() - 1; i >= 0; i--){
									if (loader.backgroundSpriteSlots.get(i).markForDeletion){
										loader.backgroundSpriteSlots.remove(i);
										}
									}
								firstHalfOfClearScreenDone = true;
								}
						}
						break;
					case Out:
						if(curAlpha < 255){
							Game.fadeColor = new Color(Game.fadeColor.getRed(),Game.fadeColor.getGreen(),Game.fadeColor.getBlue(),curAlpha += fadeSpeed);
							}
						else {
							CTMemory.currentlyFading = false;
							}
						break;
					}
			}
			
			if (Game.fadeColor.getAlpha() > 0) {
				g.setColor(Game.fadeColor);
				g.fillRect(0, 0, Game.WIDTH, Game.HEIGHT);
				}				

			//FADE PROCESSING END
			
			}
		
		public static void ClearMessage(){
			
			for (int i = currentlyDisplayedTexts.size() - 1; i >= 0; i--){
				
				Text currentText = currentlyDisplayedTexts.get(i);
				currentText.color = Game.backgroundColor;
				currentText.highlight = Game.backgroundColor;
				drawText(currentText,Game.g,null);	//draw background-coloured text over the text to erase it. This is a really hacky solution but I don't want to have to blank the screen at the start of every frame because some things (like bganim) are supposed to hang around even once they have been kicked out of the animation renderer
				
				currentlyDisplayedTexts.get(i).displayingMessage = false;
				currentlyDisplayedTexts.remove(i);
			}
			currentlyDisplayedTexts = new LinkedList<Text>();
		}
		
		
		public enum FadeType{
			In,
			Out,
			ClearScreen
        }
			
		
		public static void EnactFade(int _fadeType){	//0 fadein, 1 fadeout, 2 screenclr
			
			switch (_fadeType) {
			case 0:
				fadeType = FadeType.In;
				Game.fadeColor = new Color(Game.fadeColor.getRed(),Game.fadeColor.getGreen(),Game.fadeColor.getBlue(),255);
				break;
			case 1:
				fadeType = FadeType.Out;
				Game.fadeColor = new Color(Game.fadeColor.getRed(),Game.fadeColor.getGreen(),Game.fadeColor.getBlue(),0);
				break;
			case 2:
				firstHalfOfClearScreenDone = false;
				fadeType = FadeType.ClearScreen;
				
				//mark current sprites etc for deletion if required
				for (int i = loader.spriteSlots.size() - 1; i >= 0; i--){
						loader.spriteSlots.get(i).markForDeletion = true;
						}
					for (int i = loader.backgroundSpriteSlots.size() - 1; i >= 0; i--){
						loader.backgroundSpriteSlots.get(i).markForDeletion = true;
						} 
				break;
			default:
				System.out.println("Unknown fade type "+_fadeType);
				break;
			}
			
			CTMemory.currentlyFading = true;
		}
	}