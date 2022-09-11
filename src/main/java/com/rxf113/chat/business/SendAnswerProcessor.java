package com.rxf113.chat.business;

import com.alibaba.fastjson.JSON;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import com.rxf113.chat.server.DTO;
import com.rxf113.chat.server.RoomInfo;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static com.rxf113.chat.server.CustomChannelInboundHandler.CHANNEL_ROOMS_MAP;

/**
 * 向对端 发送 webrtc answer
 *
 * @author rxf113
 */
public class SendAnswerProcessor implements BusinessProcessor<DTO> {
    @Override
    public ReceiveTypeEnum supportType() {
        return ReceiveTypeEnum.sendAnswer;
    }

    @Override
    public void process(Channel channel, DTO reqData) {
        sendAnswer(channel, reqData.getMsg());
    }


    private void sendAnswer(Channel channel, String msg) {
        RoomInfo currentRoom = CHANNEL_ROOMS_MAP.get(channel);
        Channel remoteChannel = currentRoom.getCallChannel();
        DTO answerData = new DTO();
        answerData.setType(SendTypeEnum.receiveAnswer.getValue());
        answerData.setMsg(msg);
        remoteChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(answerData)));
    }
}
