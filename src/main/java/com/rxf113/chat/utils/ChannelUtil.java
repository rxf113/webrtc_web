package com.rxf113.chat.utils;

import com.rxf113.chat.server.CustomException;
import com.rxf113.chat.server.RoomInfo;
import io.netty.channel.Channel;

import static com.rxf113.chat.server.CustomChannelInboundHandler.channelRooms;

public class ChannelUtil {

    /**
     * 获取呼叫应答连接
     *
     * @param channel
     * @return Channel[] 0 呼叫 1应答
     */
    public static Channel[] getCallReChannel(Channel channel) {
        RoomInfo roomInfo = channelRooms.get(channel);
        if (roomInfo == null) {
            throw new CustomException("房间已解散!");
        }
        return new Channel[]{roomInfo.getCallChannel(), roomInfo.getReceiveChannel()};
    }

    /**
     * 获取对端连接
     *
     * @param channel
     * @return Channel
     */
    public static Channel getRemoteChannel(Channel channel) {
        Channel[] channels = getCallReChannel(channel);
        return channels[0] == channel ? channels[1] : channels[0];
    }

}
