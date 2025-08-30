package com.defen.fojbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.defen.fojbackend.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class FojBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FojBackendApplication.class, args);
    }

}
