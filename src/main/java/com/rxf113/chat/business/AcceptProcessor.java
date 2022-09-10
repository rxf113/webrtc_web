package com.rxf113.chat.business;

import com.alibaba.fastjson.JSONObject;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import com.rxf113.chat.server.DTO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static com.rxf113.chat.server.CustomChannelInboundHandler.channelRooms;

public class AcceptProcessor implements BusinessProcessor<DTO> {

    @Override
    public ReceiveTypeEnum supportType() {
        return ReceiveTypeEnum.accept;
    }

    @Override
    public void process(Channel channel, DTO reqData) {
        accept(reqData, channel);
    }


    /**
     * 接受
     *
     * @param reqData
     * @param channel
     */
    private void accept(DTO reqData, Channel channel) {
        reqData.setType(SendTypeEnum.accepted.getValue());
        getRemoteChannel(channel).writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(reqData)));
    }


    /**
     * 获取对端连接
     *
     * @param channel
     * @return Channel
     */
    private Channel getRemoteChannel(Channel channel) {
        Channel[] channels = getCallReChannel(channel);
        return channels[0] == channel ? channels[1] : channels[0];
    }


    /**
     * 获取呼叫应答连接
     *
     * @param channel
     * @return Channel[] 0 呼叫 1应答
     */
    private Channel[] getCallReChannel(Channel channel) {
        com.rxf113.chat.server.RoomInfo roomInfo = channelRooms.get(channel);
        if (roomInfo == null) {
            throw new com.rxf113.chat.server.CustomException("房间已解散!");
        }
        return new Channel[]{roomInfo.getCallChannel(), roomInfo.getReceiveChannel()};
    }
}
