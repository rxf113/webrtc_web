package com.rxf113.chat.business;

import com.rxf113.chat.enums.ReceiveTypeEnum;

import io.netty.channel.Channel;

/**
 * 业务处理器
 *
 * @author rxf113
 */
public interface BusinessProcessor<T> {

    ReceiveTypeEnum supportType();

    void process(Channel channel, T reqData);
}
