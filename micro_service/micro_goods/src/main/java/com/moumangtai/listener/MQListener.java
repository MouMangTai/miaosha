package com.moumangtai.listener;

import com.moumangtai.service.IGoodsService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MQListener {
    @Autowired
    private IGoodsService goodsService;

    //@Queue(name = "goods_queue",durable = "true") 创建一个队列
    //@Exchange(name = "seckill_exchange",type = "fanout",declare = "true")创建一个交换机
    //@QueueBinding 将交换机和队列绑定在一起
    //@RabbitListener 自动监听绑定的队列
    @RabbitListener(bindings =
        @QueueBinding(value = @Queue(name = "goods_queue",durable = "true"),
        exchange = @Exchange(name = "seckill_exchange",type = "fanout",declare = "true")))
    public void msgHandler(Map<String,Object> msg){
        System.out.println("商品服务接收到消息："+msg);
        //读取消息
        Integer gid = (Integer) msg.get("gid");
        Integer uid = (Integer) msg.get("uid");
        Integer gnumber = (Integer) msg.get("gnumber");
        System.out.println(gid+" "+uid+" "+gnumber);
        //扣除商品的库存
        goodsService.updateGoodsSave(gid,gnumber);
    }
}
