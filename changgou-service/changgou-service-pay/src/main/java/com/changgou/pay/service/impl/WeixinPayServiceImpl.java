package com.changgou.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${weixin.appid}")
    private String appid;  // 应用id

    @Value("${weixin.partner}")
    private String partner;  // 商户号

    @Value("${weixin.partnerkey}")
    private String partnerkey;  // 秘钥

    @Value("${weixin.notifyurl}")
    private String notifyurl;  // 支付回调地址


    @Override
    public Map createNative(Map<String, String> parameterMap) {
        try {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", appid);
            paramMap.put("mch_id", partner);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());  // 随机字符串
            paramMap.put("body", "畅购商城商品不错！");
            paramMap.put("out_trade_no", parameterMap.get("outTradeNo"));  // 订单号
            paramMap.put("total_fee", parameterMap.get("totalFee"));  // 交易金额，单位：分
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", notifyurl);  // 交易结果回调通知结果
            paramMap.put("trade_type", "NATIVE");

            // 获取自定义数据
            String exchange = parameterMap.get("exchange");
            String routingKey = parameterMap.get("routingKey");
            Map<Object, Object> attachMap = new HashMap<>();
            attachMap.put("exchange", exchange);
            attachMap.put("routingKey", routingKey);
            String username = parameterMap.get("username");  // 如果是秒杀订单，需要传username
            if (!StringUtils.isEmpty(username)) {
                attachMap.put("username", username);
            }
            String attach = JSON.toJSONString(attachMap);
            paramMap.put("attach", attach);

            String xmlParameters = WXPayUtil.generateSignedXml(paramMap, partnerkey);  // Map转成XML字符串，可以携带签名
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";  // 微信统一下单API
            Map<String, String> resultMap = sendToWeixin(xmlParameters, url);  // 请求微信接口并返回数据
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map queryStatus(String outTradeNo) {
        try {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("appid", appid);
            paramMap.put("mch_id", partner);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());  // 随机字符串
            paramMap.put("out_trade_no", outTradeNo);  // 订单号

            String xmlParameters = WXPayUtil.generateSignedXml(paramMap, partnerkey);  // Map转成XML字符串，可以携带签名
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";  // 微信统一下单API
            Map<String, String> resultMap = sendToWeixin(xmlParameters, url);  // 请求微信接口并返回数据
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 请求微信接口并返回数据（https、POST）
     */
    private Map<String, String> sendToWeixin(String xmlParameters, String url) throws Exception {
        HttpClient httpClient = new HttpClient(url);
        httpClient.setHttps(true);
        httpClient.setXmlParam(xmlParameters);
        httpClient.post();
        String xmlResult = httpClient.getContent();
        return WXPayUtil.xmlToMap(xmlResult);  // 将响应的xml数据转成Map
    }
}
