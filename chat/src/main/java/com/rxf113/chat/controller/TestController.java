//package com.rxf113.chat.controller;
//
//import com.rxf113.chat.MJ.DataHandle;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.HashMap;
//import java.util.Map;
//
//@Controller
//@RequestMapping(value = "/")
//public class TestController {
//    @GetMapping(value = "test")
//    @ResponseBody
//    public Object test1(HttpServletRequest request,String cookie){
//        if(cookie != null && cookie.indexOf("aliyungf_tc") == 0){
//            DataHandle.setCookie(cookie);
//        }
//        Map resultMap = new HashMap();
//        resultMap.put("msg","测试msg");
//        resultMap.put("code","200");
//        return resultMap;
//    }
//}
