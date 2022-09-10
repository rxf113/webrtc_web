package com.rxf113.chat.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import com.rxf113.chat.server.CustomException;
import com.rxf113.chat.server.DTO;
import com.rxf113.chat.server.RoomInfo;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static com.rxf113.chat.server.CustomChannelInboundHandler.*;

public class HangUpProcessor implements BusinessProcessor<DTO> {

    @Override
    public ReceiveTypeEnum supportType() {
        return ReceiveTypeEnum.hangUp;
    }

    @Override
    public void process(Channel channel, DTO reqData) {
        hangUp(channel);
    }


    /**
     * 挂断
     *
     * @param channel
     */
    private void hangUp(Channel channel) {
        DTO sendMsgData = new DTO();
        sendMsgData.setType(SendTypeEnum.hangUp.getValue());
        sendMsgData.setMsg("对方已挂断!");
        getRemoteChannel(channel).writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(sendMsgData)));
        Channel[] channels = getCallReChannel(channel);
        userInfoModify(channels[0], channels[1], 1);
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
     * 用户状态修改
     *
     * @param callChannel
     * @param answerChannel
     * @param status        修改为
     */
    private void userInfoModify(Channel callChannel, Channel answerChannel, Integer status) {
        RoomInfo roomInfo;
        if (answerChannel == null) {
            //存在 解除room
            Channel[] channels = getCallReChannel(callChannel);
            nameInfoMap.get(channelNameMap.get(channels[0])).setStatus(status);
            nameInfoMap.get(channelNameMap.get(channels[1])).setStatus(status);
            channelRooms.remove(channels[0]);
            channelRooms.remove(channels[1]);
        } else {
            //创建
            nameInfoMap.get(channelNameMap.get(callChannel)).setStatus(status);
            nameInfoMap.get(channelNameMap.get(answerChannel)).setStatus(status);
            //创建room
            roomInfo = new RoomInfo();
            roomInfo.setCallChannel(callChannel);
            roomInfo.setReceiveChannel(answerChannel);
            roomInfo.setStatus(1);
            roomInfo.setRoomName(String.valueOf(System.currentTimeMillis()));
            channelRooms.put(callChannel, roomInfo);
            channelRooms.put(answerChannel, roomInfo);
        }
    }


    /**
     * 获取呼叫应答连接
     *
     * @param channel
     * @return Channel[] 0 呼叫 1应答
     */
    private Channel[] getCallReChannel(Channel channel) {
        RoomInfo roomInfo = channelRooms.get(channel);
        if (roomInfo == null) {
            throw new CustomException("房间已解散!");
        }
        return new Channel[]{roomInfo.getCallChannel(), roomInfo.getReceiveChannel()};
    }
}
