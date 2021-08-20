package com.rxf113.chat.enums;

/**
 * 接收类型枚举
 *
 * @author rxf113
 **/
public enum ReceiveTypeEnum {
    login(1),
    accept(2),
    //呼叫
    call(4),
    sendOffer(6),
    sendAnswer(7),
    sendICE(8),
    hangUp(9),
    disconnection(10),
    heartBeat(999);

    private int value = 0;

    ReceiveTypeEnum(int value) {
        this.value = value;
    }

    public static ReceiveTypeEnum getReceiveTypeEnum(int value) {
        for (ReceiveTypeEnum type : ReceiveTypeEnum.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new RuntimeException("receiveTypeEnum not exists");
    }

    public int getValue() {
        return this.value;
    }
}
