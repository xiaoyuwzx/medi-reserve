package com.medireserve.websocket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * WebSocket 服务启动类
 * 负责在线问诊的实时通信功能
 */
@SpringBootApplication(scanBasePackages = "com.medireserve")
@MapperScan(basePackages = {"com.medireserve.common.mapper", "com.medireserve.websocket.mapper"})
@EnableAsync  // 启用异步处理，提升消息吞吐量
public class WebsocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebsocketApplication.class, args);
    }
}