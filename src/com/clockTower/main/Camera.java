package com.clockTower.main;

import com.clockTower.CT.CTMemory;
import com.clockTower.Utility.Transform;

public class Camera {

	public static Transform transform = new Transform(); //the real position considered by the game
	public static float x = 0; //the actual, window size adjusted position
	public static float y = 0; //the actual, window size adjusted position
	
	public static int roundedCamX;
	public static int roundedCamY;
	
	public static boolean allowMouseControl = false;

	static boolean needToMoveToPlayer = false;
	
	
	public static void tick() {
		
			if (CTMemory.userControl && loader.player != null && Math.abs(loader.player.myTransform.position.x - (transform.position.x + Game.DEFAULT_WIDTH / 2)) > Game.DEFAULT_WIDTH / 3) {
				needToMoveToPlayer = true;
			}
			
			if (needToMoveToPlayer) {
				
				if (((loader.player.myTransform.position.x - loader.player.mostRecentOffsetX) - (transform.position.x + Game.DEFAULT_WIDTH / 2)) < 0) {			
					transform.position.x-=2;
				} else {
					transform.position.x+=2;
				}
				
				if ((transform.position.x +(Game.DEFAULT_WIDTH / 2) - 5) < (loader.player.myTransform.position.x - loader.player.mostRecentOffsetX) && (transform.position.x +(Game.DEFAULT_WIDTH / 2) + 5) >= (loader.player.myTransform.position.x - loader.player.mostRecentOffsetX)) {
					needToMoveToPlayer = false;
				}
			}
			
			x = Game.GetAdjustedDimension(transform.position.x);
			y = Game.GetAdjustedDimension(transform.position.y);
		
			roundedCamX = Math.round(x);
			roundedCamY = Math.round(y);
	}
}