package com.clockTower.CT;

import com.clockTower.Utility.Transform;
import com.clockTower.main.ExecuteADOFunction;
import com.clockTower.main.loader;
 
public class CTMark 				//A hotspot/point of interest.
	{
	public short unk1;
	public short unk2;
	public short unk3;
	
	public int eventID;
	public String name;
	public Transform transform = new Transform();
	
	void OnMouseDown(){
		
		System.out.println("Need to check if player is eligible for the event here.");
		
		for (int i = 0; i < loader.events.size(); i++){
			
			if (loader.events.get(i).eventID == eventID)
				{
					ExecuteADOFunction executeADO = new ExecuteADOFunction(loader.mainADO, loader.events.get(i).functionToStart, -1);
					executeADO.start();	//Start executing the ADO instructions
					break;
				}
			}
		}

	public void destroy() {
		// TODO Auto-generated method stub
		
		}
	}