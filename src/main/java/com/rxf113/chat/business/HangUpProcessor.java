package com.rxf113.chat.business;

import com.alibaba.fastjson.JSON;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import com.rxf113.chat.server.CustomException;
import com.rxf113.chat.server.DTO;
import com.rxf113.chat.server.RoomInfo;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static com.rxf113.chat.server.CustomChannelInboundHandler.*;

/**
 * 挂断处理
 *
 * @author rxf113
 */
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
        userInfoModify(channels[0], channels[1]);
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
     *  @param callChannel
     * @param answerChannel
     */
    private void userInfoModify(Channel callChannel, Channel answerChannel) {
        RoomInfo roomInfo;
        if (answerChannel == null) {
            //存在 解除room
            Channel[] channels = getCallReChannel(callChannel);
            NAME_INFO_MAP.get(CHANNEL_NAME_MAP.get(channels[0])).setStatus(1);
            NAME_INFO_MAP.get(CHANNEL_NAME_MAP.get(channels[1])).setStatus(1);
            CHANNEL_ROOMS_MAP.remove(channels[0]);
            CHANNEL_ROOMS_MAP.remove(channels[1]);
        } else {
            //创建
            NAME_INFO_MAP.get(CHANNEL_NAME_MAP.get(callChannel)).setStatus(1);
            NAME_INFO_MAP.get(CHANNEL_NAME_MAP.get(answerChannel)).setStatus(1);
            //创建room
            roomInfo = new RoomInfo();
            roomInfo.setCallChannel(callChannel);
            roomInfo.setReceiveChannel(answerChannel);
            roomInfo.setStatus(1);
            roomInfo.setRoomName(String.valueOf(System.currentTimeMillis()));
            CHANNEL_ROOMS_MAP.put(callChannel, roomInfo);
            CHANNEL_ROOMS_MAP.put(answerChannel, roomInfo);
        }
    }


    /**
     * 获取呼叫应答连接
     *
     * @param channel
     * @return Channel[] 0 呼叫 1应答
     */
    private Channel[] getCallReChannel(Channel channel) {
        RoomInfo roomInfo = CHANNEL_ROOMS_MAP.get(channel);
        if (roomInfo == null) {
            throw new CustomException("房间已解散!");
        }
        return new Channel[]{roomInfo.getCallChannel(), roomInfo.getReceiveChannel()};
    }
}
