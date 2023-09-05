package com.clockTower.online;

import java.util.LinkedList;

import com.clockTower.Utility.Utility;
import com.clockTower.main.Game;
import com.clockTower.main.loader;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class Server extends Thread
{
	public int port;
	public boolean running = false;

	public EventLoopGroup bossGroup;
	public EventLoopGroup workerGroup;
	public ServerBootstrap bootstrap;
	
	
	public static LinkedList<ClientInfo> clients = new LinkedList<ClientInfo>();
	
	public ClientInfo myClientInfo; //this is what the clients will draw from when they need to teat the server as just another player

	public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	
    public void stopServer()
    {
    	if (bossGroup != null) {
    		bossGroup.shutdownGracefully();
    	}
        
    	if (workerGroup != null) {
    		workerGroup.shutdownGracefully();
    	}
    	
        running = false;
    }
    
    public void run() {
    	
    	  running = true;
          
          try {
			bootstrap.bind(port).sync().channel().closeFuture().sync();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    public Server(int port)
    {
    	myClientInfo = new ClientInfo(Utility.RandomInRange(-Integer.MIN_VALUE, Integer.MAX_VALUE-1), "Server_user");
    	myClientInfo.myPlayer = loader.player;
    	

        	this.port = port;
            System.out.println("Starting server on port "+port);

            Game.isServer = true;
           
            
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            
            bootstrap = new ServerBootstrap()
            		.group(bossGroup, workerGroup)
            		.channel(NioServerSocketChannel.class)
            		.childHandler(new ChatServerInitializer())
            		.option(ChannelOption.SO_BACKLOG, 128)       
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

	public void SendMessageToClients(byte[] msg) {
		
		for (Channel channel : channels) {
				channel.writeAndFlush(Unpooled.wrappedBuffer(msg));
		}
	}
}
