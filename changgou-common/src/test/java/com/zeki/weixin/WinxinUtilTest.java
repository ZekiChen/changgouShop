package com.zeki.weixin;

import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 微信SDK相关测试
 * @Author:      Zeki
 * @CreateDate:  2020/1/17 0017 上午 9:00
 */
public class WinxinUtilTest {

    /**
     * 生成随机字符串
     */
    @Test
    public void test1() throws Exception {
        String str = WXPayUtil.generateNonceStr();
        System.out.println("随机字符串：" + str);

        // Map转成XML字符串
        Map<String, String> map = new HashMap<>();
        map.put("id", "No.001");
        map.put("title", "畅购商城");
        map.put("money", "998");
        String xmlStr = WXPayUtil.mapToXml(map);
        System.out.println("XML字符串：\n" + xmlStr);
    }
}
