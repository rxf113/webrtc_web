### 一个基于 webrtc + netty 的视频聊天演示程序

运行程序：

1. 直接启动start包下的Start类
2.  进入项目目录， 命令行执行 mvn clean package 打包。完成后  java -jar target/xx.jar  启动

 启动成功后访问 http://localhost:9000/video.html

**本地测试:**  在浏览器打开两个页面，分别登陆，输入对方用户名呼叫、接听，能看到效果

**webrtc 注意事项 :**  

浏览器链接访问方式必须为 **https** (localhost下可以为http)，https下 使用 websocket  要用 wss协议

ice 服务器未搭建，  默认使用的谷歌提供的

