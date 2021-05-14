package com.moumangtai.micro_kill;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public FanoutExchange getExchange(){
        return new FanoutExchange("seckill_exchange",true,false);
    }
}
