package com.moumangtai.controller;

import com.moumangtai.entity.ResultData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/kill")
@CrossOrigin
public class KillController {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private String lua = "--获取下单的商品id\n" +
            "local gid = KEYS[1]\n" +
            "--获取下单数量\n" +
            "local gnumber = tonumber(ARGV[1])\n" +
            "\n" +
            "--进行库存的判定\n" +
            "--获取当前商品的库存\n" +
            "local gsave = tonumber(redis.call('get','goods'..gid) or 0)\n" +
            "\n" +
            "--判断库存\n" +
            "if gsave < gnumber then\n" +
            "   --库存不足\n" +
            "   return -1;\n" +
            "end\n" +
            "\n" +
            "--库存充足，进行库存扣减\n" +
            "local result = redis.call('decrby','goods'..gid,gnumber)\n" +
            "\n" +
            "--返回结果，抢购成功\n" +
            "if result > 0 then\n" +
            "   --抢购成功，但是还有库存可以继续\n" +
            "   return 1\n" +
            "   else\n" +
            "   --抢购成功，并且已经没有库存\n" +
            "   return 0\n" +
            "end";


    @RequestMapping("/qianggou")
    public ResultData<Boolean> qiangGou(Integer gid){
        System.out.println("获取秒杀请求： "+gid+" " +new SimpleDateFormat("HH:mm:ss").format(new Date()));

        //通过lua脚本进行库存扣减
        int result = redisTemplate.execute(new DefaultRedisScript<Long>(lua,Long.class),
                Collections.singletonList(gid+""),
        "1").intValue();
        if(result >=  0){
            //通过MQ发送给下游服务（分布式事务的最终一致性，请求削峰）
            //商品id 用户id 商品数量 订单号
            Map<String,Object> map = new HashMap<>();
            map.put("gid",gid);
            map.put("uid",1);
            map.put("gnumber",1);

            //发送到消息队列
            rabbitTemplate.convertAndSend("seckill_exchange","",map);


            //抢购成功
            return new ResultData<Boolean>().setData(true);

        }

        return new ResultData<Boolean>().setCode(ResultData.Code.ERROR).setMsg("抢购失败，库存不足").setData(false);
    }
}