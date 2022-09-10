package com.rxf113.chat.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import com.rxf113.chat.server.DTO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static com.rxf113.chat.server.CustomChannelInboundHandler.channelRooms;

/**
 * @author rxf113
 */
public class SendOfferProcessor implements BusinessProcessor<DTO> {
    @Override
    public ReceiveTypeEnum supportType() {
        return ReceiveTypeEnum.sendOffer;
    }

    @Override
    public void process(Channel channel, DTO reqData) {
        sendOffer(channel, reqData.getMsg());
    }

    /**
     * offer answer 消息传递
     *
     * @param channel
     * @param msg
     */
    private void sendOffer( Channel channel, String msg) {
        com.rxf113.chat.server.RoomInfo currentRoom = channelRooms.get(channel);
        Channel remoteChannel = currentRoom.getReceiveChannel();
        com.rxf113.chat.server.DTO offerData = new com.rxf113.chat.server.DTO();
        offerData.setType(SendTypeEnum.receiveOffer.getValue());
        offerData.setMsg(msg);
        remoteChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(offerData)));
    }

}
