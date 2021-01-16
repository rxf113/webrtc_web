package com.rxf113.chat.runner;

import com.alibaba.fastjson.JSONObject;
//import com.rxf113.chat.MJ.DataHandle;
import com.rxf113.chat.Server.OnlineVideo;
//import com.rxf113.chat.netty.TCPServer;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rxf113
 * @Description //初始化完成加载配置文件
 * @Date  2019/3/16
 * @Param
 * @return
 **/
@Component
@PropertySource(value = "classpath:/config/system.properties")
public class CustomRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        OnlineVideo onlineVideo = new OnlineVideo();
        onlineVideo.init(9000);
    }
}