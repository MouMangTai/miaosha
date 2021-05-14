package com.moumangtai.micro_kill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = "com.moumangtai",exclude = DataSourceAutoConfiguration.class)
public class MicroKillApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroKillApplication.class, args);
    }

}
