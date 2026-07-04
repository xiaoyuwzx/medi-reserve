package com.medireserve.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = "com.medireserve",
        exclude = {
                DataSourceAutoConfiguration.class,                // 排除数据源自动配置
                DataSourceTransactionManagerAutoConfiguration.class // 排除事务管理器自动配置
        }
)
public class WebsocketApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebsocketApplication.class, args);
    }
}