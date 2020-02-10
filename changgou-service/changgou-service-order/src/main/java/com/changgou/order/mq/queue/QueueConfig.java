package com.changgou.order.mq.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 延时消息队列配置
 * @Author:      Zeki
 * @CreateDate:  2020/1/17 0017 下午 11:37
 */
@Configuration
public class QueueConfig {

    /**
     * 创建Queue1：延时队列，会过期，过期后将数据发给Queue2（一旦过期，就会进入死信队列）
     * 死信队列：消息过了有效期，仍未被读
     */
    @Bean  // @Bean(name="默认为方法名")
    public Queue orderDelayQueue() {
        return QueueBuilder
                .durable("orderDelayQueue")
                .withArgument("x-dead-letter-exchange", "orderListenerExchange")  // 死信队列数据绑定到其他交换机中
                .withArgument("x-dead-letter-routing-key", "orderListenerQueue")  // 死信队列数据路由到其他队列
                .build();
    }

    /**
     * 创建Queue2
     */
    @Bean
    public Queue orderListenerQueue() {
        return new Queue("orderListenerQueue", true);
    }

    /**
     * 创建交换机
     */
    @Bean
    public Exchange orderListenerExchange() {
        return new DirectExchange("orderListenerExchange");
    }

    /**
     * 队列Queue2绑定Exchange
     */
    @Bean
    public Binding queueToExchange(Queue orderListenerQueue, Exchange orderListenerExchange) {
        return BindingBuilder.bind(orderListenerQueue).to(orderListenerExchange).with("orderListenerQueue").noargs();
    }
}
