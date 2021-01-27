### 一个基于 webrtc + netty 的视频聊天demo

运行springboot程序 ，访问 http://localhost:9000/video.html

**本地测试:**  在浏览器打开两个页面，分别登陆，输入对方用户名呼叫、接听，能看到效果

**webrtc 注意事项 :**  

浏览器链接访问方式必须为 **https** (localhost下可以为http)，https下 使用 websocket  要用 wss协议

ice 服务器未搭建，  默认使用的谷歌提供的

