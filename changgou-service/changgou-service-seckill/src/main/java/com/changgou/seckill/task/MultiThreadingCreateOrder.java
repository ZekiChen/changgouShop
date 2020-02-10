package com.changgou.seckill.task;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import entity.IdWorker;
import entity.SeckillStatus;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description: Spring提供的异步操作，底层还是多线程，也用到了线程池
 * @Author:      Zeki
 * @CreateDate:  2020/1/18 0018 下午 4:39
 */
@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 多线程下单操作
     */
    @Async  // 该方法会异步执行
    public void createOrder() {
        try {
            System.out.println("准备睡会再下单！");
            Thread.sleep(10000);

            // 从Redis队列中获取用户排队信息
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();
            if (seckillStatus == null) {
                return;
            }

            String time = seckillStatus.getTime();
            Long id = seckillStatus.getGoodsId();
            String username = seckillStatus.getUsername();

            // 先到SeckillGoodsCountList_ID队列中获取该商品的一个信息，如果能获取，则可以下单
            Object goodsId = redisTemplate.boundListOps("SeckillGoodsCountList_" + id).rightPop();
            if (goodsId == null) {  // 如果不能获取商品的队列信息，则表示没有库存，清理排队信息
                clearUserQueue(username);
                return;
            }

            String namespace = "SeckillGoods_" + time;
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(namespace).get(id);  // 查询秒杀商品

            if (seckillGoods == null || seckillGoods.getStockCount() <= 0) {  // 判断有没有库存
                throw new RuntimeException("已售罄");
            }

            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setSeckillId(id);  // 商品id
            seckillOrder.setMoney(seckillGoods.getCostPrice());  // 支付金额
            seckillOrder.setUserId(username);
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0");  // 未支付

            // 将秒杀订单存入到Redis中，一个用户只允许有一个未支付的秒杀订单
            redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);

            // 库存递减，如果商品递减后库存为0，则将Redis中该商品信息删除，并同步到MySQL
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            // 获取该商品对应的队列库存数量
            Long size = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).size();
            if (size <= 0) {
                seckillGoods.setStockCount(size.intValue());  // 同步数量
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                redisTemplate.boundHashOps(namespace).delete(id);
            } else {
                redisTemplate.boundHashOps(namespace).put(id, seckillGoods);
            }

            // 更新下单状态
            seckillStatus.setOrderId(seckillOrder.getId());
            seckillStatus.setMoney(Float.valueOf(seckillGoods.getCostPrice()));  // 支付金额
            seckillStatus.setStatus(2);  // 等待秒杀支付
            redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println("秒杀商品下单时间：" + format.format(new Date()));

            // 发送消息给延时队列
            rabbitTemplate.convertAndSend("delaySeckillQueue", (Object) JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setExpiration("10000");  // 10秒后超时
                    return message;
                }
            });

            System.out.println("下单完成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 清理用户排队抢单信息
     */
    public void clearUserQueue(String username) {
        // 清理排队标识（用于记录用户排队的次数）
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        // 清理排队信息状态（用于查询用户抢单状态）
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);
    }
}
