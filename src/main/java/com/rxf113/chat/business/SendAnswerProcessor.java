package com.rxf113.chat.business;

import com.alibaba.fastjson.JSON;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import com.rxf113.chat.server.DTO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static com.rxf113.chat.server.CustomChannelInboundHandler.channelRooms;

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
        com.rxf113.chat.server.RoomInfo currentRoom = channelRooms.get(channel);
        Channel remoteChannel = currentRoom.getReceiveChannel();
        com.rxf113.chat.server.DTO offerData = new com.rxf113.chat.server.DTO();
        offerData.setType(SendTypeEnum.receiveOffer.getValue());
        offerData.setMsg(msg);
        remoteChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(offerData)));
    }
}
