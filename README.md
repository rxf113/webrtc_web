### 一个基于 webrtc + netty 的视频聊天演示程序

运行程序：

方式一. 直接启动start包下的Start类
方式二.  进入项目目录， 命令行执行 mvn clean package 打包。完成后  java -jar target/xx.jar  启动

 启动成功后访问 http://localhost:9000/video.html

### 端口切换问题
程序默认使用9000端口，若9000被占需要切换，请按如下步骤修改

1.修改start包下的Start类的 serverPort

2.修改resources目录下static目录下的video.html 

65行的simpleSdk.openWebSocketConnection("ws://127.0.0.1:8990/ws");保持端口一致

**本地测试:**  在浏览器打开两个页面，分别登陆，输入对方用户名呼叫、接听，能看到效果

**webrtc 注意事项 :**  

浏览器链接访问方式必须为 **https** (localhost下可以为http)，https下 使用 websocket  要用 wss协议

ice 服务器未搭建，  默认使用的谷歌提供的

---
2022-01-21: 准备更新
- 更新前端聊天界面: [webrtc_web_frontend 项目](https://github.com/rxf113/webrtc_web_frontend)