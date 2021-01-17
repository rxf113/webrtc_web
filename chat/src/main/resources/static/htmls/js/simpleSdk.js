//创建PeerConnection实例
const PeerConnection = (window.PeerConnection ||
    window.webkitPeerConnection00 ||
    window.webkitRTCPeerConnection ||
    window.mozRTCPeerConnection);
if (!PeerConnection) {
    throw "浏览器不支持webrtc功能"
}
//let testRTCPeerConnection = window.RTCPeerConnection ||
//         window.webkitRTCPeerConnection ||
//         window.mozRTCPeerConnection ||
//         window.RTCIceGatherer;

//let serverConfig = {
//             "iceServers": [{
//                 urls: [
//                     "stun:stun.l.google.com:19302",
//                     "stun:stun1.l.google.com:19302",
//                     "stun:stun2.l.google.com:19302",
//                     "stun:stun.l.google.com:19302?transport=udp"
//                 ]
//             }]
//         };
//new RTCPeerConnection(serverConfig);
const iceServer = {
    "iceServers": [{
        "urls": "stun:stun.l.google.com:19302"
    }]
};
//创建PeerConnection实例
let pc = new RTCPeerConnection(iceServer);
let sender = null;
let localStream = null;
setPeerConnection(pc);

//自定义的事件 收到webSocket消息后根据type类型触发
simpleCustomEvents = {
    loginSuccess: new CustomEvent('login-success'),
    loginFail: new CustomEvent('login-fail'),
    loginTimeout: new CustomEvent('login-timeout'),
    called: new CustomEvent('called'),
    accepted: new CustomEvent('accepted'),
    receiveOffer: new CustomEvent('receive-offer'),
    refuse: new CustomEvent('refuse'),
    notOnline: new CustomEvent('not-online'),
    busy: new CustomEvent('busy'),
    accept: new CustomEvent('accept'),
    hangUp: new CustomEvent('hang-up'),
    cutOff: new CustomEvent('cut-off'),
    ice: new CustomEvent('ice'),
    beCalling: new CustomEvent('be-calling'),
    callSuccess: new CustomEvent('call-success'),
    exception: new CustomEvent('exception'),
    dataChannel: new CustomEvent('data-channel'),
};

/**
 * 数据传输对象
 */
class DTO {
    constructor(msg, type) {
        this.data = {
            "msg": msg,
            "type": type
        }
    }
}

/**
 * 发送方的业务类型枚举
 */
sendTypeEnum = {
    //登录
    login: 1,
    //接受呼叫
    accept: 2,
    //发起呼叫
    call: 4,
    //被呼叫
    called: 5,
    //发送 offer
    sendOffer: 6,
    //发送answer
    sendAnswer: 7,
    //发送ice
    sendICE: 8,
    //挂断
    hangUp: 9
};

/**
 * 接收方的业务类型枚举
 */
receiveTypeEnum = {
    //登录成功
    loginSuccess: 1,
    //登陆失败
    loginFail: 2,
    //被呼叫
    called: 3,
    //呼叫成功
    callSuccess: 4,
    //忙碌
    busy: 5,
    //不在线
    notOnline: 6,
    //已接受
    accepted: 7,
    //接收offer
    receiveOffer: 8,
    //接收answer
    receiveAnswer: 9,
    //接收ice
    receiveICE: 10,
    //挂断
    hangUp: 11
};

/**
 * sdk
 */
simpleSdk = {

    webSocket: null,
    //本地video标签
    localVideo: null,
    //远端video标签
    remoteVideo: null,

    /**
     * 建立websocket连接
     *
     * @param address 信令服务器地址
     * @returns {string}
     */
    openWebSocketConnection: function (address) {
        //正则校验 ws://
        const reg = new RegExp("^(ws|wss)://.*");
        if (!reg.test("ws://127.0.0.1")) {
            throw "只支持 ws & wss 协议"

        }
        simpleSdk.webSocket = new WebSocket(address);

        simpleSdk.webSocket.onopen = function () {
            console.log("webSocket连接建立成功... " + "服务端address: " + address);
        };
        simpleSdk.webSocket.onclose = function () {
            console.log("webSocket连接关闭...");
        };
        simpleSdk.webSocket.onerror = function (error) {
            console.log("webSocket发生错误..." + error.data);
        };
        //监听webSocket消息
        simpleSdk.webSocket.onmessage = function (event) {
            let data = JSON.parse(event.data);
            let type = data.type;
            //根据type触发对应的事件
            let customEvent = null;
            switch (type) {
                case receiveTypeEnum.loginSuccess://登录成功
                    customEvent = simpleCustomEvents.loginSuccess;
                    break;
                case receiveTypeEnum.loginFail://登录失败
                    customEvent = simpleCustomEvents.loginFail;
                    break;
                // case typeEnum.loginTimeout://登录超时
                //     customEvent = simpleCustomEvents.loginTimeout;
                //     break;
                case receiveTypeEnum.busy://忙碌
                    customEvent = simpleCustomEvents.busy;
                    break;
                case receiveTypeEnum.callSuccess://呼叫成功
                    customEvent = simpleCustomEvents.callSuccess;
                    //开始等待接受
                    simpleSdk.openWaitAccept();
                    break;
                case receiveTypeEnum.called://被呼叫
                    customEvent = simpleCustomEvents.called;
                    console.log(type + "   " + "被呼叫");
                    break;
                case receiveTypeEnum.notOnline://不在线
                    customEvent = simpleCustomEvents.notOnline;
                    break;
                case  receiveTypeEnum.accepted://已接受
                    //关闭等待接受
                    simpleSdk.clearWaitAccept();
                    //发送offer
                    simpleSdk.sendOffer();
                    customEvent = simpleCustomEvents.accepted;
                    break;
                case receiveTypeEnum.receiveOffer://接收offer
                    let promise = pc.setRemoteDescription(new RTCSessionDescription(JSON.parse(data.msg)));
                    promise.catch(() => {
                        throw "setRemoteDescription receiveOffer error"
                    });
                    //发送answer
                    simpleSdk.senAnswer();
                    break;
                case receiveTypeEnum.receiveAnswer://接收answer
                    pc.setRemoteDescription(new RTCSessionDescription(JSON.parse(data.msg)))
                        .catch(() => {
                            throw "setRemoteDescription receiveAnswer error"
                        });
                    break;
                case receiveTypeEnum.receiveICE://接收ice
                    if(data.msg !== null){
                        pc.addIceCandidate(new RTCIceCandidate(JSON.parse(data.msg)))
                            .catch(() => {
                                throw "receiveICE error"
                            });
                    }

                    break;
                case receiveTypeEnum.hangUp://对方挂断
                    customEvent = simpleCustomEvents.hangUp;
                    simpleSdk.hangUp();
                    break;
                default:
                    customEvent = null;
            }
            //触发事件
            if (customEvent !== null) {
                customEvent.data = data;
                simpleSdk.triggerEvent(customEvent);
            }
        }
    },

    /**
     * 挂断
     */
    hangUp: function () {
        let dto = new DTO("", sendTypeEnum.hangUp);
        simpleSdk.webSocket.send(JSON.stringify(dto));
        //pc.removeTrack(sender);
        simpleSdk.closeTracks();
    },

    closeTracks: function () {
        if (localStream !== null) {
            localStream.getTracks().forEach(track => track.stop());
        }
        localVideo.srcObject = null;
        remoteVideo.srcObject = null;
        pc = new PeerConnection(iceServer);
        setPeerConnection(pc);
        localStream = null;
    },

    /**
     * 触发事件
     *
     * @param event
     */
    triggerEvent: function triggerEvent(event) {
        window.dispatchEvent(event);
    },


    /**
     * 监听事件
     *
     * @param eventName
     * @param callbackFunction
     */
    on: function (eventName, callbackFunction) {
        window.addEventListener(eventName, function (event) {
            callbackFunction(event);
        });
    },

    /**
     * 登录
     *
     * @param userName 用户名
     */
    login: function (userName) {
        let dto = new DTO(userName, sendTypeEnum.login);
        simpleSdk.webSocket.send(JSON.stringify(dto));
    },

    //约束条件
    constraints: {
        "audio": true,
        "video": true
    },

    //摄像头开启状态 0关闭 1开启
    cameraStatus: 0,

    /**
     * 打开本地摄像头
     *
     * @param constraints 视频信息约束(在上面)
     */
    openLocalCamera: function openLocalCamera(constraints) {
        constraints = constraints === undefined ? simpleSdk.constraints : constraints;
        let promise = navigator.mediaDevices.getUserMedia(constraints);
        promise.then((stream) => {
            localVideo.srcObject = stream;
            //修改摄像头状态
            simpleSdk.cameraStatus = 1;
            localStream = stream;
            //向PeerConnection中加入视频流
            stream.getTracks().forEach(function (track) {
                pc.addTrack(track, stream);
            });
        }).catch((err) => {
            console.log(err);
            throw `openLocalCamera navigator.getUserMedia error  case:${err}`;
        });
    },


    //呼叫成功后 等待对方接听的时间 默认10s
    timeout: 10000,
    //等待接受超时interval
    interval: null,

    /**
     * 呼叫用户
     *
     * @param remoteUser 对方用户
     * @param timeout 超时时间
     */
    call: function (remoteUser, timeout) {
        let dto = new DTO(remoteUser, sendTypeEnum.call);
        //自动开启摄像头
        if (simpleSdk.cameraStatus === 0) {
            simpleSdk.openLocalCamera();
        }
        simpleSdk.webSocket.send(JSON.stringify(dto));
        if (timeout === undefined || timeout === null) {
            simpleSdk.timeout = timeout;
        }
    },

    /**
     * 等待接受
     */
    openWaitAccept: function () {
        simpleSdk.interval = setInterval(function () {
            //simpleSdk.webSocket.hangUpAll();
            alert("超时未接听");
            simpleSdk.clearWaitAccept();
        }, simpleSdk.timeout);
    },

    /**
     * 清除等待接受
     */
    clearWaitAccept: function () {
        clearInterval(simpleSdk.interval);
    },

    /**
     * 接受通话请求
     */
    accept: function () {
        let dto = new DTO("", sendTypeEnum.accept);
        simpleSdk.webSocket.send(JSON.stringify(dto));
        simpleSdk.openLocalCamera();
    },

    /**
     * 发送offer
     */
    sendOffer: function sendOffer() {
        //simpleSdk.offerOptions
        pc.createOffer().then(function (offerInfo) {
            let promise = pc.setLocalDescription(offerInfo);
            promise.then(() => {
                //发送到远端
                let dto = new DTO(offerInfo, sendTypeEnum.sendOffer);
                simpleSdk.webSocket.send(JSON.stringify(dto));
            }).catch((err) => {
                throw `sendOffer failed cause: ${err}`
            })
        });
    },

    /**
     * 发送answer
     */
    senAnswer: function senAnswer() {
        pc.createAnswer().then(function (answerInfo) {
            pc.setLocalDescription(answerInfo).then(() => {
                //发送到远端
                let dto = new DTO(answerInfo, sendTypeEnum.sendAnswer);
                simpleSdk.webSocket.send(JSON.stringify(dto));
            }).catch((error) => {
                throw `senAnswer ERROR cause ${error}`
            });
        });
    },


    /**
     * 设置视频画面的标签
     *
     * @param localVideoId 本地视频标签id
     * @param remoteVideoId 对方视频标签id
     */
    setVideoTag: function (localVideoId, remoteVideoId) {
        let localVideoIdEle = document.getElementById(localVideoId);
        let remoteVideoEle = document.getElementById(remoteVideoId);
        if (localVideoIdEle.tagName !== 'VIDEO' || remoteVideoEle.tagName !== 'VIDEO') {
            throw "视频画面的标签只能是video"
        }
        simpleSdk.localVideo = localVideoIdEle;
        simpleSdk.remoteVideo = remoteVideoEle;
    },
};

function setPeerConnection(pc) {
    //监听ice信息
    pc.onicecandidate = function (event) {
        console.log("onicecandidate " + event);
        if (!event || !event.candidate) return;
        let dto = new DTO(event.candidate, sendTypeEnum.sendICE);
        simpleSdk.webSocket.send(dto)
    };
    //监听远端流
    pc.ontrack = function (event) {
        console.log("收到远端流 " + event);
        simpleSdk.remoteVideo.srcObject = event.streams[0];
    };
}

