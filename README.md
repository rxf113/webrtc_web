<div style="text-align: center;font-size: 18px">
 一个基于 webrtc + netty 的视频聊天演示程序
</div>

---

**运行程序:**
- 1、启动 ```com.rxf113.chat.start``` 包下的Start类
- 2、启动成功后访问 http://localhost:9000


**本地测试:**  在浏览器打开两个页面，分别登陆，输入对方用户名呼叫、接听，能看到效果

---

**项目介绍:**
- 此项目为后端项目，默认端口 9000
- 前端项目地址:  [webrtc_web_frontend](https://github.com/rxf113/webrtc_web_frontend)

---

**webrtc 注意事项 :**

浏览器链接访问方式必须为 **https** (localhost下可以为http)，https下 使用 websocket 要用 wss协议

ice 服务器未搭建， 默认使用的谷歌提供的

---
2022-01-21: 准备更新

- 更新前端聊天界面: [webrtc_web_frontend 项目](https://github.com/rxf113/webrtc_web_frontend)

