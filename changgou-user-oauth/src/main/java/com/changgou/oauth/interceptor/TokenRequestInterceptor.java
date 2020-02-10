package com.changgou.oauth.interceptor;

import com.changgou.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {

    /**
     * Feign执行之前进行拦截
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = AdminToken.adminToken();// 生成admin令牌
        requestTemplate.header("Authorization", "bearer " + token);
    }
}
