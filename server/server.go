//// Copyright 2015 The Gorilla WebSocket Authors. All rights reserved.
//// Use of this source code is governed by a BSD-style
//// license that can be found in the LICENSE file.
//
//// +build ignore
//
package server

import (
	"encoding/json"
	"fmt"
	"github.com/gorilla/websocket"
	"log"
	"net/http"
	"time"
)

var constMt int = -1

var upgrader = websocket.Upgrader{
	CheckOrigin: func(r *http.Request) bool {
		return true
	},
} // use default options
//
func WebSocketHandle(w http.ResponseWriter, r *http.Request) {
	c, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Print("upgrade:", err)
		return
	}
	defer func() {
		err := c.Close()
		if err != nil {
			fmt.Println(err)
			return
		}
	}()
	//defer c.Close()
	for {
		mt, message, err := c.ReadMessage()
		if constMt == -1 {
			constMt = mt
		}
		if err != nil {
			log.Println("read:", err)
			break
		}
		//接收消息
		var mapResult map[string]*Dto

		var dtoMap *Dto
		uuu := json.Unmarshal(message, &mapResult)
		dtoMap = mapResult["data"]

		if uuu != nil {
			break
		}
		reType := dtoMap.ReType
		msg := dtoMap.Msg
		returnData := new(Dto)

		switch reType {
		//登录
		case ReceiveTypeEnum.login:
			login(msg, c, returnData)
			break
		//send offer/answer
		case ReceiveTypeEnum.sendOffer:
		case ReceiveTypeEnum.sendAnswer:
			offerAnswer(ReceiveTypeEnum.sendOffer, c, msg)
			break
		case ReceiveTypeEnum.call:
			call(msg, c, returnData)
			break
		case ReceiveTypeEnum.accept:
			accept(dtoMap,c)
			break
		case ReceiveTypeEnum.hangUp:
			hangUp(c)
			break
		case ReceiveTypeEnum.sendICE:
			dtoMap.ReType = SendTypeEnum.receiveICE
			remoteChannel := getRemoteChannel(c)
			writeMsg(remoteChannel,dtoMap)
			break

		}
		writeMsg(c,returnData)
	}
}

//用户map
var nameInfoMap = make(map[string]*UserInfo)

//channelId - name
var channelNameMap = make(map[*websocket.Conn]string)
//channel - room
var channelRooms = make(map[*websocket.Conn]*RoomInfo)

/**
登录
*/
func login(userName string, c *websocket.Conn, returnData *Dto) {
	//登录
	log.Println("登录: userName: ", userName)
	_, ok := nameInfoMap[userName]
	if ok {
		//存在
		returnData.ReType = SendTypeEnum.loginFailed
		returnData.Msg = "用户名已存在"
	} else {
		//存储用户信息
		userInfo := new(UserInfo)
		userInfo.userName = userName
		userInfo.channel = c
		userInfo.status = 1
		nameInfoMap[userName] = userInfo
		channelNameMap[c] = userName

		returnData.ReType = SendTypeEnum.loginSuccess
		returnData.Msg = "登录成功!"
	}
}

//ReceiveTypeEnum.sendOffer,c,msg
//If(2>3, "大于", false)
func offerAnswer(reType int8, c *websocket.Conn, msg string) {
	var currentRoom = channelRooms[c]
	var remoteChannel *websocket.Conn
	if reType == ReceiveTypeEnum.sendOffer {
		remoteChannel = currentRoom.receiveChannel
	} else {
		remoteChannel = currentRoom.callChannel
	}
	offerData := new(Dto)
	if reType == ReceiveTypeEnum.sendOffer {
		offerData.ReType = SendTypeEnum.receiveOffer
	} else {
		offerData.ReType = SendTypeEnum.receiveAnswer
	}
	offerData.Msg = msg

	writeMsg(remoteChannel, offerData)
	//offerData.setType(receiveTypeEnum == ReceiveTypeEnum.sendOffer ? SendTypeEnum.receiveOffer.getValue() : SendTypeEnum.receiveAnswer.getValue());
	//offerData.setMsg(msg);
	//remoteChannel.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(offerData)));
}

func call(remoteUserName string, channel *websocket.Conn, returnData *Dto) {
	var answerUserInfo = nameInfoMap[remoteUserName]
	if answerUserInfo != nil {
		if answerUserInfo.status == 1 {
			data := new(Dto)
			answerUserInfoChannel := answerUserInfo.channel
			data.ReType = SendTypeEnum.called
			data.Msg = channelNameMap[channel]
			//通知被叫
			writeMsg(answerUserInfoChannel, data)
			//更改状态 为2
			userInfoModify(channel,answerUserInfoChannel,2)

			returnData.ReType = SendTypeEnum.callSuccess
			returnData.Msg = "呼叫成功!"
		}else {
			//忙碌
			returnData.ReType = SendTypeEnum.busy
			returnData.Msg = "用户忙!"
		}
	}else {
		//离线
		returnData.ReType = SendTypeEnum.notOnline
		returnData.Msg = "用户不在线!"
	}
}

//响应消息
func writeMsg(c *websocket.Conn, dto *Dto) {
	bytes, err := json.Marshal(dto)
	if err != nil {
		log.Println(err)
	}
	err = c.WriteMessage(constMt, bytes)
}

/**
 * 用户状态修改
 *
 * @param callChannel
 * @param answerChannel
 * @param status        修改为
 */
func userInfoModify(callChannel *websocket.Conn, answerChannel *websocket.Conn, status int)  {
	var roomInfo *RoomInfo
	if(answerChannel == nil){
		//解除room
		var channels  [2]*websocket.Conn = getCallReChannel(callChannel)
		nameInfoMap[channelNameMap[channels[0]]].status = int8(status)
		nameInfoMap[channelNameMap[channels[1]]].status = int8(status)
		delete(channelRooms,channels[0])
		delete(channelRooms,channels[1])
	}else {
		//创建
		nameInfoMap[channelNameMap[callChannel]].status = int8(status)
		nameInfoMap[channelNameMap[answerChannel]].status = int8(status)
		//创建room
		roomInfo = new(RoomInfo)
		roomInfo.callChannel = callChannel
		roomInfo.receiveChannel = answerChannel
		roomInfo.status = 1
		roomInfo.roomName = time.Now().Format("2006-01-02 15:04:05")
		channelRooms[callChannel] = roomInfo
		channelRooms[answerChannel] = roomInfo
	}
}

/**
 * 获取对端连接
 *
 * @param channel
 * @return Channel
 */
func getRemoteChannel(channel *websocket.Conn) *websocket.Conn {
	channels := getCallReChannel(channel)
	if channels[0] == channel {
		return channels[1]
	}else {
		return channels[0]
	}
}

/**
 * 获取呼叫应答连接
 *
 * @param channel
 * @return Channel[] 0 呼叫 1应答
 */
func getCallReChannel(channel *websocket.Conn)  [2]*websocket.Conn{
	var roomInfo = channelRooms[channel]
	if roomInfo == nil{
		log.Println("房间已解散")
	}
	return [2]*websocket.Conn{roomInfo.callChannel,roomInfo.receiveChannel}
}
//接受
func accept(customData *Dto, channel *websocket.Conn)  {
	customData.ReType = SendTypeEnum.accepted
	writeMsg(getRemoteChannel(channel),customData)
}
//挂断
func hangUp(channel *websocket.Conn)  {
	sendMsgData := new(Dto)
	sendMsgData.ReType = SendTypeEnum.hangUp
	sendMsgData.Msg = "对方已挂断!"
	writeMsg(getRemoteChannel(channel),sendMsgData)
	var channels =  getCallReChannel(channel)
	userInfoModify(channels[0],channels[1],1)
}