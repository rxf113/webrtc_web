package com.rxf113.chat.enums;

public enum SendTypeEnum {
    loginSuccess(1),
    loginFailed(2),
    called(3),
    callSuccess(4),
    busy(5),
    notOnline(6),
    accepted(7),
    receiveOffer(8),
    receiveAnswer(9),
    receiveICE(10),
    hangUp(11);
    //呼叫
    //call(4);

    private int value = 0;

    SendTypeEnum(int value){
        this.value =value;
    }

    public int getValue() {
        return this.value;
    }
}
