package com.rxf113.chat.Server;

import io.netty.channel.Channel;

public class RoomInfo {
    private String roomName;
    private Channel callChannel;
    private Channel receiveChannel;
    //1等待连接 2连通 3非正常断开
    private Integer status;

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Channel getCallChannel() {
        return callChannel;
    }

    public void setCallChannel(Channel callChannel) {
        this.callChannel = callChannel;
    }

    public Channel getReceiveChannel() {
        return receiveChannel;
    }

    public void setReceiveChannel(Channel receiveChannel) {
        this.receiveChannel = receiveChannel;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
