package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 微信支付相关Controller
 * @Author:      Zeki
 * @CreateDate:  2020/1/17 0017 上午 10:41
 */
@RestController
@RequestMapping(value = "/weixin/pay")
public class WeixinPayController {

    @Autowired
    private WeixinPayService weixinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建支付二维码链接
     *
     * 普通订单：
     *      exchange: exchange.order
     *      routingKey: queue.order
     * 秒杀订单：
     *      exchange: exchange.seckillorder
     *      routingKey: queue.seckillorder
     *
     * exchange + routingKey -> JSON -> attach
     */
    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String, String> parameterMap){
        Map<String,String> resultMap = weixinPayService.createNative(parameterMap);
        return new Result(true, StatusCode.OK,"创建二维码预付订单成功！",resultMap);
    }

    /**
     * 查询订单支付状态
     */
    @GetMapping(value = "/status/query")
    public Result queryStatus(@RequestParam String outTradeNo){
        Map<String,String> resultMap = weixinPayService.queryStatus(outTradeNo);
        return new Result(true,StatusCode.OK,"查询订单支付状态成功！",resultMap);
    }

    /**
     * 支付结果通知回调
     */
    @RequestMapping(value = "/notify/url")
    public String notifyUrl(HttpServletRequest request) {
        InputStream inStream;
        try {
            // 读取支付回调数据
            inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            outSteam.close();
            inStream.close();
            // 将支付回调数据转换成xml字符串
            String result = new String(outSteam.toByteArray(), "utf-8");
            // 将xml字符串转换成Map结构
            Map<String, String> map = WXPayUtil.xmlToMap(result);

            String attach = map.get("attach");  // 获取自定义数据
            Map<String, String> attachMap = JSON.parseObject(attach, Map.class);
            System.out.println(map);

            // 将消息发送给RabbitMQ
            rabbitTemplate.convertAndSend(attachMap.get("exchange"), attachMap.get("routingKey"), JSON.toJSONString(map));

            // 响应数据设置
            Map respMap = new HashMap();
            respMap.put("return_code","SUCCESS");
            respMap.put("return_msg","OK");
            return WXPayUtil.mapToXml(respMap);
        } catch (Exception e) {
            e.printStackTrace();
            // 记录错误日志
        }
        return null;
    }
}
