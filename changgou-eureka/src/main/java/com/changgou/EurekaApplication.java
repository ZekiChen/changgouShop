package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @Description: Eureka引导类
 * @Author:      Chenzk
 * @CreateDate:  2020/1/1 0001 上午 11:35
 */
@SpringBootApplication( )
@EnableEurekaServer  // 开启Eureka服务
public class EurekaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }
}
