var customSDK;

var localVideo;
var remoteVideo;
var localStream = null;
var socketAddress = "";

var PeerConnection = (window.PeerConnection ||
    window.webkitPeerConnection00 ||
    window.webkitRTCPeerConnection ||
    window.mozRTCPeerConnection);
//与后台服务器的WebSocket连接
var socket = new WebSocket("ws://192.168.0.189:9000/ws");

const iceServer = {
    "iceServers": [{
        "url": "stun:stun.l.google.com:19302"
    }]
};

//创建PeerConnection实例
var pc;
    pc = new PeerConnection(iceServer);
    //*****************
var dc = pc.createDataChannel("my channel",{negotiated: true, id: 0});

dc.onmessage = function (event) {
    console.log(event);
    customSDK.dataChannel.data = event.data;
    customSDK.triggerEvent(customSDK.dataChannel);
    //window.dispatchEvent(event);
    //console.log("received: " + event.data);
};

dc.onopen = function () {
    console.log("datachannel open");
};

dc.onclose = function () {
    console.log("datachannel close");
};

function sendMsg(msg){
    dc.send(JSON.stringify(msg));
}

function sendFile(file){
    dc.send(file);
}

    //******************

    resetPeerConnection(pc);
console.log("原始的pc : " + pc);
console.log(pc);
customSDK = {
    loginSuccess: new CustomEvent('login-success'),
    loginFail: new CustomEvent('login-fail'),
    overtime: new CustomEvent('overtime'),
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


    intiVideoTag : function (localVideoId,remoteVideoId) {
        localVideo = document.getElementById(localVideoId);
        remoteVideo = document.getElementById(remoteVideoId);
    },

    socket:null,
    initServerAddress : function(address){
        socketAddress = address;
        //与后台服务器的WebSocket连接
        customSDK.socket = new WebSocket(socketAddress);

        customSDK.socket.onopen = function(){
            console.log("连接建立成功...");
        };
        customSDK.socket.onclose = function() {
            console.log("连接关闭...");
        };
        customSDK.socket.onerror = function() {
            console.log("发生错误...");
        };

        customSDK.socket.onmessage = function (event) {
            var data = JSON.parse(event.data);
            const type = data.type;
            var customEvent = null;
            switch (type) {
                case 0:
                    customEvent = customSDK.exception;
                    break;
                case 13:
                    console.log("收到offer信息");
                    pc.setRemoteDescription(new RTCSessionDescription(data.info.sdp));
                    //发送answer
                    customSDK.senAnswer();
                    break;
                case 14:
                    console.log("收到answer信息");
                    pc.setRemoteDescription(new RTCSessionDescription(data.info.sdp));
                    break;
                case 10:
                    console.log("收到ice信息");
                    console.log(data.info.candidate);
                    if(data.info.candidate !== null){
                        try{
                            pc.addIceCandidate(new RTCIceCandidate(data.info.candidate));
                        }catch (e) {
                            console.log(e);
                        }
                    }
                    break;
                case 3:
                    customEvent = customSDK.overtime;
                    customSDK.closeTracks();
                    //关闭超时设置
                    customSDK.clearOverTime();
                    break;
                case 4:
                    customEvent = customSDK.refuse;
                    customSDK.closeTracks();
                    //关闭超时设置
                    customSDK.clearOverTime();
                    break;
                case 5:
                    customEvent = customSDK.notOnline;
                    customSDK.closeTracks();
                    //关闭超时设置
                    customSDK.clearOverTime();
                    break;
                case 6:
                    customEvent = customSDK.busy;
                    customSDK.closeTracks();
                    //关闭超时设置
                    customSDK.clearOverTime();
                    break;
                case 7:
                    //接受 发送offer
                    customSDK.sendOffer();
                    //关闭超时设置
                    customSDK.clearOverTime();
                    break;
                case 8:
                    customEvent = customSDK.hangUp;
                    customSDK.closeTracks();
                    //关闭超时设置
                    customSDK.clearOverTime();
                    break;
                case 9:
                    customEvent = customSDK.cutOff;
                    customSDK.closeTracks();
                    //关闭超时设置
                    customSDK.clearOverTime();
                    break;
                case 11:
                    customEvent = customSDK.beCalling;
                    break;
                case 12:
                    customEvent = customSDK.callSuccess;
                    console.log(customSDK.overTimes);
                    customSDK.overTime(customSDK.overTimes);
                    break;
                case 1:
                    customEvent = customSDK.loginSuccess;
                    break;
                case 2:
                    customEvent = customSDK.loginFail;
                    break;
                default:
                    customEvent = null;
            }
            //触发事件
            if(customEvent !== null){
                customEvent.data = data;
                customSDK.triggerEvent(customEvent);
            }
        };
    },

    login: function (userName) {
        customSDK.socket.send(JSON.stringify({
            "data": {
                "info": {
                    "userName": userName
                },
                "type": 1
            }
        }));
    },

    param: {
        on: function (name, callbackFunction) {
            window.addEventListener(name, function (event) {
                callbackFunction(event);
            });
        }
    },

    offerOptions: {
        offerToReceiveVideo: 1,
        offerToReceiveAudio: 1
    },

    constraints : {
        "audio": true,
        "video": true
        // "audio": true,
        // "video": { width: 800, height: 500 }

},

    start: function start(constraints) {
        constraints = constraints === undefined ? this.constraints : constraints;
        navigator.mediaDevices.getUserMedia(
            constraints
        ).then(function (stream) {
            localVideo.srcObject = stream;
            localStream = stream;
            //向PeerConnection中加入需要发送的流
            pc.addStream(stream);
        });
    },

    closeOpenKind: function closeOpenKind(kind) {
        if(localStream !== null){
            localStream.getTracks().forEach(track => {
                if (track.kind === kind) {
                    track.enabled = !track.enabled;
                }
                console.log(track);
            })
        }
    },

    //发送 sendOffer
    sendOffer: function sendOffer() {
        pc.createOffer(this.offerOptions).then(function (offerInfo) {
            pc.setLocalDescription(offerInfo);
            //发送到远端
            var baseData = {
                "data": {
                    "info": {
                        "sdp": offerInfo,
                    },
                    "type": 7
                }
            };
            customSDK.socket.send(JSON.stringify(baseData));
        });
    },

    overTimes:10000,

    //呼叫
    call: function (remoteId,times) {
        var data = {
            "data": {
                "info": {
                    "responder": remoteId
                },
                "type": 2
            }
        };
        //开启摄像头
        customSDK.start();
        customSDK.socket.send(JSON.stringify(data));
        if(times === undefined || times === null){
            this.overTimes = times;
        }
    },

    interval:null,

    overTime:function(times){
        this.interval = setInterval(function(){
            customSDK.hangUpAll();
            alert("超时未接听");
            clearInterval(customSDK.interval);
        }, times);
    },

    clearOverTime:function () {
        clearInterval(this.interval);
        },



    //接听
    answer: function () {
        var data = {
            "data": {
                "type": 3
            }
        };
        customSDK.socket.send(JSON.stringify(data));
    },

    senAnswer: function senAnswer() {
        pc.createAnswer().then(function (answerInfo) {
            pc.setLocalDescription(answerInfo);
            //发送到远端
            customSDK.socket.send(JSON.stringify({
                "data": {
                    "info": {
                        "sdp": answerInfo,
                    },
                    "type": 8
                }
            }));
        });
    },


    senRefuse: function () {
        var data = JSON.stringify({
            "data": {
                "type": 4,
            }
        });
        customSDK.socket.send(data);
        customSDK.closeTracks();
    },

    hangUpAll: function () {
        var data = JSON.stringify({
            "data": {
                "type": 5,
            }
        });
        customSDK.socket.send(data);
        customSDK.closeTracks();

    },

    closeTracks : function(){
        if(localStream !== null){
            localStream.getTracks().forEach(track => track.stop());
        }
        localVideo.srcObject = null;
        remoteVideo.srcObject = null;
        pc = new PeerConnection(iceServer);
        resetPeerConnection(pc);
        localStream = null;
    },


    triggerEvent: function triggerEvent(event) {
        window.dispatchEvent(event);
    },
};

function resetPeerConnection(pc){
//发送ICE候选到其他客户端
    pc.onicecandidate = function (event) {
        //console.log("iceInfo: ");
        //console.log(event);
        customSDK.socket.send(JSON.stringify({
            "data": {
                "info": {
                    "candidate": event.candidate,
                },
                "type": 6
            }
        }));
    };

    pc.onaddstream = function (event) {
    console.log("收到远端流");
    remoteVideo.srcObject = event.stream;
};
}


// //发送ICE候选到其他客户端
// pc.onicecandidate = function (event) {
//     console.log("iceInfo: ");
//     console.log(event);
//     customSDK.socket.send(JSON.stringify({
//         "data": {
//             "info": {
//                 "candidate": event.candidate,
//             },
//             "type": 6
//         }
//     }));
// };

//如果检测到媒体流连接到本地，将其绑定到一个video标签上输出
// pc.onaddstream = function (event) {
//     console.log("收到远端流");
//     remoteVideo.srcObject = event.stream;
// };