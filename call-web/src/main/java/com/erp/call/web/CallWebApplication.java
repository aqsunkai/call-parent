package com.erp.call.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CallWebApplication {

    public static void main(String[] args) {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            //如果是Windows系统
            System.setProperty("log.path", "D:/call/logs");
        } else {
            //linux 和mac
            System.setProperty("log.path", "/Users/zhangchenghui/sunkai/call/logs");
        }
        SpringApplication.run(CallWebApplication.class, args);
    }

}
