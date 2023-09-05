package com.clockTower.online;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.clockTower.Utility.Utility;
import com.clockTower.fileUtilities.BitConverter;
import com.clockTower.main.Game;

public class OnlineMessages {

	//Class for composing ADO commands for online use
	
	public static byte[] RegisterMeOnServer(ClientInfo myClientInfo) {	//create an ADO command to register the client and send to the server
		 
		 byte[] output = new byte[6+(myClientInfo.name.length()*2)+2];
		 output[0] = (byte) 0xB1;
		 output[1] = (byte) 0xFF;
		 System.arraycopy(Utility.GetBytes(myClientInfo.ID), 0, output, 2, 4);
		 
		 byte[] nameAsBytes = null;

		nameAsBytes = myClientInfo.name.getBytes(StandardCharsets.UTF_16);

		System.arraycopy(nameAsBytes, 2, output, 6, nameAsBytes.length-2);

		 output[6+(myClientInfo.name.length()*2)] = (byte)0xFF;
		 output[6+(myClientInfo.name.length()*2)+1] = (byte)0xFF;
		 
		 return output;
	 }
	
	public static byte[] SendChatMessage(ClientInfo myClientInfo, String message) {	//create an ADO command to send a chat message
		 
		//format
		
		//2 bytes MSGCLR
		//2 bytes MSGONLINESETSPEAKER
		//4 bytes Client ID
		//2 bytes MSGOUT
		//4 bytes F0 00 F0 00 padding (well, it's not padding, but it's not currently used)
		//? bytes The message follows in SHIFT-JIS
		//2 bytes 28FF message terminator
		
		byte[] msgAsBytes = null; 
		 
		 try {
		 msgAsBytes = message.getBytes("SHIFT-JIS");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		 byte[] output = new byte[14+(msgAsBytes.length)+2];
		 output[0] = (byte) 0x40; //MSGCLR
		 output[1] = (byte) 0xFF;
	
		 output[2] = (byte) 0xB2; //MSGONLINESETSPEAKER
		 output[3] = (byte) 0xFF;
		 System.arraycopy(Utility.GetBytes(myClientInfo.ID), 0, output, 4, 4);
		 
		 output[8] = (byte) 0x33; //MSGOUT
		 output[9] = (byte) 0xFF;
		 output[10] = (byte) 0x00;
		 output[11] = (byte) 0xF0;		
		 output[12] = (byte) 0x00;
		 output[13] = (byte) 0xF0;	
	
		System.arraycopy(msgAsBytes, 0, output, 14, msgAsBytes.length);

		 output[14+(msgAsBytes.length)] = (byte)0x28;
		 output[14+(msgAsBytes.length)+1] = (byte)0xFF;
		 
		 return output;
	 }
	
	public static byte[] UpdateAvatarOnServer(ClientInfo myClientInfo) {	//create an ADO command to set this client's avatar on the server side

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		if(myClientInfo.avatar == null) {
			
			myClientInfo.avatar = new BufferedImage(80,80,BufferedImage.TRANSLUCENT);

			for (int y = 0; y < 80; y++) {
				for (int x = 0; x < 80; x++) {
					myClientInfo.avatar.setRGB(x, y, Color.white.getRGB());	
				}
			}
		}
		
		if(myClientInfo.avatar.getWidth() != 80 || myClientInfo.avatar.getHeight() != 80) {
			myClientInfo.avatar = (BufferedImage) myClientInfo.avatar.getScaledInstance(80, 80, BufferedImage.TRANSLUCENT);
		}
		
		File f = new File(Game.GAME_ROOT_DIR,"avatar_temp");
			
		//write it to a gif and back to make it smaller
		
        try {
        	ImageIO.write(myClientInfo.avatar, "gif", f);
        	stream.write(new byte[] {(byte)0xB3,(byte)0xFF});
        	stream.write(BitConverter.GetBytes(myClientInfo.ID));
        	byte[] gif = FileUtils.readFileToByteArray(f);
        	stream.write(gif.length);
        	stream.write(gif);
        	FileUtils.forceDelete(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        return stream.toByteArray();
	}
	
	public static byte[] UpdateMyAnimationOnServer(ClientInfo myClientInfo, int animGroup, int animIndex, int startFrame, boolean loop, int numLoops, Point position, int flipX){
		
		 byte[] output = new byte[28];
		 output[0] = (byte) 0xB4;
		 output[1] = (byte) 0xFF;
		 System.arraycopy(Utility.GetBytes(myClientInfo.ID), 0, output, 2, 4);
		 System.arraycopy(Utility.GetBytes(animGroup), 0, output, 6, 4);
		 System.arraycopy(Utility.GetBytes(animIndex), 0, output, 10, 4);
		 System.arraycopy(Utility.GetBytes((short)startFrame), 0, output, 14, 2);
		 if (loop) {output[16] = 1;} else {output[16] = 0;}
		 System.arraycopy(Utility.GetBytes((short)numLoops), 0, output, 17, 2);
		 System.arraycopy(Utility.GetBytes(position.x), 0, output, 19, 4);
		 System.arraycopy(Utility.GetBytes(position.y), 0, output, 23, 4);
		 output[27] = (byte)flipX;
		 System.out.println("Sending animation update");
		 return output;
	}

	public static byte[] UpdatePlayerDummiesOnClients(ClientInfo client) {
		// run by the server to send an update to all clients
		
		 LinkedList<byte[]> registries = new LinkedList<byte[]>();
		 
		 for (int i = 0; i < Server.clients.size(); i++) {
			 registries.add(RegisterMeOnServer(Server.clients.get(i)));
		 }
		 
		 registries.add(RegisterMeOnServer(Game.server.myClientInfo)); //also register the server, as it needs to appear as a dummy for the other players
		 
		 int len = 0;
		 
		 for (int i = 0; i < registries.size(); i++) {
			 len += registries.get(i).length;
		 }
		 
		 byte[] output = new byte[len];
		 
		 int pos = 0;
		 
		 for (int i = 0; i < registries.size(); i++) {
			 byte[] registration = registries.get(i);
			 for (int j = 0; j < registration.length; j++) {
				 output[pos] = registration[j];
				 pos++;
			 }
		 }
		 
		return output;
	}
}