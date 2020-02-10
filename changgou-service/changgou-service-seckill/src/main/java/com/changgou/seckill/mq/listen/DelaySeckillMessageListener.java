package com.changgou.seckill.mq.listen;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import entity.SeckillStatus;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 监听秒杀订单消息，并根据支付状态处理订单
 * @Author:      Zeki
 * @CreateDate:  2020/1/19 0019 下午 8:06
 */
@Component
@RabbitListener(queues = "seckillQueue")
public class DelaySeckillMessageListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 支付结果监听
     */
    @RabbitHandler
    public void getMessage(String message) {
        SeckillStatus seckillStatus = JSON.parseObject(message, SeckillStatus.class);  // 获取用户排队信息

        // 如果此时Redis中没有该用户的排队信息，则表明该订单已经处理，如果有用户排队信息，则表明用户尚未支付，关闭微信支付、取消订单、回滚库存
        Object userQueueStatus = redisTemplate.boundHashOps("UserQueueStatus").get(seckillStatus.getUsername());
        if (userQueueStatus != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("回滚时间监听：" + format.format(new Date()));

            seckillOrderService.deleteOrder(seckillStatus.getUsername());  //删除订单、回滚库存
            // 待完成：关闭微信支付
        }
    }
}
