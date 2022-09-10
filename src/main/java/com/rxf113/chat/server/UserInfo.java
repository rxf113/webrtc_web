package com.rxf113.chat.server;

import io.netty.channel.Channel;

/**
 * 用户信息
 *
 * @author rxf113
 */
public class UserInfo {
    private String userName;
    private Channel channel;
    /**
     * //1 空闲 2忙碌
     */
    private Integer status;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
