package com.changgou.order.mq.listen;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: 过期消息监听
 * @Author:      Zeki
 * @CreateDate:  2020/1/18 0018 上午 12:12
 */
@Component
@RabbitListener(queues = "orderListenerQueue")
public class DelayMessageListener {

    /**
     * 延时队列监听，然后关闭订单，取消订单，回滚库存
     */
    @RabbitHandler
    public void getDelayMessage(String message) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("监听消息的时间：" + format.format(new Date()));
        System.out.println("监听到的消息：" + message);
    }
}
