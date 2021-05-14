package com.moumangtai.micro_goods;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = "com.moumangtai")
@MapperScan("com.moumangtai.dao")
@EnableCaching
public class MicroGoodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroGoodsApplication.class, args);
    }

}
