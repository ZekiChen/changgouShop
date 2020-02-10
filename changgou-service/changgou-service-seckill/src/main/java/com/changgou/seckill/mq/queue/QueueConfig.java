package com.changgou.seckill.mq.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description: 延时消息队列配置
 * @Author:      Zeki
 * @CreateDate:  2020/1/19 0019 下午 7:47
 */
@Configuration
public class QueueConfig {

    /**
     * 延时超时队列->负责数据暂时存储  Queue1
     */
    @Bean
    public Queue delaySeckillQueue() {
        return QueueBuilder.durable("delaySeckillQueue")
                .withArgument("x-dead-letter-exchange", "seckillExchange")  // 死信队列数据绑定到其他交换机中
                .withArgument("x-dead-letter-routing-key", "seckillQueue")  // 死信队列数据路由到其他队列
                .build();
    }

    /**
     * 真正监听的消息队列             Queue2
     */
    @Bean
    public Queue seckillQueue() {
        return new Queue("seckillQueue");
    }

    /**
     * 创建秒杀交换机
     */
    @Bean
    public Exchange seckillExchange() {
        return new DirectExchange("seckillExchange");
    }

    /**
     * 队列绑定交换机
     */
    @Bean
    public Binding seckillQueueToExchange(Queue seckillQueue, Exchange seckillExchange) {
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with("seckillQueue").noargs();
    }
}
