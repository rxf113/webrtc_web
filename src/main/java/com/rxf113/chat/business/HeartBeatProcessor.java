package com.rxf113.chat.business;

import com.alibaba.fastjson.JSON;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import com.rxf113.chat.server.DTO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;


/**
 * 心跳处理器
 *
 * @author rxf113
 */
public class HeartBeatProcessor implements BusinessProcessor<DTO> {


    public static final String HEART_BEAT_RESPONSE;

    static {
        DTO dto = new DTO();
        dto.setType(SendTypeEnum.heartBeat.getValue());
        HEART_BEAT_RESPONSE = JSON.toJSONString(dto);
    }

    @Override
    public ReceiveTypeEnum supportType() {
        return ReceiveTypeEnum.heartBeat;
    }

    @Override
    public void process(Channel channel, DTO reqData) {
        System.out.println("receive heart beat ...");
        channel.writeAndFlush(new TextWebSocketFrame(HEART_BEAT_RESPONSE));
    }
}
