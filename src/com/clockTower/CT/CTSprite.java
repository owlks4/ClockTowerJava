package com.clockTower.CT;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;

import org.apache.commons.io.FilenameUtils;

import com.clockTower.main.Camera;
import com.clockTower.main.Game;
import com.clockTower.main.Window;
import com.clockTower.main.loader;
import com.clockTower.online.OnlineMessages;
import com.clockTower.Utility.Transform;
import com.clockTower.Utility.Utility;

public class CTSprite
	{
	//for backgrounds and sprites:
	public String myName;
	public BufferedImage bufferedImage;
	public Transform myTransform = new Transform();
	public int mySlotID;
	public boolean isSprite = false;
	public int layer;
	public boolean isPSX = false;
	
	//for sprites:
	public CTAnimatedBitmap abm;
	public int bufferedImagesCount;
	public LinkedList<Short> WaitTimes = new LinkedList<Short>();
	public LinkedList<Short> MovementsX = new LinkedList<Short>();
	public LinkedList<Short> MovementsY = new LinkedList<Short>();
	public boolean isAnimating = false;
	public boolean loop = true;
	public short startingFrame = 0;
	public int numLoops = 1;
	
	public int currentLoop = 0;
	public int currentFrame = 0;
	
	public int animGroup;
	public int animIndex;
	
	public boolean markForDeletion;
	
	public boolean immuneToDeletion;
	
	public boolean busy;
	
	public boolean isOtherPlayer;

	public boolean measuresFromBase;
	public boolean stickToCamera;
	
	public long nextFrameStartTime = 0;
	
	int XatStart = myTransform.position.x;
	int YatStart = myTransform.position.y;
	
	public boolean nonAnimating = false;
	
	public int walkToPositionMinX;
	public int walkToPositionMaxX;
	short directionToFaceAtEnd;
	private boolean sprWalkingX;
	
	public int mostRecentOffsetX = 0;
	public int mostRecentOffsetY = 0;
	
	public LinkedList<CTAnimation> queuedAnims = new LinkedList<CTAnimation>();
	
	public void tick() {
		
		if (!nonAnimating  && isSprite && !isAnimating){
			isAnimating = true;
	
			XatStart = myTransform.position.x + mostRecentOffsetX;
			YatStart = myTransform.position.y + mostRecentOffsetY;
			mostRecentOffsetX = mostRecentOffsetY = 0;

			bufferedImagesCount = abm.bufferedImages.size();
			currentFrame = startingFrame;
			currentLoop = 0;
			nextFrameStartTime = System.currentTimeMillis();
			
			if (!Game.isServer && mySlotID == 0 && Game.client != null) { //assume it's the player, and send the new animation to the server
				Game.client.sendBytes(OnlineMessages.UpdateMyAnimationOnServer(Game.client.myClientInfo, animGroup, animIndex, startingFrame, loop, numLoops, myTransform.position, myTransform.flipping.x));
				}
			else if (Game.isServer && mySlotID == 0 && Game.server.running) { //send server player if we are the server
				Game.server.SendMessageToClients(OnlineMessages.UpdateMyAnimationOnServer(Game.server.myClientInfo, animGroup, animIndex, startingFrame, loop, numLoops, myTransform.position, myTransform.flipping.x));
				}
			}
		
		if (!nonAnimating && isSprite && isAnimating && System.currentTimeMillis() >= nextFrameStartTime)
		{
			bufferedImagesCount = abm.bufferedImages.size();
			
			if (currentFrame >= WaitTimes.size()) {
				nonAnimating = true;
				}
			else if (currentFrame >= MovementsX.size()) {
				currentFrame = 0;
				}
			else {
				int currentFrameWaitTime = WaitTimes.get(currentFrame);
				 
				if (currentFrameWaitTime== 0){	//if this frame has a duration of 0 frames, just ignore it and play a footstep sound instead
					CTAudio newSE = new CTAudio();
					if (CTMemory.curStep){
						newSE.Init("SNDDATA/STEP_L.WAV",false,false);	
					} else {
						newSE.Init("SNDDATA/STEP_R.WAV",false,false);		
					}
					CTMemory.curStep = !CTMemory.curStep;
					loader.soundEffects.add(newSE);
				}
				else if (currentFrame < bufferedImagesCount)
					{
					ClockTowerBufferedImage frame = abm.bufferedImages.get(currentFrame);
					bufferedImage = frame.tex;
					
					mostRecentOffsetX = frame.offsetX;
					mostRecentOffsetY = frame.offsetY;

					myTransform.position = new Point(XatStart+Math.round((-mostRecentOffsetX+((float)MovementsX.get(currentFrame))*myTransform.flipping.x*myTransform.scale.x)),YatStart+Math.round((-mostRecentOffsetY+((float)MovementsY.get(currentFrame))*myTransform.flipping.y*myTransform.scale.y)));
			
					nextFrameStartTime = System.currentTimeMillis() + (currentFrameWaitTime * 15);
					}
					
				if (startingFrame != 0)
					{startingFrame = 0;}
				
				currentFrame++;
				
				if (currentFrame >= abm.bufferedImages.size()){
					XatStart = XatStart + Math.round(MovementsX.get(currentFrame-1)*myTransform.flipping.x*myTransform.scale.x);
					YatStart = YatStart + Math.round(MovementsY.get(currentFrame-1)*myTransform.flipping.y*myTransform.scale.y);
					
					if (loop) {
						currentLoop--;
						currentFrame = 0;
					} else {
						currentLoop++;
						}
					
					if (currentLoop >= numLoops) {
						PlayNextQueuedAnim();	//if it exists
					} else {
						currentFrame = 0;
					}
				}
			}
		}
		
		//SPRWALKX stuff
		
		if (sprWalkingX) {
			
			if (myTransform.position.x >= walkToPositionMinX && myTransform.position.x <= walkToPositionMaxX){ //if we have reached our destination
				
				if ((myTransform.flipping.x < 0 && directionToFaceAtEnd >= 0) || (myTransform.flipping.x >= 0 && directionToFaceAtEnd < 0)){
					LoadSpriteDataIntoSprite(new CTAnimation( animGroup, CTMemory.GetTurningAnimation(animGroup), 0));
					isSprite = true;
					loop = false;
					currentLoop = 0;
					numLoops = 1;
					}
				
				//if we are not turning or if we have finished the turning animation
				if ((CTMemory.GetTurningAnimation(animGroup) != animIndex) || (CTMemory.GetTurningAnimation(animGroup) == animIndex && currentLoop >= numLoops)) {
					busy = false;
					sprWalkingX = false;
					
					LoadSpriteDataIntoSprite(new CTAnimation(animGroup, CTMemory.GetIdleAnimation(animGroup), 0));
					isSprite = true;
					loop = true;
					numLoops = 1;
					}
			}
		}
	}
	
	public void PlayNextQueuedAnim() {
		if (queuedAnims.size() > 0) {
			LoadSpriteDataIntoSprite(queuedAnims.get(0));
			if (queuedAnims.size() > 0) {
				queuedAnims.remove(0);
				}
			isAnimating = false;
			}
	}
	
	
	public void initializeNonAnimatingSprite(){	//called to initialize sprites that don't animate (and thus would not have picked up important values from its animation loop)
		
		nonAnimating = true;
		myTransform.position = new Point(Math.round(myTransform.position.x-(myTransform.scale.x*myTransform.flipping.x*((abm.bufferedImages.get(startingFrame).offsetX)))),Math.round(myTransform.position.y+(myTransform.scale.y*myTransform.flipping.y*((abm.bufferedImages.get(startingFrame).offsetY)))));
	}
	
	
	public void render(Graphics g, Camera cam){

		myTransform.rotation = 0;
		
		DrawToScreen((Graphics2D)g,Game.GetAdjustedDimension(myTransform.position.x),Game.GetAdjustedDimension(myTransform.position.y));
		}
	
	
public void DrawToScreen(Graphics2D g2d, float effectiveX, float effectiveY) {
		
		if (bufferedImage != null) {
			
			float width = Game.GetAdjustedDimension(bufferedImage.getWidth()*myTransform.scale.x);
			float height =  Game.GetAdjustedDimension(bufferedImage.getHeight()*myTransform.scale.y);
			
			if (measuresFromBase) {
				effectiveY -= height;
				effectiveY -= Game.GetAdjustedDimension(Window.headerSizeY);
			}
			
			if (myTransform.flipping.x < 0) {effectiveX += width; width *= -1;}
			if (myTransform.flipping.y < 0) {effectiveY -= height; height *= -1;}
			
			if (stickToCamera) {
				g2d.drawImage(bufferedImage, 
						  Window.blackBarSize + (Math.round(effectiveX)),
					      (Math.round(effectiveY)),
					       Math.round(width),
					       Math.round(height),
					      null);
				}
			else
				{
				g2d.drawImage(bufferedImage, 
						  Window.blackBarSize + (Math.round(effectiveX)-Camera.roundedCamX),
					      (Math.round(effectiveY)-Camera.roundedCamY),
					       Math.round(width),
					       Math.round(height),
					      null);
				}
			} 
			else {
				//System.out.println("BufferedImage was null");
			}
	}

	public void fadeIn(){
		//TODO: Reimplement sprite fadeIn
		System.out.println("Sprite fadein needs to be implemented");
		/*
		CTMemory.currentlyFading = true;
		while (rend.materials[0].GetColor("_Color").a < 1f)
			{
			Color colorToEdit = rend.materials[0].GetColor("_Color");
			Color newColor = new Color(colorToEdit.r, colorToEdit.g, colorToEdit.b, colorToEdit.a + (1.25f * Time.deltaTime));
			rend.materials[0].SetColor("_Color",newColor);
			yield return null;
			}
		CTMemory.currentlyFading = false;*/
	}
	
	public void LoadSpriteDataIntoSprite(CTAnimation anim){
					
					String animationGroup = Utility.GetAnimationGroupNameFromID(anim.animGroup);
					String animationPath = FilenameUtils.concat(Game.GAME_ROOT_DIR,animationGroup + Utility.IntToStringWithSetNumberOfDigits(anim.animIndex,3) + ".ABM");
					
					isPSX = false;
					
					if (!new File(animationPath).exists()) {
						System.out.println("need to implement TMC-finding logic here");
						isPSX = true;
					}
					
					if (!isPSX) {
					
						String[] splitAnimationGroup = Utility.GetFileNameWithoutExtension(animationGroup).split("_");
						
						String myNameForPTNandPYXlookup = splitAnimationGroup[0] + "_act";
						
						myName = myNameForPTNandPYXlookup;
						
						if (splitAnimationGroup[1].length() == 1){
							myNameForPTNandPYXlookup += "0"+splitAnimationGroup[1];
							}
						else{
							myNameForPTNandPYXlookup += splitAnimationGroup[1];
							}
							
						myNameForPTNandPYXlookup = myNameForPTNandPYXlookup.toLowerCase();
					
						for (int i = 0; i < loader.PTNfiles.size(); i++){
							if (loader.PTNfiles.get(i).PTNname.equalsIgnoreCase(myNameForPTNandPYXlookup)){
									WaitTimes = new LinkedList<Short>();
									for (int j = 0; j < loader.PTNfiles.get(i).PTNAnimations.get(anim.animIndex).frames.size(); j++){
										WaitTimes.add((short) loader.PTNfiles.get(i).PTNAnimations.get(anim.animIndex).frames.get(j).duration);
										}
								break;
							}
						}
						
						for (int i = 0; i < loader.PYXfiles.size(); i++){
							if (loader.PYXfiles.get(i).PYXname.equalsIgnoreCase(myNameForPTNandPYXlookup)){
									MovementsX = new LinkedList<Short>();
									MovementsY = new LinkedList<Short>();
									for (int j = 0; j < loader.PYXfiles.get(i).PYXAnimations.get(anim.animIndex).frames.size(); j++){
										MovementsX.add((short)(loader.PYXfiles.get(i).PYXAnimations.get(anim.animIndex).frames.get(j).XOffset*2));
										MovementsY.add((short)(loader.PYXfiles.get(i).PYXAnimations.get(anim.animIndex).frames.get(j).YOffset*2));
										}
								break;
								}
							}
						}
					else {
						
						//TODO: PSX TMC embedded PYX/PTN data stuff here
						
					}
					
					animGroup = anim.animGroup;
					animIndex = anim.animIndex;
					loop = anim.loop;
					numLoops = anim.numLoops;
					
					abm = new CTAnimatedBitmap();
					
					if (isPSX) {
						abm.LoadTMCFromPath(animationPath);
					} else {
						abm.LoadFromPath(animationPath);
						abm.UpdateBufferedImages(loader.palettes.get(loader.activePalette));
					}
					
					bufferedImage = abm.bufferedImages.get(startingFrame).tex;
					myName = new File(animationPath).getName();
					startingFrame = (short) anim.startingFrame;
					
					isAnimating = false;	//so that it then gets set to true by render()
		}
	
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void sprWalkX(int destPosXMin, int destPosXMax, short directionToFaceAtEnd, short unk) {
		
		loop = false; //whatever is currently playing, abandon any prospect of looping, because this stuff is ready to take over
		
		//if we are not facing our destination, turn around
		if ((myTransform.flipping.x < 0 && destPosXMin > myTransform.position.x) || (myTransform.flipping.x > 0 && destPosXMax < myTransform.position.x)){
			myTransform.flipping.x *= -1;
			queuedAnims.add(new CTAnimation(animGroup, CTMemory.GetTurningAnimation(animGroup), 0, false, 1));
			}
		
		queuedAnims.add(new CTAnimation(animGroup, CTMemory.GetWalkingAnimation(animGroup), 0, true, 1));
		
		nonAnimating = false;
		
		PlayNextQueuedAnim();
		
		WaitUntilAtPosition(Math.round(destPosXMin*1.25f), Math.round(destPosXMax*1.25f), directionToFaceAtEnd);
	}
	
	public void WaitUntilAtPosition(int _walkToPositionMinX, int _walkToPositionMaxX, short _directionToFaceAtEnd){

		busy = true;
		
		walkToPositionMinX = _walkToPositionMinX;
		walkToPositionMaxX = _walkToPositionMaxX;
		directionToFaceAtEnd = _directionToFaceAtEnd;
		
		if (walkToPositionMinX  == walkToPositionMaxX ){	//if they are both the same then increase the area to be a bit more lenient
			walkToPositionMinX -= 8f;
			walkToPositionMaxX += 8f;
		}
		
		sprWalkingX = true;
	}
}