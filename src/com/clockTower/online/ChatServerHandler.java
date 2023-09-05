package com.clockTower.online;

import com.clockTower.fileUtilities.ADOScript;
import com.clockTower.main.ExecuteADOFunction;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServerHandler extends ChannelInboundHandlerAdapter {

	private static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	
	@Override
	public void handlerAdded(ChannelHandlerContext arg0) throws Exception {
		Channel incoming = arg0.channel();
		System.out.println("New client joined");
		
		channels.add(arg0.channel());
		
		Server.channels = channels;
	}
	

	@Override
	public void handlerRemoved(ChannelHandlerContext arg0) throws Exception {
		Channel incoming = arg0.channel();
		System.out.println("A client left");
		
		channels.remove(arg0.channel());
		
		Server.channels = channels;
	}
	
	
	@Override
	public void channelRead(ChannelHandlerContext arg0, Object msg) throws Exception {
		//System.out.println("received message "+((byte[])msg).length);
		
		 if (((byte[])msg).length > 1 && ((byte[])msg)[1] == (byte)0xFF) {
	          	//treat as ADO command
	          	ExecuteADOFunction executeADO = new ExecuteADOFunction(new ADOScript((byte[])msg), 0, 0);
	      		executeADO.start();	//Start executing the ADO instructions
	          }
		
		Channel incoming = arg0.channel();  //relay the message from a client to all the other clients too
		for (Channel channel : channels) {
			if (channel != incoming) {
				channel.writeAndFlush(Unpooled.wrappedBuffer((byte[])msg));
			}
		}
	}
}
