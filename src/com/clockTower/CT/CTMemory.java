package com.clockTower.CT;

import java.util.Hashtable;
import java.util.LinkedList;

import com.clockTower.main.loader;
 
 
public class CTMemory
	{		
		
		public static Hashtable <Short,Short> Bank = new Hashtable<Short, Short>();
		
		//Now, from my testing, I haven't actually worked out what banks 0 - 8 do, or if they even exist. The only ones I confirmed were 0xC, 0xD, and 0xE.
		
		
		//C0 memory began at 42623C

		//D0 memory began at 425D3C		(and so did 00 memory)

		//01 memory began at 425F3C
		
		//02 memory began at 42613C

		//E0 memory began at 4261BC

		//The ghidra analysis suggests the existence of F0 memory at 42613C, although I didn't find it being used in practice?
	
		public static Hashtable<String,Short> CustomBank = new Hashtable<String,Short>();	//a new bank I have added where you can set and retrieve variables using names.
	
		public static loader loader;
		public static boolean userControl = false;
		public static boolean currentlyFading = false;
		public static boolean SpriteWalkingX = false;
		public static boolean curStep = false;	//variant 1 (false) or variant 2 (true)
		
		public static LinkedList<CTAudio.CTClip> currentAudioClips = new LinkedList<CTAudio.CTClip>();
	
		public static boolean isPlaystationMode = false;

	public static short GetBankOffset(byte bankNumber){
		
		switch (bankNumber){
			case 0x0C:
				return 0x500;
			case 0x0D:
				return 0x00;
			case 0x0E:
				return 0x480;
			case 0x0F:
				return 0x200;
			default:
				return (short)(bankNumber * 0x200);
		}
	}
	
	public static short EvaluateInt16Bank(short input){
			
			//look for the appropriate int16 in the memory banks and return it. Or just return normally if there is no bank.
			
			if ((input & 0x0F00) == 0x0F00){	//if the second nibble is F, it's probably just a negative number, so don't look for its bank
				return input;	
				}
			
			 byte bankNumber = (byte)((input >>> 12) & 0x0F);
			
			if (bankNumber == 0){	//assume that we weren't looking for a banked variable
					return input;
				}
			
			short variable = (short)(input & 0x01FF);
			
				if (Bank.containsKey(variable)){
					return Bank.get(variable);}
					else {return 0;}
			}
		

	public static short ForceInt16Bank(short input){
			
			//look for the appropriate int16 in the memory banks and return it. 
			
			//With this function, even a '0' is forced to return bank 0x0D instead. (Which it does at least once in the actual game - near the end of function 27)
			
			 byte bankNumber = (byte)((input >>> 12) & 0x0F);
				
			int variable = GetBankOffset(bankNumber) + (input & 0x01FF);
				
				if (Bank.containsKey((short)variable)){
					return Bank.get((short)variable);}
					else {return 0;}
		}
		
		
	public static void SetVar(short bankAndVariable, short newValue){
		
			byte bankNumber = (byte)((bankAndVariable >>> 12) & 0x0F);
			
			int variable = GetBankOffset(bankNumber) + (bankAndVariable & 0x01FF);
			
			Bank.put((short)variable,newValue);
	}
	
	public static void MultiplyVar(short bankAndVariable, short multiplyAmount){
		
			byte bankNumber = (byte)((bankAndVariable >>> 12) & 0x0F);
			
			int variable = GetBankOffset(bankNumber) + (bankAndVariable & 0x01FF);
			
			if (Bank.containsKey((short)variable)){
				Bank.put((short)variable, (short)(Bank.get((short)variable) * multiplyAmount));}
			else{
				Bank.put((short)variable,(short)0);
				}

	}
	
	public static void DecVar(short bankAndVariable, short decreaseAmount){
		
			byte bankNumber = (byte)((bankAndVariable >>> 12) & 0x0F);
			
			int variable = GetBankOffset(bankNumber) + (bankAndVariable & 0x01FF);
			
			Bank.put((short)variable, (short)(Bank.get((short)variable) - decreaseAmount));
	}
	
	public static void IncVar(short bankAndVariable, short increaseAmount){
		
			byte bankNumber = (byte)((bankAndVariable >>> 12) & 0x0F);
			
			int variable = GetBankOffset(bankNumber) + (bankAndVariable & 0x01FF);
			
			Bank.put((short)variable,(short)(Bank.get((short)variable)+increaseAmount));
		}


	//ANIMATION FINDERS:

	public static int GetIdleAnimation(int animGroup){
		
		switch (animGroup)	//returns the animIndex of the idle animation for that group
			{
			case 1:		//Jennifer in the foreground
				return 2;
			case 12:	//Mary
				return 0;
			default:
				System.out.println("Don't know what the idle animation is for anim group "+animGroup+"!");
				return 0;
			}
		}
		
	public static int GetWalkingAnimation(int animGroup){
		
		switch (animGroup)	//returns the animIndex of the walking animation for that group
			{
			case 1:		//Jennifer in the foreground
				return 14;
			case 12:	//Mary
				return 1;
			default:
				System.out.println("Don't know what the walking animation is for anim group "+animGroup+"!");
				return 0;
			}
		}
		
	public static int GetTurningAnimation(int animGroup){
		
		switch (animGroup)	//returns the animIndex of the turning animation for that group
			{
			case 1:		//Jennifer in the foreground
				return 5;
			default:
				System.out.println("Don't know what the turning animation is for anim group "+animGroup+"!");
				return 0;
			}
		}
	}