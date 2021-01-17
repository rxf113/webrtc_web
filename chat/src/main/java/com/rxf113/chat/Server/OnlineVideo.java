package com.rxf113.chat.Server;

import com.alibaba.fastjson.JSONObject;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rxf113
 */
@SuppressWarnings("all")
public class OnlineVideo {

    /**
     * 自定义ChannelInitializer
     */
    public void init(Integer port) {
        port = port == null ? 9000 : port;
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup boos = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap.group(boos, worker);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new CustomChannelInitializer());
        try {
            Channel channel = serverBootstrap.bind(port).sync().channel();
            System.out.println("服务端启动 端口:" + port);
            channel.closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        OnlineVideo tcpServer = new OnlineVideo();
        tcpServer.init(null);
    }
}



