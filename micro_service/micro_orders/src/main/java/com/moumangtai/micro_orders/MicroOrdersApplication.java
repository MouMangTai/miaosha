package com.moumangtai.micro_orders;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.moumangtai")
@MapperScan("com.moumangtai.dao")
public class MicroOrdersApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroOrdersApplication.class, args);
    }

}
