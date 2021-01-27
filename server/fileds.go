package server

import "github.com/gorilla/websocket"

type ReceiveType struct {
	login         int8
	accept        int8
	call          int8
	sendOffer     int8
	sendAnswer    int8
	sendICE       int8
	hangUp        int8
	disconnection int8
}

// 接收类型枚举
var ReceiveTypeEnum = ReceiveType{
	login:         1,
	accept:        2,
	call:          4,
	sendOffer:     6,
	sendAnswer:    7,
	sendICE:       8,
	hangUp:        9,
	disconnection: 10,
}

type SendType struct {
	loginSuccess  int8
	loginFailed   int8
	called        int8
	callSuccess   int8
	busy          int8
	notOnline     int8
	accepted      int8
	receiveOffer  int8
	receiveAnswer int8
	receiveICE    int8
	hangUp        int8
}

//发送类型枚举
var SendTypeEnum = SendType{
	loginSuccess:  1,
	loginFailed:   2,
	called:        3,
	callSuccess:   4,
	busy:          5,
	notOnline:     6,
	accepted:      7,
	receiveOffer:  8,
	receiveAnswer: 9,
	receiveICE:    10,
	hangUp:        11,
}

type UserInfo struct {
	userName string
	channel *websocket.Conn
	/**
	 * //1 空闲 2忙碌
	 */
	status int8
}

type RoomInfo struct {
	roomName string
	callChannel *websocket.Conn
	receiveChannel *websocket.Conn
	//1等待连接 2连通 3非正常断开
	status int8
}