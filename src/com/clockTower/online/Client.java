package com.clockTower.online;

import com.clockTower.Utility.Utility;
import com.clockTower.main.Game;
import com.clockTower.main.loader;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;



public class Client
{
	public ClientInfo myClientInfo;
	

	String host;
	int serverPort;
	byte[] dummy = new byte[1];
	
	EventLoopGroup group;
	Bootstrap bootstrap;
	Channel channel;
	
	public boolean active;
	
	 public Client(String _host,int _serverPort,String _name) {
		
		 host = _host;
		 serverPort = _serverPort;
		 
		 if (host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1")) {
			 host = "0.0.0.0";
		 }
		 
		 System.out.println("Trying to connect client to server '" +host+ "' on port " +serverPort);
		
		 group = new NioEventLoopGroup();
		 
		 try {
			 bootstrap = new Bootstrap()
					 .group(group)
					 .channel(NioSocketChannel.class)
					 .handler(new ChatClientInitializer());
			 
			 channel = bootstrap.connect(host,serverPort).sync().channel();
			 
			 System.out.println(channel.localAddress());
		 
			 Game.client = this;
			 
			 myClientInfo = new ClientInfo(Utility.RandomInRange(-Integer.MIN_VALUE, Integer.MAX_VALUE-1),_name);
			 myClientInfo.myPlayer = loader.player;
			 
			 
			 active = true;
			 //System.out.println(sendMessage("Hello, this is a message from a client who has connected to the server"));
			 sendBytes(OnlineMessages.RegisterMeOnServer(myClientInfo));
			 sendBytes(OnlineMessages.UpdateAvatarOnServer(myClientInfo));
			 sendBytes(OnlineMessages.SendChatMessage(myClientInfo,"Hello, this is a message from a client who \nhas connected to the server"));
		 }
		 catch (Exception e) {
			 System.out.println("Failed to connect to server!");
		 }
		}
	 
	 public byte[] sendBytes(byte[] byteMsg) {
		    channel.writeAndFlush(Unpooled.wrappedBuffer(byteMsg));   //.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
		
		    return null;
		}
	 

	public void heartbeat() {
			if(active) {
				sendBytes(dummy);
			}
	}
	
	public void CloseConnection() {
		if (group != null) {
			active = false;
			group.shutdownGracefully();
			}
	}
}