package com.rxf113.chat.Server;

public enum SocketType {
    //登录1 呼叫2 接听3 拒绝4 挂断5 candidate6
    登录(1),
    呼叫(2),
    接听(3),
    拒绝(4),
    挂断(5),
    ICE候选(6);

    private int value = 5;

    SocketType(int value){
        this.value =value;
    }

    public static SocketType getSocketType(int value) {
        for (SocketType type : SocketType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }


    public int getValue() {
        return this.value;
    }
    @Override
    public String toString()
    {
        return String.valueOf(this.value);
    }
}
