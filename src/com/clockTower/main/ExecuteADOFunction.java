package com.clockTower.main;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;

import com.clockTower.CT.CTAnimatedBitmap;
import com.clockTower.CT.CTAnimation;
import com.clockTower.CT.CTAudio;
import com.clockTower.CT.CTBackground;
import com.clockTower.CT.CTMark;
import com.clockTower.CT.CTMemory;
import com.clockTower.CT.CTSprite;
import com.clockTower.CT.CTTileMap;
import com.clockTower.CT.CTUI;
import com.clockTower.CT.ClockTowerBufferedImage;
import com.clockTower.Utility.Utility;
import com.clockTower.Utility.Vector2;
import com.clockTower.fileUtilities.ADOFunction;
import com.clockTower.fileUtilities.ADOScript;
import com.clockTower.fileUtilities.BMPLoader;
import com.clockTower.fileUtilities.BitConverter;
import com.clockTower.fileUtilities.MidiStream;
import com.clockTower.main.loader.ConditionalType;
import com.clockTower.online.ClientInfo;
import com.clockTower.online.OnlineMessages;
import com.clockTower.online.Server;
import com.clockTower.CT.CTEvent;

public class ExecuteADOFunction extends Thread {
	public ADOScript ado;
	public int functionIndex;
	public int pos;

	public Thread oldThread;
	
public ExecuteADOFunction(ADOScript _ado, int functionToStart, int _pos) {
		ado = _ado;
		functionIndex = functionToStart;
		pos = _pos;
	}

public void run(){
	

	if (functionIndex > ado.ADOFunctions.size()){
		System.out.println("Function "+functionIndex+" does not exist in that ADO!");
		return;
	}
	
	if (oldThread != null) {
		try {
			oldThread.join();	
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	oldThread = null;
	
	boolean isCall = false;
	
	if (pos == -1){
	pos = 0;
	isCall = true;		//calls will not return at the end
	}
	
	ADOFunction currentFunction = ado.ADOFunctions.get(functionIndex);
	
	
	//now the ADO command processing begins
	
	//System.out.println("ADO function "+functionIndex+" read successfully, beginning parsing");
	
	while (pos < currentFunction.filebytes.length){
		//ADO instruction parsing is here
		
		if (currentFunction.filebytes[pos+1] != (byte) 0xFF){
			System.out.println("The ADO parser is lost... it was expecting an opcode, but this doesn't look like one! Function index: "+functionIndex+", Pos: "+pos);
		}
		
		switch (currentFunction.filebytes[pos] & 0xFF) 		//check the instruction
			{
			case 0x00:	 //RETN					The first instruction in every function
				pos += 2;
				break;
				
				//0x0A is divide_variable
				
			case 0x0B:	//MULTIPLY_VARIABLE
				{
				pos += 2;
			
				short variable = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos+=2;
				short multiplyAmount = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+=2;
				
				CTMemory.MultiplyVar(variable,multiplyAmount);
				}
				break;
			case 0x0C:	//DECREASE_VARIABLE_BY
				{
				pos += 2;
			
				short variable = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos+=2;
				short decreaseAmount = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+=2;
				
				CTMemory.DecVar(variable,decreaseAmount);
				}
				break;
			case 0x0D:	//INCREASE_VARIABLE_BY
				{
				pos += 2;
			
				short variable = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos+=2;
				short increaseAmount = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+=2;
				
				CTMemory.IncVar(variable,increaseAmount);
				}
				break;
			case 0x10:	//SET_VARIABLE
				{
				pos += 2;
			
				short variable = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos+=2;
				short newValue = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+=2;
				
				CTMemory.SetVar(variable,newValue);
				}
				break;
			case 0x11:	//IS_VARIABLE_EQUAL_TO
				{
				pos += 2;
			
				short param1 = CTMemory.ForceInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//It is forced to get the variable from a bank, you can't use a regular number with this parameter
				pos+=2;
				short param2 = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+=2;
				
				if (loader.conditionalStack.get(loader.conditionalStack.size()-1).operatorType == 1){	//AND
					if (param1 != param2){loader.conditionalStack.get(loader.conditionalStack.size()-1).fulfilled = false;} //only need to do this for one side of the boolean, because the other result 'true' is the default
				}
				else if (loader.conditionalStack.get(loader.conditionalStack.size()-1).operatorType == 2){	//OR
					if (param1 == param2){loader.conditionalStack.get(loader.conditionalStack.size()-1).fulfilled = true;}  //only need to do this for one side of the boolean, because the other result 'false' is the default
				}
				}
				break;
			case 0x12:	//IS_VARIABLE_NOT_EQUAL_TO
				{
				pos += 2;
			
				short param1 = CTMemory.ForceInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//It is forced to get the variable from a bank, you can't use a regular number with this parameter
				pos+=2;
				short param2 = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+=2;
				
				if (loader.conditionalStack.get(loader.conditionalStack.size()-1).operatorType == 1){	//AND
					if (param1 == param2){loader.conditionalStack.get(loader.conditionalStack.size()-1).fulfilled = false;} //only need to do this for one side of the boolean, because the other result 'true' is the default
				}
				else if (loader.conditionalStack.get(loader.conditionalStack.size()-1).operatorType == 2){	//OR
					if (param1 != param2){loader.conditionalStack.get(loader.conditionalStack.size()-1).fulfilled = true;}  //only need to do this for one side of the boolean, because the other result 'false' is the default
				}
				}
				break;
			case 0x13:	//IS_VARIABLE_GREATER_THAN
				{
				pos += 2;
			
				short param1 = CTMemory.ForceInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//It is forced to get the variable from a bank, you can't use a regular number with this parameter
				pos+=2;
				short param2 = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+=2;
				
				if (loader.conditionalStack.get(loader.conditionalStack.size()-1).operatorType == 1){	//AND
					if (param1 <= param2){loader.conditionalStack.get(loader.conditionalStack.size()-1).fulfilled = false;} //only need to do this for one side of the boolean, because the other result 'true' is the default
					}
				else if (loader.conditionalStack.get(loader.conditionalStack.size()-1).operatorType == 2){	//OR
					if (param1 > param2){loader.conditionalStack.get(loader.conditionalStack.size()-1).fulfilled = true;}  //only need to do this for one side of the boolean, because the other result 'false' is the default
					}
				}
				break;
			case 0x14:	//IS_VARIABLE_LESS_THAN
				{
				pos += 2;
			
				short param1 = CTMemory.ForceInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//It is forced to get the variable from a bank, you can't use a regular number with this parameter
				pos+=2;
				short param2 = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+=2;
				
				if (loader.conditionalStack.get(loader.conditionalStack.size()-1).operatorType == 1){	//AND
					if (param1 >= param2){loader.conditionalStack.get(loader.conditionalStack.size()-1).fulfilled = false;} //only need to do this for one side of the boolean, because the other result 'true' is the default
					}
				else if (loader.conditionalStack.get(loader.conditionalStack.size()-1).operatorType == 2){	//OR
					if (param1 < param2){loader.conditionalStack.get(loader.conditionalStack.size()-1).fulfilled = true;}  //only need to do this for one side of the boolean, because the other result 'false' is the default
					}
				}
				break;
			case 0x1F:	//DO				Used like so:    IF/WHILE (condition) DO (stuff) ELSE (alternative stuff) ENDIF/ENDWHILE.   WHILE does not support ELSE.
				pos += 2;
				if (loader.conditionalStack.get(loader.conditionalStack.size()-1).type == ConditionalType.IF){
					//if the condition was not fulfilled, skip to the end of the if statement without executing the contents
					if (!loader.conditionalStack.get(loader.conditionalStack.size()-1).fulfilled){
						while (!((currentFunction.filebytes[pos] == 0x2D || currentFunction.filebytes[pos] == 0x2F) && currentFunction.filebytes[pos+1] == (byte) 0xFF && CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos+2)) == loader.conditionalStack.get(loader.conditionalStack.size()-1).myID)){
							pos++;
							}
					}
					else
					{
						loader.conditionalStack.get(loader.conditionalStack.size()-1).ignoreElseSection = true;
					}
				} else if (loader.conditionalStack.get(loader.conditionalStack.size()-1).type == ConditionalType.WHILE){
					//if the condition was not fulfilled, skip to the end of the while statement without executing the contents
					if (!loader.conditionalStack.get(loader.conditionalStack.size()-1).fulfilled){
						while (!(currentFunction.filebytes[pos] == 0x2E && currentFunction.filebytes[pos+1] == (byte) 0xFF && CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos+2)) == loader.conditionalStack.get(loader.conditionalStack.size()-1).myID)){
							pos++;
							}
					}
				}
				break;
			case 0x21:	//JMPALLEND 			like JMP except it doesn't come back
			{
				pos += 2;
				ExecuteADOFunction executeADO = new ExecuteADOFunction(ado, BitConverter.ToInt16(currentFunction.filebytes,pos), 0);
				executeADO.start();	//Start executing the ADO instructions
			}
				return; //thread should now end (I hope)
			case 0x22:	 //JMP					Jump to another function
			{
				pos += 2;
				loader.returnStack.add(new returnPoint(ado,functionIndex,pos+2));	//save position in the return stack so we can come back later
				ExecuteADOFunction executeADO = new ExecuteADOFunction(ado, BitConverter.ToInt16(currentFunction.filebytes,pos), 0);
				executeADO.start();	//Start executing the ADO instructions
			}	
				return; //thread should now end (I hope). But we added this event's information to the return stack, so we will restore to this position in execution later.
			case 0x23:	//CALL					??? sometimes used for clickable items in rooms, in conjunction with SETMARK. Other times, used with EVSTART to seemingly start a coroutine.
				pos += 2;
				CTEvent newEvent = new CTEvent();
				newEvent.functionToStart = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				newEvent.eventID = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				newEvent.unknown = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				loader.events.add(newEvent);
				loader.currentADOThread = this;  
				try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}
				break;
			case 0x28:   //Message terminator
				pos+= 2;
				break;
			case 0x29:	//START_IF			Begin an if statement
				pos+=2;
				conditional newIfStatement = new conditional(ConditionalType.IF, BitConverter.ToInt16(currentFunction.filebytes,pos), ado, functionIndex);
				pos+=2;
				newIfStatement.operatorType = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos+=2;
				newIfStatement.startPoint = pos;
				if (newIfStatement.operatorType == 1){
					newIfStatement.fulfilled = true;	//because it will only take one wrong statement in the AND to prove it false
				}else if (newIfStatement.operatorType == 2){
					newIfStatement.fulfilled = false;	//because it will only take one correct statement in the OR to prove it true
				}
				loader.conditionalStack.add(newIfStatement);
				loader.currentADOThread = this;
				try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				break;
			case 0x2A:	//WHILE				Begin a while statement
				pos+=2;
				conditional newWhileStatement = new conditional(ConditionalType.WHILE,BitConverter.ToInt16(currentFunction.filebytes,pos),ado, functionIndex);
				pos+=2;
				newWhileStatement.operatorType = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos+=2;
				newWhileStatement.startPoint = pos;
				if (newWhileStatement.operatorType == 1){
					newWhileStatement.fulfilled = true;	//because it will only take one wrong statement in the AND to prove it false
				}else if (newWhileStatement.operatorType == 2){
					newWhileStatement.fulfilled = false;	//because it will only take one correct statement in the OR to prove it true
				}
				loader.conditionalStack.add(newWhileStatement);
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				break;
			case 0x2B:	 //NOP				Do nothing
				pos += 2;
				break;
			case 0x2D: 	  //ENDIF			End an if statement
				{
				pos += 2;
				short IDofStatementToEnd = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				for (int i = loader.conditionalStack.size() - 1; i >= 0; i--)
					{
						conditional cond = loader.conditionalStack.get(i);
						if (cond.type == ConditionalType.IF && cond.myID == IDofStatementToEnd && cond.functionIndex == functionIndex){
							//Find the conditional we should be ending, and end the conditional. Theoretically, it should be the most recent one in the stack, but this is just in case it's not
							loader.conditionalStack.remove(i);
							break;
						}
					}
				break;
				}
			case 0x2E: 	  //ENDWHILE			End a while statement
				{
				pos += 2;
				short IDofStatementToEnd = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				for (int i = loader.conditionalStack.size() - 1; i >= 0; i--)
					{
						conditional cond = loader.conditionalStack.get(i);
						if (cond.type == ConditionalType.WHILE && cond.myID == IDofStatementToEnd && cond.functionIndex == functionIndex){
							
							if (cond.fulfilled)	//if it's still fulfilled, reset the fulfilment boolean and go back to the start of the loop
								{
								if (cond.operatorType == 1){
									cond.fulfilled = true;	//because it will only take one wrong statement in the AND to prove it false
									}
								else if (cond.operatorType == 2){
									cond.fulfilled = false;	//because it will only take one correct statement in the OR to prove it true
								}
								pos = cond.startPoint;
								}
							else{
								loader.conditionalStack.remove(i);
								}
							break;
						}
					}
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				break;
				}
			case 0x2F:	 //ELSE	(the logic for this one is handled by opcode 0x1F (DO))
				pos += 2;
				short ID = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				
				for (int i = loader.conditionalStack.size() - 1; i >= 0; i--){		//if this is the else for an if statement for which we've completed the not-else section, skip forwards until the ENDIF
					conditional cond = loader.conditionalStack.get(i);
					if (cond.myID == ID && cond.ignoreElseSection && cond.script == ado && cond.functionIndex == functionIndex)
						{
						while (!(currentFunction.filebytes[pos] == 0x2D && currentFunction.filebytes[pos+1] == (byte) 0xFF && CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos+2)) == loader.conditionalStack.get(loader.conditionalStack.size()-1).myID)){
							pos++;
							}
						}
					break;
				}
				break;
			case 0x30:	 //MSGINIT
				pos += 2;
				CTUI.topLeftX = (short) Math.round(BitConverter.ToInt16(currentFunction.filebytes,pos) * 1.25f);
				pos += 2;
				CTUI.topLeftY = (short) Math.round(BitConverter.ToInt16(currentFunction.filebytes,pos) * 1.25f);
				pos += 2;
				CTUI.bottomRightX = (short) Math.round(BitConverter.ToInt16(currentFunction.filebytes,pos) * 1.25f);
				pos += 2;
				CTUI.bottomRightY = (short) Math.round(BitConverter.ToInt16(currentFunction.filebytes,pos) * 1.25f);
				pos += 2;
				CTUI.unk1 = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				break;
			case 0x32:	 //MSGATTR
				pos += 2;
				CTUI.unk2 = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				CTUI.unk3 = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				CTUI.unk4 = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				CTUI.unk5 = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				CTUI.unk6 = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				CTUI.unk7 = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				CTUI.unk8 = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				CTUI.unk9 = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				break;
			case 0x33:  //MSGOUT
                {
                pos += 6; //also skips over 4 bytes of data (usually 00 F0 00 F0)

                int msgStartPos = pos;
                
                while (BitConverter.ToUInt16(currentFunction.filebytes, pos) != 0xFF28)
					{
                    pos++;
                    }
                
                try {
                	CTUI.ShowMessage(new String(currentFunction.filebytes,msgStartPos,pos-msgStartPos,"SHIFT-JIS"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
             
                }
                break;
			case 0x34: //SETMARK        //This is probably setting hotspot positions. Values might include position, and the position Jennifer needs to be at to activate it?
                                                //The functions to play when each mark is pressed is set out by CALL. E.g. look at function 261, in the first if statement. It gets the calls, then draws the loader.marks.
                {
                pos += 2;
				System.out.println("SETMARK NEEDS TO BE IMPLEMENTED");

				int i = 0;
				
                while (currentFunction.filebytes[pos + 1] != (byte) 0xFF)  //count sections of 0x0A until we hit the next opcode
                    {
					CTMark newMark = new CTMark();
                    newMark.name = "Mark";
					short X = BitConverter.ToInt16(currentFunction.filebytes, pos);   //Tentatively, XPos of marker
                    pos += 2;
                    short Y = BitConverter.ToInt16(currentFunction.filebytes, pos);   //Tentatively, YPos of marker
                    pos += 2;
					newMark.transform.position = new Point(Math.round(X*1.25f), Math.round(Y*1.25f));
                    newMark.unk1 = BitConverter.ToInt16(currentFunction.filebytes, pos);   //Tentatively, max X the player must autowalk to
                    pos += 2;
                    newMark.unk2 = BitConverter.ToInt16(currentFunction.filebytes, pos);   //Tentatively, Y position the player must be at
                    pos += 2;
                    newMark.unk3 = BitConverter.ToInt16(currentFunction.filebytes, pos);    //Tentatively, min X the player must be at
                    pos += 2;
					newMark.eventID = i + 10;
                    loader.marks.add(newMark);
					i++;
					}
                 }
                 break;
			case 0x36:   //MSGWAIT
				pos+= 2;
				while (CTUI.displayingAnyMessage){
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				}
				break;
			case 0x37:	//EVSTART			loader.events... a bit like coroutines? They seem to be used in this way for scripts 58 and 59 (the credits controller and the rain controller)
				pos += 2;
				short eventIDToStart = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;
				//unknown value here
				pos += 2;
				for (CTEvent ev : loader.events){
					if (ev.eventID == eventIDToStart){
						ExecuteADOFunction executeADO = new ExecuteADOFunction(ado, ev.functionToStart, -1);
						executeADO.start();	//Start executing the ADO instructions
						break;
					}	
				}
				break;
			case 0x39:   //BGLOAD
				{
					pos+= 2;
					int targetSlot = BitConverter.ToUInt16(currentFunction.filebytes,pos);
					pos += 2;
					String path = "";
					
					while (currentFunction.filebytes[pos] != 0x00){
						path += (char)currentFunction.filebytes[pos];
						pos++;
						}
						
					path = FilenameUtils.concat(Game.GAME_ROOT_DIR, path);
					
					while(loader.backgroundSlots.size() < targetSlot + 1){
						loader.backgroundSlots.add(new CTBackground());
						loader.backgroundSlots.get(loader.backgroundSlots.size() - 1).myName = "NULL";
					}
					
					BMPLoader bmpl = new BMPLoader();
					loader.backgroundSlots.set(targetSlot, new CTBackground());
					System.out.println("trying to load background "+path);
					loader.backgroundSlots.get(targetSlot).myName = FilenameUtils.getName(path);
					loader.backgroundSlots.get(targetSlot).bufferedImage = bmpl.LoadBMP(path);
					loader.backgroundSlots.get(targetSlot).mySlotID = targetSlot;
					
					pos+=2; //skip null terminating bytes

					while(pos % 2 != 0){ //then pad until pos is a multiple of two
						pos++;
						}
				}
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				break;
			case 0x3A: 	  //PALLOAD				load a palette
				{
				pos += 2;
				int targetSlot = BitConverter.ToUInt16(currentFunction.filebytes,pos);
				pos += 2;
					
				String palettepath = "";
					
				while (currentFunction.filebytes[pos] != 0x00){	//load bufferedImage path
					palettepath += (char)currentFunction.filebytes[pos];
					pos++;
					}
					
				pos += 2; //skip null terminating bytes

                while (pos % 2 != 0) //then pad until pos is a multiple of two
                    {
                    pos++;
                    }
					
				while(loader.palettes.size() < targetSlot + 1){
						loader.palettes.add(new Color[256]);
					}
				
				BMPLoader bmpl = new BMPLoader();
				loader.palettes.set(targetSlot, bmpl.GetBMPPalette(FilenameUtils.concat(Game.GAME_ROOT_DIR,palettepath),false));
				loader.activePalette = (short)targetSlot;
				
				if (loader.cursor == null){
					loader.InitialiseCursor();
					}
				}
				break;
			case 0x3B:	//BGMREQ				Play a MIDI file
				{
				pos+=2;
				boolean loop = BitConverter.ToInt16(currentFunction.filebytes, pos) == 1;
					pos+=2;
				String path = "";
				while (currentFunction.filebytes[pos] != 0x00){
						path += (char)currentFunction.filebytes[pos];
						pos++;
						}
				pos += 2; //skip null terminating bytes

                while (pos % 2 != 0) //then pad until pos is a multiple of two
                    {
                    pos++;
                    }
					
				path = FilenameUtils.concat(Game.GAME_ROOT_DIR, path);
				if (loader.music != null){loader.music.Stop();}
				loader.music = new MidiStream();
				loader.music.LoadFromPath(path);
				loader.music.SetVolume(33000);
				loader.music.Play(loop);
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				}
				break;
			case 0x3C:   //SPRCLR
				{
				pos += 2;
				int spriteID = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2 ;
				
				CTSprite target = null;
				
				for (int i = 0; i < loader.spriteSlots.size(); i++) {
					if (loader.spriteSlots.get(i).mySlotID == spriteID) {
						target = loader.spriteSlots.get(i);
						break;
					}
				}
				
				if (!target.immuneToDeletion){
					target.destroy();
					loader.spriteSlots.remove(target);
					}
				break;
				}
			case 0x3F:   //ALLSPRCLEAR
				pos+= 2;
				for (int i = loader.spriteSlots.size() - 1; i >= 0; i--){
					if (loader.spriteSlots.get(i) != null && !loader.spriteSlots.get(i).immuneToDeletion){
						loader.spriteSlots.get(i).destroy();
						loader.spriteSlots.remove(i);
						}
					}
				break;
			case 0x40:   //MSGCLEAR
				pos+= 2;
				CTUI.ClearMessage();
				break;
			case 0x41:   //SCREENCLR
				pos+= 2;
				
				CTUI.EnactFade(2);

				if (loader.faWaitCarriesOver){	//if a fawait is pending, then use it now
					loader.faWaitCarriesOver = false;
					while (CTMemory.currentlyFading){
						loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
						}
					}
				break;
			case 0x42:   //SCREENON
				pos+= 2;
				CTUI.EnactFade(0);
				
				if (loader.faWaitCarriesOver){	//if a fawait is pending, then use it now
					loader.faWaitCarriesOver = false;
					while (CTMemory.currentlyFading){
						loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
						}
					}
				break;
			case 0x43:   //SCREENOFF
				pos+= 2;
				CTUI.EnactFade(1);
				
				if (loader.faWaitCarriesOver){	//if a fawait is pending, then use it now
					loader.faWaitCarriesOver = false;
					while (CTMemory.currentlyFading){
						loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
						}
					}
				break;
			case 0x46:   //BGDISP
				{
					pos += 2;
					short X = BitConverter.ToInt16(currentFunction.filebytes,pos);
					pos+= 2;
					short Y = BitConverter.ToInt16(currentFunction.filebytes,pos);
					pos+= 2;
					int CopyFromBGSlot = BitConverter.ToUInt16(currentFunction.filebytes,pos) & 0x3F;	//possibly
					pos+= 2;
					int layer = BitConverter.ToUInt16(currentFunction.filebytes,pos);	//possibly
					pos+= 2;
					pos+= 2;
					boolean useAlpha = (CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos)) == 1);
					pos+= 2;
				
					int targetSlot = loader.backgroundSpriteSlots.size();
					
					CTSprite newBGSprite = new CTSprite();
					
					newBGSprite.myName = loader.backgroundSlots.get(CopyFromBGSlot).myName;
					newBGSprite.mySlotID = targetSlot;

					newBGSprite.bufferedImage = loader.backgroundSlots.get(CopyFromBGSlot).bufferedImage;

					newBGSprite.myTransform.position = new Point(Math.round(X*1.25f), Math.round(Y*1.25f));
					newBGSprite.myTransform.scale = new Vector2(1.25f,1.25f);
					newBGSprite.stickToCamera = true;
					
					boolean done = false;
					
					for (int i = 0; i < loader.backgroundSpriteSlots.size(); i++) {
						
						if (loader.backgroundSpriteSlots.get(i).mySlotID == targetSlot) {
							loader.backgroundSpriteSlots.set(i, newBGSprite);
							loader.backgroundSpriteSlots = Utility.MoveSpriteToCorrectLayerPosition(loader.backgroundSpriteSlots,i);
							done = true;
							break;
						}
					}
					
					if (!done) {
						loader.backgroundSpriteSlots.add(newBGSprite);
						loader.backgroundSpriteSlots = Utility.MoveSpriteToCorrectLayerPosition(loader.backgroundSpriteSlots,loader.backgroundSpriteSlots.size()-1);
						done = true;
						}
					
					loader.backgroundSpriteSlots.get(targetSlot).fadeIn();
					}
				break;
			case 0x47:   //BGANIM				Display an animated background (e.g. the talking character faces)
				{
					pos += 2;
					short X = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
					pos+= 2;
					short Y = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
					pos+= 2;
					int FirstCopyFromBGSlot = BitConverter.ToUInt16(currentFunction.filebytes,pos);
					pos+= 2;
					int NumberOfCopyFromBGSlots = BitConverter.ToUInt16(currentFunction.filebytes,pos);	//number of slots to go through after that
					pos+= 2;	
					short waitTime = (short)(BitConverter.ToUInt16(currentFunction.filebytes,pos) * 2);	//possibly
					pos+= 2;
					short numLoops = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//possibly
					pos+= 2;
					boolean useAlpha = (CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos)) == 1);
					pos+= 2;
				
					int targetSlot = loader.backgroundSpriteSlots.size();
				
					if(loader.previousBGAnimIndex != -1) {
						targetSlot = loader.previousBGAnimIndex;
					}
					
					loader.previousBGAnimIndex = targetSlot;
					
					CTSprite newBGAnim = new CTSprite();
					
					newBGAnim.mySlotID = targetSlot;
					newBGAnim.bufferedImage = loader.backgroundSlots.get(FirstCopyFromBGSlot).bufferedImage;
					newBGAnim.abm = new CTAnimatedBitmap();
					
					newBGAnim.WaitTimes = new LinkedList<Short>();
					newBGAnim.MovementsX = new LinkedList<Short>();
					newBGAnim.MovementsY = new LinkedList<Short>();
					

					for (int i = FirstCopyFromBGSlot; i < FirstCopyFromBGSlot + NumberOfCopyFromBGSlots; i++)
						{
						newBGAnim.abm.bufferedImages.add(new ClockTowerBufferedImage(loader.backgroundSlots.get(i).bufferedImage,(short)loader.backgroundSlots.get(i).bufferedImage.getWidth(),(short)loader.backgroundSlots.get(i).bufferedImage.getHeight(),0,0));
						newBGAnim.WaitTimes.add(waitTime);
						newBGAnim.MovementsX.add((short) 0);
						newBGAnim.MovementsY.add((short) 0);
						}
					
					newBGAnim.myName = loader.backgroundSlots.get(FirstCopyFromBGSlot).myName;
					
					newBGAnim.isSprite = true;	//yeah, it's not really a sprite but we want it to animate
					newBGAnim.loop = false;
					newBGAnim.numLoops = numLoops;
					newBGAnim.stickToCamera = true;
					newBGAnim.nonAnimating = false;
					
					newBGAnim.myTransform.position = new Point(Math.round(X*1.25f), Math.round(Y*1.25f));
					newBGAnim.myTransform.scale = new Vector2(1.25f,1.25f);
					
					while (targetSlot >= loader.backgroundSpriteSlots.size()) {
					loader.backgroundSpriteSlots.add(new CTSprite());	
					}
					
					loader.backgroundSpriteSlots.set(targetSlot,newBGAnim);
					}
				break;
			case 0x48: //BGSCROLL                      
                {
				pos += 2;
                int unknown1 = BitConverter.ToUInt16(currentFunction.filebytes, pos);		//? 0xFF
                pos += 2;
                int unknown2 = BitConverter.ToUInt16(currentFunction.filebytes, pos);   //Should be set to 4. Other values seem to stop it from scrolling.
                pos += 2;
                int speedMultiplier = BitConverter.ToUInt16(currentFunction.filebytes, pos);   //scroll speed (lower is slower)
                pos += 2;
                int speedMultiplier2 = BitConverter.ToUInt16(currentFunction.filebytes, pos);   //? speed multiplier where higher is slower
                pos += 2;
                int scrollAmount = BitConverter.ToUInt16(currentFunction.filebytes, pos);   //amount to scroll?
                pos += 2;
				System.out.println("BGSCROLL NEEDS TO BE IMPLEMENTED");
				break;		
				}
			case 0x49:   //PALSET
				pos+= 2;
				System.out.println("PALSET NEEDS TO BE IMPLEMENTED");
				pos += 10;
				break;
			case 0x4A:   //BGWAIT				
				pos += 2;
				while (loader.BGwaiting){
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				}
				break;
			case 0x4B:   //WAIT				
				pos += 2;
				{
				long endTime = System.currentTimeMillis() + (BitConverter.ToInt32(currentFunction.filebytes,pos) * 30);
				while (System.currentTimeMillis() < endTime) {
					loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}
				}
				pos += 4;
				}
				break;
			case 0x4D:   //BOXFILL
				pos += 2;
				pos += 14;
				System.out.println("BOXFILL needs to be implemented!");
				break;
			case 0x4F:   //SETBKCLR
				{
				pos += 2;
				int R = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				int G =  CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				int B =  CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				
				Game.backgroundColor = new Color(R,G,B,255);
				Game.fadeColor = new Color(R,G,B,Game.fadeColor.getAlpha());
				Game.gameWindow.setBackground(Game.backgroundColor);
				}
				break;
			case 0x50:   //MSGCOL
				{
				pos += 2;
				int R = (CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos)));
				pos += 2;
				int G =  (CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos)));
				pos += 2;
				int B =  (CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos)));
				pos += 2;
				int R2 = (CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos)));
				pos += 2;
				int G2 =  (CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos)));
				pos += 2;
				int B2 =  (CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos)));
				pos += 2;
				CTUI.textColour = new Color(R,G,B, 255);
				CTUI.highlightColour = new Color(R2,G2,B2,255);
				}
				break;
			case 0x51:	 //MSGSPD
				pos += 2;
				CTUI.messageSpeed = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				break;
			case 0x52:   //MAPINIT
				{
				pos+= 2;
				short MapSizeX = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+= 2;
				short MapSizeY = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+= 2;
				short unk1 = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+= 2;
				short rightBorderPos = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+= 2;
				short lowerBorderPos = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+= 2;
				short unk2 = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos+= 2;
				
				CTUI.SetBorder(rightBorderPos * 1.25f,lowerBorderPos*1.25f);
				}
				break;
			case 0x53:   //MAPLOAD
				{
					pos += 2;
					int targetSlot = BitConverter.ToUInt16(currentFunction.filebytes,pos);
					pos+= 2;
					
					String bufferedImagepath = "";
					
					while (currentFunction.filebytes[pos] != 0x00){	//load bufferedImage path
						bufferedImagepath += (char)currentFunction.filebytes[pos];
						pos++;
						}
					
					pos += 2; //skip null terminating bytes

                    while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                        pos++;
                        }
						
					String mappath = "";
					
					while (currentFunction.filebytes[pos] != 0x00){	//load map path
						mappath += (char)currentFunction.filebytes[pos];
						pos++;
						}
					
					pos += 2; //skip null terminating bytes

                    while (pos % 2 != 0) //then pad until pos is a multiple of two
                        {
                        pos++;
                        }
						
					bufferedImagepath = FilenameUtils.concat(Game.GAME_ROOT_DIR,bufferedImagepath);
					mappath = FilenameUtils.concat(Game.GAME_ROOT_DIR,mappath);
					
					CTSprite newBackgroundMap = new CTSprite();
					
					newBackgroundMap.myName = new File(bufferedImagepath).getName();

					CTTileMap map = new CTTileMap();
					map.LoadFromPath(mappath);
					newBackgroundMap.bufferedImage = map.MakeBufferedImageFromTileset(bufferedImagepath);
					newBackgroundMap.myName = newBackgroundMap.myName;
					
					newBackgroundMap.myTransform.position.y -= Window.headerSizeY; //for some reason, the backgrounds, unlike the sprites, measure from the very very top corner of the window, underneath the top bar

					loader.backgroundMaps.add(newBackgroundMap);
					}
				break;
			case 0x55:   //SPRENT			- Creates a sprite.
				{
				pos += 2;
				int targetSlot = BitConverter.ToUInt16(currentFunction.filebytes,pos);
				pos += 2;
				short X = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				short Y = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				short layer = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				int animationIndex = currentFunction.filebytes[pos];
				pos++;
				int animGroupNumber = currentFunction.filebytes[pos] & 0x7F;
				
				if ((currentFunction.filebytes[pos] & 0x80) == 0x80){System.out.println("But why was the highest bit set on this animation group request? Animation group was: "+Utility.GetAnimationGroupNameFromID(animGroupNumber));}
				pos++;
				short unk = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				boolean animateOrNot = ((unk & 0x10) == 0x10);
				pos += 2;
				short startingFrame = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				pos += 2;
				
				CTSprite newSprite = new CTSprite();
				
				newSprite.mySlotID = targetSlot;	
				
				CTAnimation newAnimation = new CTAnimation(animGroupNumber, animationIndex, startingFrame);
				
				newSprite.LoadSpriteDataIntoSprite(newAnimation);
					
				newSprite.myTransform.position = new Point(Math.round(X*1.25f), Math.round(Y*1.25f));
				newSprite.isSprite = true;
				newSprite.measuresFromBase = true;
				newSprite.layer = layer;
				
				if (animateOrNot){
					newSprite.numLoops = 1;
					newSprite.nonAnimating = false;
				}else{
					newSprite.numLoops = 0;	//and now we make sure it gets the correct offset, because it will never go through the loop and get it the regular way.
					newSprite.initializeNonAnimatingSprite();
					}
				
				boolean done = false;
				
				for (int i = 0; i < loader.spriteSlots.size(); i++) {
					
					if (loader.spriteSlots.get(i).mySlotID == targetSlot) {
						loader.spriteSlots.set(i, newSprite);
						loader.spriteSlots = Utility.MoveSpriteToCorrectLayerPosition(loader.spriteSlots,i);
						done = true;
						break;
					}
				}
				
				if (!done) {
					loader.spriteSlots.add(newSprite);
					loader.spriteSlots = Utility.MoveSpriteToCorrectLayerPosition(loader.spriteSlots,loader.spriteSlots.size()-1);
					done = true;
					}
				
				if(targetSlot == 0) { //assume the player is always in slot 0? I think it's true, but who knows...
					loader.player = newSprite;
					}				
				}
				break;
			case 0x56:   //SETPROC			Don't know
				pos += 2;
				System.out.println("SETPROC... MAYBE NEEDS TO BE IMPLEMENTED?");		//I say this because it might be irrelevant outside the original engine
				pos += 2;
				break;
			case 0x57:   //SCEINIT
				pos+= 2;
				System.out.println("SCE INIT NEEDS TO BE IMPLEMENTED");
				break;
			case 0x58:   //USERCTL				Sets whether the player can control the character or not
				pos+= 2;
				CTMemory.userControl = BitConverter.ToInt16(currentFunction.filebytes, pos) == 1;
				pos += 2;
				break;
			case 0x59:	  //MAPATTR
				{
				pos += 2;
				System.out.println("MAPATTR NEEDS TO BE IMPLEMENTED");
				pos += 2;
				}
				break;
			case 0x5A:   //MAPPOS			Sets the position of a background, usually the room background?
				{
				pos += 2;
				short X = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				short Y = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				int targetBackgroundSlot = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				if (targetBackgroundSlot == 0){
					Camera.transform.position = new Point(Math.round((X*1.25f)-CTUI.leftBorderPos),(Math.round(Y*1.25f)-CTUI.topBorderPos)-6);
					}
				else{
					loader.backgroundMaps.get(targetBackgroundSlot).myTransform.position = new Point(Math.round((X*1.25f)-CTUI.leftBorderPos),(Math.round(Y*1.25f)-CTUI.topBorderPos)-6);
					}
				}
				break;
			case 0x5C:   //SPRANIM
				{
				pos += 2;
				pos += 8;
				System.out.println("SPRANIM needs to be implemented!");
				}
				break;
			case 0x5D:   //SPRDIR			Changes sprite direction?
				{
				pos += 2;
				int targetSpriteSlot = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				pos += 2;	
				pos += 2;
				short directionX = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				short directionY = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				
				CTSprite target = null;
				
				for (int i = 0; i < loader.spriteSlots.size(); i++) {
					if (loader.spriteSlots.get(i).mySlotID == targetSpriteSlot) {
						target = loader.spriteSlots.get(i);
						break;
					}
				}
				
				if (target != null){
					target.myTransform.flipping = new Point(-directionX,-directionY);
					}
				}
				break;
			case 0x5E:   //GAMEINIT
				pos+= 2;
				System.out.println("GAME INIT NEEDS TO BE IMPLEMENTED");
				break;
			case 0x5F:   //CONTINIT
				pos+= 2;
				System.out.println("CONT INIT NEEDS TO BE IMPLEMENTED");
				break;
			case 0x60:   //SCE END
				pos+= 2;
				System.out.println("SCE END NEEDS TO BE IMPLEMENTED");
				break;
			case 0x61:   //MAPSCROLL
				{
				pos += 2;
				short TargetPosX = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//-1 means none
				pos += 2;
				short TargetPosY = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//-1 means none
				pos += 2;
				short scrollSpeed = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				loader.AddMapScroll(TargetPosX,TargetPosY,scrollSpeed);
				}
				break;
			case 0x62:   //SPRLMT
				pos += 8;
				System.out.println("SPRLMT NEEDS TO BE IMPLEMENTED");
				break;
			case 0x63:   //SPRWALKX
				{
				pos += 2;
				short targetSlot = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				short destPosXMin = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				short destPosXMax = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				short directionToFaceAtEnd = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//Usually -1. Possibly direction, but one script has '12' in this slot. ????	changing this to zero for Jennifer in the yellow foyer cutscene makes her turn around at the end
				pos += 2;
				short unk = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//possibly. changing this to 1 for mary in the yellow foyer cutscene puts her in a hooded robe
				pos += 2;
				
				CTSprite target = null;
				
				for (int i = 0; i < loader.spriteSlots.size(); i++) {
					if (loader.spriteSlots.get(i).mySlotID == targetSlot) {
						target = loader.spriteSlots.get(i);
						break;
					}
				}
				
				target.sprWalkX(destPosXMin, destPosXMax, directionToFaceAtEnd, unk);
				}
				break;
			case 0x66:   //SPRWAIT
				pos += 2;
				short SpriteSlotToWaitFor = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				
				CTSprite target = null;
				
				for (int i = 0; i < loader.spriteSlots.size(); i++) {
					if (loader.spriteSlots.get(i).mySlotID == SpriteSlotToWaitFor) {
						target = loader.spriteSlots.get(i);
						break;
					}
				}
				
				while (target != null && target.busy){
					loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
					}
				break;
			case 0x67:   //SEREQ			Plays a sound effect
				{
				pos+= 2;
				boolean loop = BitConverter.ToInt16(currentFunction.filebytes, pos) == 1;
					pos+=2;
				String path = "";
				while (currentFunction.filebytes[pos] != 0x00){
						path += (char)currentFunction.filebytes[pos];
						pos++;
						}
				pos += 2; //skip null terminating bytes

                while (pos % 2 != 0) //then pad until pos is a multiple of two
                    {
                    pos++;
                    }
				CTAudio newSE = new CTAudio();
				newSE.Init(path,loop, false);
				loader.soundEffects.add(newSE);
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				break;
				}
			case 0x68:   //SNDSTOP			Stops playing sound effects?
				pos+= 2;
				for (int i = loader.soundEffects.size() - 1; i >= 0; i--)
					{
					loader.soundEffects.get(i).myClip.stop();
					loader.soundEffects.remove(i);
					}
				break;
			case 0x69:   //SESTOP				Stops a sound effect
				{
				pos+= 2;
				String path = "";
				while (currentFunction.filebytes[pos] != 0x00){
						path += (char)currentFunction.filebytes[pos];
						pos++;
						}
				pos += 2; //skip null terminating bytes

                while (pos % 2 != 0) //then pad until pos is a multiple of two
                    {
                    pos++;
                    }
				
				if (path.equalsIgnoreCase("ALL")){
					for(int i = loader.soundEffects.size() - 1; i >= 0; i--){
					loader.soundEffects.get(i).myClip.stop();
					loader.soundEffects.remove(i);
					}
				}
				else
				{
					path = new File(Game.GAME_ROOT_DIR,path).getAbsolutePath();
					for(int i = loader.soundEffects.size() - 1; i >= 0; i--){
					if (loader.soundEffects.get(i).myClip.path.equalsIgnoreCase(path)){
					loader.soundEffects.get(i).myClip.stop();
					loader.soundEffects.remove(i);
					break;
						}
					}
				}
				
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				break;
				}
			case 0x6A:	//BGMSTOP			Stop the current loader.music
				pos+=2;
				loader.music.Stop();
				loader.music = new MidiStream();
				loader.music.filebytes = loader.midiMuter;	//sets all channels to note off
				loader.music.Play(false);
				loader.music.Stop();
				break;
			case 0x6B:   //DOORNOSET
				{
				pos += 2;
				System.out.println("DOORNOSET needs to be implemented!");
				}
				break;
			case 0x6C:   //RAND			Get a random number	in the chosen range, and store it in a variable
				{
				pos += 2;
				short min = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//inclusive min
				pos += 2;
				short max = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//inclusive min
				pos += 2;
				short randomResult = (short)Utility.RandomInRange(min,max);
				short bankAndVariable = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos += 2;		
				CTMemory.SetVar(bankAndVariable, randomResult);
				break;
				}
			case 0x6E:   //FAWAIT			Wait for the current fade to finish
				pos+= 2;
				loader.faWaitCarriesOver = true;
				while (CTMemory.currentlyFading){
					loader.faWaitCarriesOver = false;	//it only carries over to another instruction if FaWait is triggered and there's nothing to wait for yet, so in this case, it would be false
					loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
					}
				break;
			case 0x6F: //SCLBLOCK			Who knows. My random guess: Maybe it sets out the collision boundaries for the room
                {
                System.out.println("SCLBLOCK needs to be implemented");
                pos += 2;
                int type = BitConverter.ToInt32(currentFunction.filebytes, pos);
                pos += 4;

                while (currentFunction.filebytes[pos + 1] != (byte) 0xFF)
                    {
					BitConverter.ToUInt16(currentFunction.filebytes, pos);
					pos += 2;
                    } 
                }
                break;
			case 0x71:   //SEREQPV			Plays a sound effect, but with pan and volume parameters maybe?
				{
				pos+= 2;
				boolean loop = BitConverter.ToInt16(currentFunction.filebytes, pos) == 1;
					pos+=2;
				short pan = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos+=2;
				short volume = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos+=2;
				String path = "";
				while (currentFunction.filebytes[pos] != 0x00){
						path += (char)currentFunction.filebytes[pos];
						pos++;
						}
				pos += 2; //skip null terminating bytes

                while (pos % 2 != 0) //then pad until pos is a multiple of two
                    {
                    pos++;
                    }
				CTAudio newSE = new CTAudio();
				newSE.Init(path,loop,false);
				loader.soundEffects.add(newSE);
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				break;
				}
			case 0x72:   //SEREQSPR			Plays a sound effect, but from a sprite's position?
				{
				System.out.println("SEREQSPR needs to be properly implemented (at the moment it just plays a normal sound)");
				pos+= 2;
				boolean loop = BitConverter.ToInt16(currentFunction.filebytes, pos) == 1;
					pos+=2;
				short unk1 = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos+=2;
				short unk2 = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos+=2;
				String path = "";
				while (currentFunction.filebytes[pos] != 0x00){
						path += (char)currentFunction.filebytes[pos];
						pos++;
						}
				pos += 2; //skip null terminating bytes

                while (pos % 2 != 0) //then pad until pos is a multiple of two
                    {
                    pos++;
                    }
				CTAudio newSE = new CTAudio();
				newSE.Init(path,loop,false);
				loader.soundEffects.add(newSE);
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				break;
				}
			case 0x73:   //SCERESET			
				pos+= 2;
				System.out.println("SCE RESET NEEDS TO BE IMPLEMENTED");
				for (int i = loader.marks.size() - 1; i >= 0; i--)
					{
					loader.marks.get(i).destroy();
					loader.marks.remove(i);		
					}
				for (int i = loader.events.size() - 1; i >= 0; i--)
					{
					loader.events.set(i, null);
					loader.events.remove(i);		
					}
				break;
			case 0x74:   //BGSPRENT			- Creates a background sprite.
				{
				pos += 2;
				int targetSlot = BitConverter.ToUInt16(currentFunction.filebytes,pos);
				pos += 2;
				short X = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				short Y = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));
				pos += 2;
				short layer = CTMemory.EvaluateInt16Bank(BitConverter.ToInt16(currentFunction.filebytes,pos));	//possibly
				pos += 2;
				short CopyFromBGSlot = currentFunction.filebytes[pos];	//possibly
				pos += 2;
				short unk2 = currentFunction.filebytes[pos];
				boolean animateOrNot = ((unk2 & 0x10) == 0x10);
				pos += 2;

				CTSprite newBGSprite = new CTSprite();
				
				if (animateOrNot){
					newBGSprite.numLoops = 1;
				}else{
					newBGSprite.numLoops = 0;
				}
				
				newBGSprite.myName = loader.backgroundSlots.get(CopyFromBGSlot).myName;
				newBGSprite.mySlotID = targetSlot;
				newBGSprite.layer = layer;
				
				if (CopyFromBGSlot < loader.backgroundSlots.size()){
					newBGSprite.bufferedImage = loader.backgroundSlots.get(CopyFromBGSlot).bufferedImage;
				}
				
				newBGSprite.measuresFromBase = true; //maybe?

				newBGSprite.myTransform.position = new Point(Math.round(X*1.25f), Math.round(Y*1.25f));
				newBGSprite.stickToCamera = true;
				
				boolean done = false;
				
				for (int i = 0; i < loader.backgroundSpriteSlots.size(); i++) {
					
					if (loader.backgroundSpriteSlots.get(i).mySlotID == targetSlot) {
						loader.backgroundSpriteSlots.set(i, newBGSprite);
						loader.backgroundSpriteSlots = Utility.MoveSpriteToCorrectLayerPosition(loader.backgroundSpriteSlots,i);
						done = true;
						break;
					}
				}
				
				if (!done) {
					loader.backgroundSpriteSlots.add(newBGSprite);
					loader.backgroundSpriteSlots = Utility.MoveSpriteToCorrectLayerPosition(loader.backgroundSpriteSlots,loader.backgroundSpriteSlots.size()-1);
					done = true;
					}
				}
				break;
			case 0x7D:							//BGDISPTRN
				{
					pos += 2;
					short X = BitConverter.ToInt16(currentFunction.filebytes,pos);
					pos+= 2;
					short Y = BitConverter.ToInt16(currentFunction.filebytes,pos);
					pos+= 2;
					int layer = BitConverter.ToUInt16(currentFunction.filebytes,pos);	//possibly
					pos+= 2;
					int CopyFromBGSlot = BitConverter.ToUInt16(currentFunction.filebytes,pos);	//possibly
					pos+= 2;
					pos+= 2;
					pos+= 2;
					boolean useFade = BitConverter.ToUInt16(currentFunction.filebytes, pos) == 1;
					pos+= 2;
					
					int targetSlot = 99999;
					
					CTSprite newBGSprite = new CTSprite();
				
					newBGSprite.mySlotID = targetSlot;

					newBGSprite.bufferedImage = loader.backgroundSlots.get(CopyFromBGSlot).bufferedImage;

					newBGSprite.measuresFromBase = true; //maybe?
					
					newBGSprite.myTransform.position = new Point(Math.round(X*1.25f), Math.round(Y*1.25f));
					newBGSprite.stickToCamera = true;
					
					boolean done = false;
					
					
					
					for (int i = 0; i < loader.backgroundSpriteSlots.size(); i++) {
						
						if (loader.backgroundSpriteSlots.get(i).mySlotID == targetSlot) {
							loader.backgroundSpriteSlots.set(i, newBGSprite);
							loader.backgroundSpriteSlots = Utility.MoveSpriteToCorrectLayerPosition(loader.backgroundSpriteSlots,i);
							done = true;
							break;
						}
					}
					
					if (!done) {
						loader.backgroundSpriteSlots.add(newBGSprite);
						loader.backgroundSpriteSlots = Utility.MoveSpriteToCorrectLayerPosition(loader.backgroundSpriteSlots,loader.backgroundSpriteSlots.size()-1);
						done = true;
						}
					
					if (useFade){
						newBGSprite.fadeIn();
					}
					}	
				break;
			case 0x7B:   //SEPAN			Changes the pan of a sound effect
				{
				pos+= 2;
				short panAmount = BitConverter.ToInt16(currentFunction.filebytes,pos);
				pos+=2;
				String path = "";
				while (currentFunction.filebytes[pos] != 0x00){
						path += (char)currentFunction.filebytes[pos];
						pos++;
						}
				pos += 2; //skip null terminating bytes

                while (pos % 2 != 0) //then pad until pos is a multiple of two
                    {
                    pos++;
                    }
				for (CTAudio audio : loader.soundEffects){
					if (audio.myClip.path.equalsIgnoreCase(path)){
					audio.panAmount = (float)panAmount / 128f;
					break;
					}
				}
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				break;
				}
			case 0x80:   //TMWAIT				WaitForSeconds?
				{
				pos += 2;
				long endTime = System.currentTimeMillis() + (BitConverter.ToInt32(currentFunction.filebytes,pos) * 1000);
				while (System.currentTimeMillis() < endTime) {
					loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}
				}
				pos += 4;
				}
				break;
			case 0x83:   //NEXTCOM					????
				pos+= 2;
				pos+= 2;
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				break;
			case 0x84:   //WORKCLR
				pos+= 2;
				System.out.println("Screen cleared by WORKCLR, hope I've got that right");
				for (int i = loader.spriteSlots.size() - 1; i >= 0; i--){
					if (!loader.spriteSlots.get(i).immuneToDeletion){
						loader.spriteSlots.get(i).destroy();
						loader.spriteSlots.remove(i);}
					}
				for (int i = loader.backgroundSpriteSlots.size() - 1; i >= 0; i--){
					loader.backgroundSpriteSlots.get(i).destroy();
					loader.backgroundSpriteSlots.remove(i);
					}
				break;
			case 0x87:   //AVIPLAY					Plays a video file
				pos+= 2;
				System.out.println("AVIPLAY NEEDS TO BE IMPLEMENTED");
                pos += 2;
                pos += 2;
				pos += 2;
				pos += 2;
				pos += 2;

				String avipath = "";
   				while (currentFunction.filebytes[pos] != 0x00)
                    {
                    avipath += (char)currentFunction.filebytes[pos];
                    pos++;
                    }
                pos += 2; //skip null terminating bytes

                while (pos % 2 != 0) //then pad until pos is a multiple of two
                    {
                    pos++;
                    }
				loader.currentADOThread = this;  try {synchronized(this){this.wait();}} catch (InterruptedException e) {e.printStackTrace();}          //formerly yield return null
				break;
			case 0x88:   //AVISTOP				 Stops the playing video
				pos+= 2;
				break;
			case 0xB1:	//CUSTOM OPCODE (register client on server)
				{
				pos+=2;
				int clientID = BitConverter.ToInt32(currentFunction.filebytes, pos);
				pos+=4;
				
				String clientName = "";
				while(BitConverter.ToInt16(currentFunction.filebytes, pos) != (short)0xFFFF) {
					clientName += new String(currentFunction.filebytes,pos,2, StandardCharsets.UTF_16);
					pos+=2;
				}
				pos+=2; //skip 0xFFFF padding
				
				if(!Game.isServer && clientID == Game.client.myClientInfo.ID) {
					break; //no need to spawn a dummy of ourselves!
				}
				
				ClientInfo client = new ClientInfo(clientID,clientName);
				
				boolean alreadyAdded = false;
				
				for (int i = 0; i < Server.clients.size(); i++) {
					if (Server.clients.get(i).ID == clientID) {
						alreadyAdded = true;
					}
				}
				
				if (!alreadyAdded) {
					client.myPlayer = SpawnNewDummyPlayer(client);
					Server.clients.add(client);
					System.out.println("Added a new client (or dummy client player) to the game, ID:"+clientID+", name:"+clientName);
					}
				
				if (Game.isServer) {		
					//register the server player on clients too
					Game.server.SendMessageToClients(OnlineMessages.UpdatePlayerDummiesOnClients(Game.server.myClientInfo));
					}
				break;
				}
			case 0xB2:	//CUSTOM OPCODE MSGONLINESETSPEAKER
				{
					pos += 2;
					int clientID = BitConverter.ToInt32(currentFunction.filebytes, pos);
					pos+=4;
					
					int targetSlot = loader.backgroundSpriteSlots.size();
				
					if(loader.previousBGAnimIndex != -1) {
						targetSlot = loader.previousBGAnimIndex;
					}
					
					loader.previousBGAnimIndex = targetSlot;
					
					CTSprite newBGAnim = new CTSprite();
					ClientInfo clientInfo = null;
					
					for (int i = 0; i < Server.clients.size(); i++) {	
						if (Server.clients.get(i).ID == clientID) {
							clientInfo = Server.clients.get(i);
							break;
						}					
					}
					
					newBGAnim.bufferedImage = clientInfo.avatar;
					
					if (newBGAnim.bufferedImage == null) {
						newBGAnim.bufferedImage = new BufferedImage(80,80,BufferedImage.TRANSLUCENT);
					}
					
					newBGAnim.mySlotID = targetSlot;
					
					newBGAnim.abm = new CTAnimatedBitmap();
					
					newBGAnim.WaitTimes = new LinkedList<Short>();
					newBGAnim.MovementsX = new LinkedList<Short>();
					newBGAnim.MovementsY = new LinkedList<Short>();
					newBGAnim.abm.bufferedImages.add(new ClockTowerBufferedImage(newBGAnim.bufferedImage,(short)newBGAnim.bufferedImage.getWidth(),(short)newBGAnim.bufferedImage.getHeight(),0,0));
					newBGAnim.WaitTimes.add((short) 28);
					newBGAnim.MovementsX.add((short) 0);
					newBGAnim.MovementsY.add((short) 0);
					
					newBGAnim.myName = clientInfo.name + "_avatar";
					
					newBGAnim.isSprite = true;	//yeah, it's not really a sprite but we want it to animate
					newBGAnim.loop = false;
					newBGAnim.numLoops = 1;
					newBGAnim.stickToCamera = true;
					newBGAnim.nonAnimating = false;
					
					newBGAnim.myTransform.position = new Point(Math.round(32*1.25f), Math.round(328*1.25f));
					newBGAnim.myTransform.scale = new Vector2(1.25f,1.25f);
					
					while (targetSlot >= loader.backgroundSpriteSlots.size()) {
					loader.backgroundSpriteSlots.add(new CTSprite());	
					}
					
					loader.backgroundSpriteSlots.set(targetSlot,newBGAnim);
				}
				break;

			case 0xB3:   //CUSTOM OPCODE UPDATECLIENTAVATARONSERVER
			{
				pos+=2;
				int clientID = BitConverter.ToInt32(currentFunction.filebytes, pos);
				pos+=4;
				
				int len = BitConverter.ToInt32(currentFunction.filebytes, pos);
				pos+=4;
				
				BufferedImage img = null;
				try {
					img = ImageIO.read(new ByteArrayInputStream(currentFunction.filebytes, pos, len));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				pos += len;
				
				for (int i = 0; i < Server.clients.size(); i++) {	
					if (Server.clients.get(i).ID == clientID) {
						Server.clients.get(i).avatar = img;
					}
				}
				
				System.out.println("Avatar update took place");
			}
				break;
			case 0xB4:   //UPDATEMULTIPLAYERANIM
				{
				pos += 2;
				int clientID = BitConverter.ToInt32(currentFunction.filebytes, pos);
				pos+=4;
				int animGroup = BitConverter.ToInt32(currentFunction.filebytes, pos);
				pos+=4;
				int animIndex = BitConverter.ToInt32(currentFunction.filebytes, pos);
				pos+=4;
				int startFrame = BitConverter.ToUInt16(currentFunction.filebytes, pos);
				pos+=2;
				boolean loop = currentFunction.filebytes[pos] == 1;
					pos++;
				int numLoops = BitConverter.ToUInt16(currentFunction.filebytes, pos);
				pos+=2;
				int X = BitConverter.ToInt32(currentFunction.filebytes, pos);
				pos+=4;
				int Y = BitConverter.ToInt32(currentFunction.filebytes, pos);
				pos+=4;
				int flipX = currentFunction.filebytes[pos];
				pos++;
				
				CTAnimation newAnimation = new CTAnimation(animGroup, animIndex, startFrame, loop, numLoops);
				
				for (int i = 0; i < Server.clients.size(); i++) {
					
					if (Server.clients.get(i).ID == clientID) {
						ClientInfo client = Server.clients.get(i);
						//Display the animation on the server side
						client.myPlayer.loop = false;
						client.myPlayer.nonAnimating = false;
						client.myPlayer.myTransform.position = new Point(X,Y);
						client.myPlayer.myTransform.flipping.x = flipX;
						client.myPlayer.queuedAnims.add(newAnimation);
						client.myPlayer.PlayNextQueuedAnim();
						
						//System.out.println("Trying to sync a player position");
						
						if (Game.isServer) {
							//TODO: send the update to all the other clients
							
							//MIGHT NOT BE NEEDED ANYMORE BECAUSE THE SERVER RELAYS ITS MESSAGES TO ALL CLIENTS BY DEFAULT ANYWAY
							//Game.server.SendMessageToClients(OnlineMessages.UpdateMyAnimationOnServer(client, animGroup, animIndex, startFrame, loop, numLoops, client.myPlayer.myTransform.position, flipX));
							
							} else {
								//System.out.println("Trying to sync a player position on the client");
							}
						break;
						}
					}
				}
				break;
			default:
			{
				System.out.println("Instruction " + currentFunction.filebytes[pos] + " not implemented!");
				return; //thread should now end (I hope)
			}
		}
	}
		
	//now return to wherever we came from before we came here (e.g. from a JMP)
	
	if (!isCall && loader.returnStack.size() > 0){
		ADOScript returnTo = loader.returnStack.get(loader.returnStack.size() - 1).script;
		int returnToFunctionIndex = loader.returnStack.get(loader.returnStack.size() - 1).function;
		int returnToPos = loader.returnStack.get(loader.returnStack.size() - 1).returnpos;
		
		loader.returnStack.remove(loader.returnStack.size() - 1);
		
		//System.out.println("Return stack activated, returning to function "+returnToFunctionIndex + ", pos "+returnToPos);
		
		ExecuteADOFunction executeADO = new ExecuteADOFunction(returnTo, returnToFunctionIndex, returnToPos);
		System.out.println(this.getName());
		executeADO.oldThread = this;
		executeADO.start();	//Start executing the ADO instructions
		}
	}

private CTSprite SpawnNewDummyPlayer(ClientInfo clientInfo) {
	
	CTSprite newSprite = new CTSprite();
	
	CTAnimation newAnimation = new CTAnimation(1, 1, 0);
	
	newSprite.LoadSpriteDataIntoSprite(newAnimation);
		
	newSprite.myTransform.position = new Point(Math.round(200*1.25f), Math.round(590*1.25f));
	newSprite.isSprite = true;
	newSprite.measuresFromBase = true;
	newSprite.layer = 50;
	newSprite.mySlotID = clientInfo.ID;
	
	newSprite.numLoops = 1;
	newSprite.nonAnimating = false;
	
	newSprite.immuneToDeletion = true;
	newSprite.isOtherPlayer = true;
	
	boolean done = false;
	
	for (int i = 0; i < loader.spriteSlots.size(); i++) {
		
		if (loader.spriteSlots.get(i).mySlotID == clientInfo.ID) {
			loader.spriteSlots.set(i, newSprite);
			loader.spriteSlots = Utility.MoveSpriteToCorrectLayerPosition(loader.spriteSlots,i);
			done = true;
			break;
		}
	}
	
	if (!done) {
		loader.spriteSlots.add(newSprite);
		loader.spriteSlots = Utility.MoveSpriteToCorrectLayerPosition(loader.spriteSlots,loader.spriteSlots.size()-1);
		done = true;
		}
	
	return newSprite;
	}
}