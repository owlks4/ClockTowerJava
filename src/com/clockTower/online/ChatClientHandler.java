package com.clockTower.online;

import com.clockTower.fileUtilities.ADOScript;
import com.clockTower.main.ExecuteADOFunction;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ChatClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext arg0, Object msg) throws Exception {
		
		
		//System.out.println("Received a message of length "+((byte[])msg).length);
		
		 if (((byte[])msg).length > 1 && ((byte[])msg)[1] == (byte)0xFF) {
	          	//treat as ADO command
	          	ExecuteADOFunction executeADO = new ExecuteADOFunction(new ADOScript((byte[])msg), 0, 0);
	      		executeADO.start();	//Start executing the ADO instructions
	          }
	}
}
