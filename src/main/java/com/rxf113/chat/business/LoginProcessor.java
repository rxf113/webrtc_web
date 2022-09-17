package com.rxf113.chat.business;

import com.alibaba.fastjson.JSON;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import com.rxf113.chat.server.DTO;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.rxf113.chat.server.CustomChannelInboundHandler.CHANNEL_NAME_MAP;
import static com.rxf113.chat.server.CustomChannelInboundHandler.NAME_INFO_MAP;

/**
 * 登录处理
 *
 * @author rxf113
 */
public class LoginProcessor implements BusinessProcessor<DTO> {

    @Override
    public ReceiveTypeEnum supportType() {
        return ReceiveTypeEnum.login;
    }

    @Override
    public void process(Channel channel, DTO reqData) {
        String userName = reqData.getMsg();
        DTO returnData = new DTO();
        login(userName, channel, returnData);
        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSON.toJSONString(returnData));
        channel.writeAndFlush(textWebSocketFrame);
    }

    /**
     * 登录
     *
     * @param channel
     * @param returnData
     */
    private void login(String userName, Channel channel, DTO returnData) {
        System.out.println(userName);
        if (NAME_INFO_MAP.get(userName) == null) {
            //存储信息
            com.rxf113.chat.server.UserInfo userInfo = new com.rxf113.chat.server.UserInfo();
            userInfo.setUserName(userName);
            userInfo.setChannel(channel);
            //1 在线 2 忙碌
            userInfo.setStatus(1);
            NAME_INFO_MAP.put(userName, userInfo);
            CHANNEL_NAME_MAP.put(channel, userName);
            returnData.setType(SendTypeEnum.loginSuccess.getValue());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            returnData.setMsg(userName + " 登陆成功 " + dateFormat.format(new Date()));
        } else {
            //用户名已存在
            returnData.setType(SendTypeEnum.loginFailed.getValue());
            returnData.setMsg("登录失败(用户名已存在!)");
        }
    }

}
