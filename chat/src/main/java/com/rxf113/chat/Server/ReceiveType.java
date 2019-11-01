package com.rxf113.chat.Server;

public enum ReceiveType {
    //receiveType 登录成功1 登录失败2  超时3 拒绝4 接受5 对方挂断6 对方断开7  candidate8
    登录成功(1),
    登录失败(2),
    未接听超时(3),
    未接听拒绝(4),
    未接听不在线(5),
    未接听忙碌(6),
    接受(7),
    对方挂断(8),
    对方断开(9),
    ICE候选(10),
    呼叫请求(11),
    呼叫成功(12),
    接受offer(13),
    接受answer(14);
    private int value = 0;

    ReceiveType(int value){
        this.value =value;
    }

    public int getValue() {
        return this.value;
    }
}
