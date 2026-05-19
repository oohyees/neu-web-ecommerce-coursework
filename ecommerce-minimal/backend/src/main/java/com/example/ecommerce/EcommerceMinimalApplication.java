package com.example.ecommerce;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
    org.apache.shiro.spring.boot.autoconfigure.ShiroAutoConfiguration.class,
    org.apache.shiro.spring.boot.autoconfigure.ShiroAnnotationProcessorAutoConfiguration.class
})
@MapperScan("com.example.ecommerce.mapper")
public class EcommerceMinimalApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceMinimalApplication.class, args);
    }
}
