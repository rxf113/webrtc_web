package com.rxf113.chat.server;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import static com.rxf113.chat.server.CustomChannelInboundHandler.heartBeatData;

class CustomChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addLast(new HttpServerCodec());
        socketChannel.pipeline().addLast(new ChunkedWriteHandler());
        socketChannel.pipeline().addLast(new HttpObjectAggregator(1024 * 64));

        socketChannel.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
        socketChannel.pipeline().addLast(new CustomChannelInboundHandler());
        //心跳检测
        socketChannel.pipeline().addFirst(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));

        socketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void userEventTriggered(ChannelHandlerContext ctx,
                                           Object evt) throws Exception {
                if (evt instanceof IdleStateEvent) {
                    System.out.println("send heart beat ...");
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(heartBeatData)))
                            .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                } else {
                    super.userEventTriggered(ctx, evt);
                }
            }
        });
    }
}