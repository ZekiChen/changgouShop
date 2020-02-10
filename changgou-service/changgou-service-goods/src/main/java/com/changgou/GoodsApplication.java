package com.changgou;

import entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Description: goods服务启动类
 * @Author:      Chenzk
 * @CreateDate:  2020/1/1 0001 下午 5:31
 */
@SpringBootApplication
@EnableEurekaClient  // 开启Eureka客户端
@MapperScan(basePackages = {"com.changgou.goods.dao"})  // 开启通用Mapper的包扫描
public class GoodsApplication {

    public static void main(String[] args) { SpringApplication.run(GoodsApplication.class ,args); }

    /***
     * ID生成器（根据雪花算法生成分布式系统不重复的ID，每秒约可生成26万个）
     * @return
     */
    @Bean
    public IdWorker idWorker(){
        return new IdWorker(0,0);
    }
}
