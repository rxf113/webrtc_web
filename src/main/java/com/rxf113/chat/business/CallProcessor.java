package com.rxf113.chat.business;

import com.alibaba.fastjson.JSON;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import com.rxf113.chat.server.DTO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import static com.rxf113.chat.server.CustomChannelInboundHandler.*;

/**
 * 呼叫处理
 *
 * @author rxf113
 */
public class CallProcessor implements BusinessProcessor<DTO> {

    @Override
    public ReceiveTypeEnum supportType() {
        return ReceiveTypeEnum.call;
    }

    @Override
    public void process(Channel channel, DTO reqData) {
        String remoteUserName = reqData.getMsg();
        DTO returnData = new DTO();
        call(remoteUserName, channel, returnData);
        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(returnData)));
    }

    /**
     * 呼叫
     *
     * @param remoteUserName 对方用户名
     * @param channel
     * @param returnData
     */
    private void call(String remoteUserName, Channel channel, com.rxf113.chat.server.DTO returnData) {
        com.rxf113.chat.server.UserInfo answerUserInfo = NAME_INFO_MAP.get(remoteUserName);
        if (answerUserInfo != null) {
            if (answerUserInfo.getStatus() == 1) {
                com.rxf113.chat.server.DTO data = new com.rxf113.chat.server.DTO();
                Channel answerUserInfoChannel = answerUserInfo.getChannel();
                data.setType(SendTypeEnum.called.getValue());
                data.setMsg(CHANNEL_NAME_MAP.get(channel));
                //通知被叫
                answerUserInfoChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(data)));
                //更改状态 2忙碌
                userInfoModify(channel, answerUserInfoChannel, 2);

                returnData.setType(SendTypeEnum.callSuccess.getValue());
                returnData.setMsg("呼叫成功!");
            } else {
                //忙碌
                returnData.setType(SendTypeEnum.busy.getValue());
                returnData.setMsg("用户忙,无法接听!");
            }
        } else {
            //离线
            returnData.setType(SendTypeEnum.notOnline.getValue());
            returnData.setMsg("用户不在线!");
        }
    }


    /**
     * 用户状态修改
     *
     * @param callChannel
     * @param answerChannel
     * @param status        修改为
     */
    private void userInfoModify(Channel callChannel, Channel answerChannel, Integer status) {
        com.rxf113.chat.server.RoomInfo roomInfo;
        if (answerChannel == null) {
            //存在 解除room
            Channel[] channels = getCallReChannel(callChannel);
            NAME_INFO_MAP.get(CHANNEL_NAME_MAP.get(channels[0])).setStatus(status);
            NAME_INFO_MAP.get(CHANNEL_NAME_MAP.get(channels[1])).setStatus(status);
            CHANNEL_ROOMS_MAP.remove(channels[0]);
            CHANNEL_ROOMS_MAP.remove(channels[1]);
        } else {
            //创建
            NAME_INFO_MAP.get(CHANNEL_NAME_MAP.get(callChannel)).setStatus(status);
            NAME_INFO_MAP.get(CHANNEL_NAME_MAP.get(answerChannel)).setStatus(status);
            //创建room
            roomInfo = new com.rxf113.chat.server.RoomInfo();
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
        com.rxf113.chat.server.RoomInfo roomInfo = CHANNEL_ROOMS_MAP.get(channel);
        if (roomInfo == null) {
            throw new com.rxf113.chat.server.CustomException("房间已解散!");
        }
        return new Channel[]{roomInfo.getCallChannel(), roomInfo.getReceiveChannel()};
    }

}
