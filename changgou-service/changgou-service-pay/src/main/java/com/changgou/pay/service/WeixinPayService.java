package com.changgou.pay.service;

import java.util.Map;

public interface WeixinPayService {

    /**
     * 创建二维码链接
     */
    Map createNative(Map<String, String> parameterMap);

    /**
     * 查询订单支付状态
     */
    Map queryStatus(String outTradeNo);
}
