package com.rxf113.chat.Server;

public enum SendType {
    //登录1 呼叫2 接听3 拒绝4 挂断5 candidate6
    登录(1),
    呼叫(2),
    接受(3),
    拒绝(4),
    挂断(5),
    ICE候选(6),
    发送offer(7),
    发送answer(8);

    public static void main(String[] args) {
        System.out.println(SendType.接受.toString());
    }

    private int value = 5;

    SendType(int value){
        this.value =value;
    }

    public static SendType getSendType(int value) {
        for (SendType type : SendType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }


    public int getValue() {
        return this.value;
    }
//    @Override
//    public String toString()
//    {
//        return String.valueOf(this.value);
//    }
}
