package web

import (
	"flag"
	"log"
	"net/http"
	"webrtc/server"
)

var addr = flag.String("addr", "localhost:9000", "http service address")


func Start()  {
	flag.Parse()
	//websocket
	http.HandleFunc("/ws", server.WebSocketHandle)
	//静态页面
	http.Handle("/static/", http.StripPrefix("/static/", http.FileServer(http.Dir("web/static"))))
	log.Fatal(http.ListenAndServe(*addr, nil))

}

func webSocketHandle(rw http.ResponseWriter,r *http.Request){
	//访问html

}
