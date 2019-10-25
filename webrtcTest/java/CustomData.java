package com.netty;

import java.util.Map;

/**
 * @author rxf113
 */
public class CustomData {

    private Map<String,Object> info;
    private Integer type;
    private String msg;

    public Map<String, Object> getInfo() {
        return info;
    }

    public void setInfo(Map<String, Object> info) {
        this.info = info;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
