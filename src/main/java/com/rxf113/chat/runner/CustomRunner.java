package com.rxf113.chat.runner;

import com.rxf113.chat.Server.OnlineVideo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * springBoot启动完成后运行信令服务端 2019/3/16
 *
 * @author rxf113
 **/
@Component
public class CustomRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        OnlineVideo onlineVideo = new OnlineVideo();
        onlineVideo.init(9000);
    }
}