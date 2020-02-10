package com.changgou.seckill.mq.listen;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Description: 监听秒杀订单消息，并根据支付状态处理订单
 * @Author:      Zeki
 * @CreateDate:  2020/1/19 0019 下午 3:58
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SeckillMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 支付结果监听
     */
    @RabbitHandler
    public void getMessage(String message) throws Exception {
        Map<String, String> resultMap = JSON.parseObject(message, Map.class);
        String returnCode = resultMap.get("return_code");  // 通信标识
        String outTradeNo = resultMap.get("out_trade_no"); // 订单号
        String attach = resultMap.get("attach");// 自定义数据
        Map<String, String> attachMap = JSON.parseObject(attach, Map.class);
        if (returnCode.equals("SUCCESS")) {  // 业务结果
            String resultCode = resultMap.get("result_code");
            if (resultCode.equals("SUCCESS")) {
                // 更新订单状态、删除用户排队信息
                seckillOrderService.updatePayStatus(attachMap.get("username"), resultMap.get("transaction_id"), resultMap.get("time_end"));
            } else {
                // 删除订单、回滚库存
                seckillOrderService.deleteOrder(attachMap.get("username"));
            }
        }
    }
}
