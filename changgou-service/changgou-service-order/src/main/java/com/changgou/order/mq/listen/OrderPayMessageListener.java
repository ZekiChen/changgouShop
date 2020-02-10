package com.changgou.order.mq.listen;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Map;

/**
 * @Description: 监听普通订单消息，并根据支付状态处理订单
 * @Author:      Zeki
 * @CreateDate:  2020/1/17 0017 下午 9:06
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.order}")
public class OrderPayMessageListener {

    @Autowired
    private OrderService orderService;

    /**
     * 支付结果监听
     */
    @RabbitHandler
    public void getMessage(String message) throws ParseException {
        Map<String, String> resultMap = JSON.parseObject(message, Map.class);
        System.out.println(resultMap);
        String returnCode = resultMap.get("return_code");  // 通信标志
        if (returnCode.equals("SUCCESS")) {
            String resultCode = resultMap.get("result_code");  // 业务结果
            String outTradeNo = resultMap.get("out_trade_no");  // 订单号
            if (resultCode.equals("SUCCESS")) {  // 支付成功，修改订单状态
                orderService.updateStatus(outTradeNo, resultMap.get("time_end").toString(), resultMap.get("transaction_id"));
            } else {  // 支付失败，关闭支付，取消订单，回滚库存
                orderService.delete(outTradeNo);
                // 待解决：1、关闭支付   2、回滚库存
            }
        }
    }
}
