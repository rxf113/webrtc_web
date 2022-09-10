package com.rxf113.chat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 启动类
 *
 * @author rxf113
 */
public class OnlineVideo {

    /**
     * 自定义ChannelInitializer
     */
    public void init(int port) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup boos = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap.group(boos, worker);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new CustomChannelInitializer());
        try {
            Channel channel = serverBootstrap.bind(port).sync().channel();
            System.out.println("服务端启动 端口:" + port);
            System.out.println("本地启动成功!, 页面请访问 http://localhost:9000");
            channel.closeFuture().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}



