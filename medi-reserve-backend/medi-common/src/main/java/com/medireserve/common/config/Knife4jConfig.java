package com.medireserve.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediReserve 智慧医疗平台 API")
                        .version("1.0.0")
                        .description("患者端、医生端、管理端、WebSocket 统一接口文档")
                        .contact(new Contact()
                                .name("你的名字")
                                .email("your-email@example.com")
                                .url("https://github.com/your-profile"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                );
    }
}