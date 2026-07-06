package com.unilife;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.unilife.mapper")
@SpringBootApplication
public class UniLifeApplication {
    //
    public static void main(String[] args) {
        SpringApplication.run(UniLifeApplication.class, args);
    }

}
