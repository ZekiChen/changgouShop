package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Description: 网关服务引导类
 * @Author:      Zeki
 * @CreateDate:  2020/1/11 0011 下午 7:49
 */
@SpringBootApplication
@EnableEurekaClient
public class GatewayWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayWebApplication.class,args);
    }

    /**
     * 根据用户IP来创建用户唯一标识，根据用户IP进行限流
     */
    @Bean(name = "ipKeyResolver")  // 把这个对象交给Spring容器管理，Bean的名字叫ipKeyResolver
    public KeyResolver userKeyResolver() {
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                String ip = exchange.getRequest().getRemoteAddress().getHostString();
                System.out.println(ip);
                return Mono.just(ip);  // 需要使用的用户身份识别唯一标识，可以用IP
            }
        };
    }

}