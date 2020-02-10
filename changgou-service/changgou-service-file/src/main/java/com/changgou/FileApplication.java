package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @Description: 文件管理引导类
 * @Author:      Chenzk
 * @CreateDate:  2020/1/3 0003 下午 7:34
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)  // 排除掉数据库自动加载，不然会报错，因为引入了数据库相关包却不用之
@EnableEurekaClient
public class FileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}
