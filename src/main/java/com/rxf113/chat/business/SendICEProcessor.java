package com.rxf113.chat.business;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import com.rxf113.chat.server.DTO;
import com.rxf113.chat.utils.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class SendICEProcessor implements BusinessProcessor<DTO> {

    @Override
    public ReceiveTypeEnum supportType() {
        return ReceiveTypeEnum.sendICE;
    }

    @Override
    public void process(Channel channel, DTO reqData) {
        reqData.setType(SendTypeEnum.receiveICE.getValue());
        Channel remoteChannel = ChannelUtil.getRemoteChannel(channel);
        remoteChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(reqData)));
    }
}
