package com.huang.redblack;

import com.huang.redblack.commen.properties.JWTProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value = {JWTProperties.class})
@MapperScan("com.huang.redblack.mapper")
public class RedBlackApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedBlackApplication.class, args);
    }

}
