package com.clockTower.online;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public class ChatClientInitializer extends ChannelInitializer<SocketChannel> {

	@Override
	protected void initChannel(SocketChannel arg0) throws Exception {
		
		ChannelPipeline pipeline = arg0.pipeline();
		//pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192,Delimiters.lineDelimiter()));
		pipeline.addLast("decoder", new ByteArrayDecoder());		
		pipeline.addLast("encoder", new ByteArrayEncoder());
		pipeline.addLast("handler", new ChatClientHandler());

	}
}
