package com.rxf113.chat.enums;


public enum ReceiveTypeEnum {
    login(1),
    accept(2),
    //呼叫
    call(4),
    sendOffer(6),
    sendAnswer(7),
    sendICE(8);

    private int value = 0;

    ReceiveTypeEnum(int value){
        this.value =value;
    }
    public static ReceiveTypeEnum getSendType(int value) {
        for (ReceiveTypeEnum type : ReceiveTypeEnum.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new RuntimeException("code error");
    }
    public int getValue() {
        return this.value;
    }
}
