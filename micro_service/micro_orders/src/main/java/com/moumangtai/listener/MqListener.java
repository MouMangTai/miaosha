package com.moumangtai.listener;

import com.moumangtai.entity.Orders;
import com.moumangtai.service.IOrderService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class MqListener {
    @Autowired
    private IOrderService orderService;

    //@Queue(name = "goods_queue",durable = "true") 创建一个队列
    //@Exchange(name = "seckill_exchange",type = "fanout",declare = "true")创建一个交换机
    //@QueueBinding 将交换机和队列绑定在一起
    //@RabbitListener 自动监听绑定的队列
    @RabbitListener(bindings =
    @QueueBinding(value = @Queue(name = "orders_queue",durable = "true"),
            exchange = @Exchange(name = "seckill_exchange",type = "fanout",declare = "true")))
    public void msgHandler(Map<String,Object> msg){
        System.out.println("订单服务接收到消息："+msg);
        //读取消息
        Integer gid = (Integer) msg.get("gid");
        Integer uid = (Integer) msg.get("uid");
        Integer gnumber = (Integer) msg.get("gnumber");

        //生成订单
        Orders orders = new Orders()
                .setGid(gid)
                .setUid(uid)
                .setGnumber(gnumber)
                .setAllPrice(BigDecimal.valueOf(998.00))
                .setOid(UUID.randomUUID().toString())
//                .setCreateTime(new Timestamp(new Date().getTime()))
                .setStatus(0);
        //保存订单
        orderService.save(orders);
    }
}
