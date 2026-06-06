package com.example.ecommerce;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.ecommerce.mapper")
public class EcommerceMinimalApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceMinimalApplication.class, args);
    }
}
