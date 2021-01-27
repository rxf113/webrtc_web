package com.rxf113.chat.start;

import com.rxf113.chat.Server.OnlineVideo;

/**
 * @author rxf113
 **/

public class Start {

    public static void main(String[] args) {
        int serverPort = 9000;
        if(args.length == 1){
            serverPort = Integer.parseInt(args[0]);
        }
        OnlineVideo onlineVideo = new OnlineVideo();
        onlineVideo.init(serverPort);
    }
}