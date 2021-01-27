package com.rxf113.chat.Server;

import com.alibaba.fastjson.JSONObject;
import com.rxf113.chat.enums.ReceiveTypeEnum;
import com.rxf113.chat.enums.SendTypeEnum;
import com.rxf113.chat.utils.StaticFileUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 入站处理
 */
class CustomChannelInboundHandler extends SimpleChannelInboundHandler<Object> {

    //name - userInfo
    private static Map<String, UserInfo> nameInfoMap = new HashMap<>();
    //channelId - name
    private static Map<Channel, String> channelNameMap = new HashMap<>();

    private static Map<Channel, RoomInfo> channelRooms = new HashMap<>();

    /**
     * 登录
     *
     * @param channel
     * @param returnData
     */
    private void login(String userName, Channel channel, DTO returnData) {
        System.out.println(userName);
        if (nameInfoMap.get(userName) == null) {
            //存储信息
            UserInfo userInfo = new UserInfo();
            userInfo.setUserName(userName);
            userInfo.setChannel(channel);
            //userInfo.setChannelId(channel.id().toString());
            //1 在线 2 忙碌
            userInfo.setStatus(1);
            nameInfoMap.put(userName, userInfo);
            channelNameMap.put(channel, userName);
            returnData.setType(SendTypeEnum.loginSuccess.getValue());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            returnData.setMsg(userName + " 登陆成功 " + dateFormat.format(new Date()));
        } else {
            //用户名已存在
            returnData.setType(SendTypeEnum.loginFailed.getValue());
            returnData.setMsg("登录失败(用户名已存在!)");
        }
    }


    /**
     * 呼叫
     *
     * @param remoteUserName 对方用户名
     * @param channel
     * @param returnData
     */
    private void call(String remoteUserName, Channel channel, DTO returnData) {
        UserInfo answerUserInfo = nameInfoMap.get(remoteUserName);
        if (answerUserInfo != null) {
            if (answerUserInfo.getStatus() == 1) {
                DTO data = new DTO();
                Channel answerUserInfoChannel = answerUserInfo.getChannel();
                data.setType(SendTypeEnum.called.getValue());
                data.setMsg(channelNameMap.get(channel));
                //通知被叫
                answerUserInfoChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(data)));
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
     * 接受
     *
     * @param customData
     * @param channel
     */
    private void accept(DTO customData, Channel channel) {
        customData.setType(SendTypeEnum.accepted.getValue());
        getRemoteChannel(channel).writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(customData)));
    }

//    /**
//     * 拒绝
//     *
//     * @param channel
//     */
//    private void refuse(Channel channel) {
//        DTO returnData = new DTO();
//        returnData.setType(ReceiveTypeEnum.disconnection.getValue());
//        returnData.setMsg("对方已拒绝!");
//        getRemoteChannel(channel).writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(returnData)));
//        userInfoModify(channel, getRemoteChannel(channel), 1);
//    }

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

    /**
     * 挂断
     *
     * @param channel
     */
    private void hangUp(Channel channel) {
        DTO sendMsgData = new DTO();
        sendMsgData.setType(SendTypeEnum.hangUp.getValue());
        sendMsgData.setMsg("对方已挂断!");
        getRemoteChannel(channel).writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(sendMsgData)));
        Channel[] channels = getCallReChannel(channel);
        userInfoModify(channels[0], channels[1], 1);
    }

    @Override
    @SuppressWarnings("rawtypes")
    protected void channelRead0(ChannelHandlerContext handlerContext, Object obj) throws Exception {
        if (obj instanceof FullHttpRequest) {
            //http 只处理静态文件
            HttpResponseStatus httpResponseStatus = HttpResponseStatus.OK;
            FullHttpRequest httpRequest = (FullHttpRequest) obj;
            String uri = httpRequest.uri();
            byte[] fileBytes = new byte[0];
            if (uri != null && !"/".equals(uri.trim())) {
                try {
                    int i = uri.lastIndexOf("/");
                    fileBytes = StaticFileUtil.getFileBytes(uri.substring(i + 1));
                } catch (Exception e) {
                    fileBytes = new byte[0];
                    httpResponseStatus = HttpResponseStatus.NOT_FOUND;
                }
            }
            // 1.设置响应
            FullHttpResponse resp = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus,
                    Unpooled.copiedBuffer(fileBytes));

            resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");

            //响应
            handlerContext.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
        } else //websocket
            if (obj instanceof TextWebSocketFrame) {
                TextWebSocketFrame o = (TextWebSocketFrame) obj;
                Map map = JSONObject.parseObject(o.text(), Map.class);
                Channel channel = handlerContext.channel();
                DTO customData = JSONObject.parseObject(map.get("data").toString(), DTO.class);
                String msg = customData.getMsg();
                Integer type = customData.getType();
                ReceiveTypeEnum receiveTypeEnum = ReceiveTypeEnum.getReceiveTypeEnum(type);
                //应答数据
                DTO returnData = new DTO();
                switch (receiveTypeEnum) {
                    case login:
                        login(msg, channel, returnData);
                        break;
                    case sendOffer:
                    case sendAnswer:
                        offerAnswer(receiveTypeEnum, channel, msg);
                        break;
                    case call:
                        call(msg, channel, returnData);
                        break;
                    case accept:
                        accept(customData, channel);
                        break;
                    // case 拒绝:
                    // refuse(channel);
                    // break;
                    case hangUp:
                        hangUp(channel);
                        break;
                    case sendICE:
                        customData.setType(SendTypeEnum.receiveICE.getValue());
                        Channel remoteChannel = getRemoteChannel(channel);
                        remoteChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(customData)));
                        break;
                    default:
                }
                channel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(returnData)));
            }

    }

    /**
     * offer answer 消息传递
     *
     * @param receiveTypeEnum
     * @param channel
     * @param msg
     */
    private void offerAnswer(ReceiveTypeEnum receiveTypeEnum, Channel channel, String msg) {
        RoomInfo currentRoom = channelRooms.get(channel);
        Channel remoteChannel = receiveTypeEnum == ReceiveTypeEnum.sendOffer ? currentRoom.getReceiveChannel() : currentRoom.getCallChannel();
        DTO offerData = new DTO();
        offerData.setType(receiveTypeEnum == ReceiveTypeEnum.sendOffer ? SendTypeEnum.receiveOffer.getValue() : SendTypeEnum.receiveAnswer.getValue());
        offerData.setMsg(msg);
        remoteChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(offerData)));
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
     * 获取对端连接
     *
     * @param channel
     * @return Channel
     */
    private Channel getRemoteChannel(Channel channel) {
        Channel[] channels = getCallReChannel(channel);
        return channels[0] == channel ? channels[1] : channels[0];
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception {
        if (throwable instanceof CustomException) {
            DTO exceptionData = new DTO();
            exceptionData.setMsg(throwable.getMessage());
            exceptionData.setType(0);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(exceptionData)));
        } else {
            super.exceptionCaught(ctx, throwable);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        String name = channelNameMap.get(channel);
        channelNameMap.remove(channel);
        UserInfo userInfo = nameInfoMap.get(name);
        nameInfoMap.remove(name);
        //移除room信息
        if (userInfo != null) {
            if (userInfo.getStatus() == 2) {
                Channel[] channels = getCallReChannel(channel);
                Channel remoteChannel = channels[0] == channel ? channels[1] : channels[0];
                //通知断开
                DTO cutOffData = new DTO();
                cutOffData.setType(ReceiveTypeEnum.disconnection.getValue());
                cutOffData.setMsg("对方断开");
                remoteChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(cutOffData)));
                channelRooms.remove(channels[0]);
                channelRooms.remove(channels[1]);
            }
        }
    }
}