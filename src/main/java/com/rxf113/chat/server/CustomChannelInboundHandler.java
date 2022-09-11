package com.rxf113.chat.server;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rxf113.chat.business.*;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.utils.ChannelUtil;
import com.rxf113.chat.utils.StaticFileUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 入站处理
 *
 * @author rxf113
 */
public class CustomChannelInboundHandler extends SimpleChannelInboundHandler<Object> {

    /**
     * name - userInfo
     */
    public static final Map<String, UserInfo> NAME_INFO_MAP = new HashMap<>();
    /**
     * channelId - name
     */
    public static final Map<Channel, String> CHANNEL_NAME_MAP = new HashMap<>();

    public static final Map<Channel, RoomInfo> CHANNEL_ROOMS_MAP = new HashMap<>();

    Map<ReceiveTypeEnum, BusinessProcessor<DTO>> type2ProcessorMap;

    public CustomChannelInboundHandler() {
        List<BusinessProcessor<DTO>> processors = Arrays.asList(
                new CallProcessor(),
                new AcceptProcessor(),
                new HangUpProcessor(),
                new HeartBeatProcessor(),
                new LoginProcessor(),
                new SendAnswerProcessor(),
                new SendICEProcessor(),
                new SendOfferProcessor());
        type2ProcessorMap = processors.stream().collect(Collectors.toMap(BusinessProcessor::supportType, processor -> processor));
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void channelRead0(ChannelHandlerContext handlerContext, Object obj) {
        if (obj instanceof FullHttpRequest) {
            //http 只处理静态文件
            HttpResponseStatus httpResponseStatus = HttpResponseStatus.OK;
            FullHttpRequest httpRequest = (FullHttpRequest) obj;
            String uri = httpRequest.uri();
            byte[] fileBytes;
            if ("/".equals(uri.trim())) {
                uri = "/index.html";
            }
            try {
                fileBytes = StaticFileUtil.getFileBytes(uri);
            } catch (Exception e) {
                fileBytes = new byte[0];
                httpResponseStatus = HttpResponseStatus.NOT_FOUND;
            }

            // 1.设置响应
            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus,
                    Unpooled.copiedBuffer(fileBytes));
            //响应
            handlerContext.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        } else //websocket
            if (obj instanceof TextWebSocketFrame) {
                TextWebSocketFrame o = (TextWebSocketFrame) obj;
                Map map = JSON.parseObject(o.text(), Map.class);
                Channel channel = handlerContext.channel();
                DTO data = JSON.parseObject(map.get("data").toString(), DTO.class);
                Integer type = data.getType();
                ReceiveTypeEnum receiveTypeEnum = ReceiveTypeEnum.getReceiveTypeEnum(type);

                //业务处理
                BusinessProcessor<DTO> processor = type2ProcessorMap.get(receiveTypeEnum);
                processor.process(channel, data);
            }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception {
        if (throwable instanceof com.rxf113.chat.server.CustomException) {
            com.rxf113.chat.server.DTO exceptionData = new com.rxf113.chat.server.DTO();
            exceptionData.setMsg(throwable.getMessage());
            exceptionData.setType(0);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(exceptionData)));
        } else {
            super.exceptionCaught(ctx, throwable);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String name = CHANNEL_NAME_MAP.get(channel);
        CHANNEL_NAME_MAP.remove(channel);
        com.rxf113.chat.server.UserInfo userInfo = NAME_INFO_MAP.get(name);
        NAME_INFO_MAP.remove(name);
        //移除room信息
        if (userInfo != null) {
            if (userInfo.getStatus() == 2) {
                Channel[] channels = ChannelUtil.getCallReChannel(channel);
                Channel remoteChannel = channels[0] == channel ? channels[1] : channels[0];
                //通知断开
                com.rxf113.chat.server.DTO cutOffData = new com.rxf113.chat.server.DTO();
                cutOffData.setType(ReceiveTypeEnum.disconnection.getValue());
                cutOffData.setMsg("对方断开");
                remoteChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(cutOffData)));
                CHANNEL_ROOMS_MAP.remove(channels[0]);
                CHANNEL_ROOMS_MAP.remove(channels[1]);
            }
        }
    }
}