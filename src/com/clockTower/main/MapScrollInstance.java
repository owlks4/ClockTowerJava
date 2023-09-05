package com.clockTower.main;

import java.awt.Point;

import com.clockTower.CT.CTUI;

public class MapScrollInstance{
	
	int targetPosX;
	int targetPosY;
	short scrollSpeed;
	long startTime;
	float distance;
	int scrollTime;
	Point startingPos;
	Point endingPos;
	
	public MapScrollInstance(short _targetPosX, short _targetPosY, short _scrollSpeed) {
		targetPosX = _targetPosX;
		targetPosY = _targetPosY;
		scrollSpeed = _scrollSpeed;
		
		if (targetPosX == -1){
			targetPosX = Camera.transform.position.x;	//if we're not using this axis
		} else {
			targetPosX -= CTUI.leftBorderPos;	//if we ARE using it, then apply this adjustment too
			targetPosX *= 5f/4f;					//if we ARE using it, then apply this adjustment too
			targetPosX += 20;
		}

		if (targetPosY == -1){
			targetPosY = Camera.transform.position.y;	//if we're not using this axis
		} else {
			targetPosY -= CTUI.topBorderPos;	//if we ARE using it, then apply this adjustment too
			targetPosY *= 5f/4f;						//if we ARE using it, then apply this adjustment too
			targetPosY -= 1;
			}
		
		startingPos = new Point(Camera.transform.position.x,Camera.transform.position.y);
		endingPos = new Point(targetPosX,targetPosY);
		startTime = System.currentTimeMillis();
		
		distance = (float)Math.abs(Math.hypot(endingPos.x - startingPos.x, endingPos.y - startingPos.y));
		
		scrollTime = Math.round((distance * 80) / (scrollSpeed * 3f));
	}
}