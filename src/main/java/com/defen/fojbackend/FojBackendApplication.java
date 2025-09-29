package com.defen.fojbackend;

import com.defen.fojbackend.rabbitmq.InitRabbitMq;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.defen.fojbackend.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class FojBackendApplication {

    public static void main(String[] args) {
        // 初始化消息队列
        InitRabbitMq.doInit();
        SpringApplication.run(FojBackendApplication.class, args);
    }

}
