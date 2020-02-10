package com.changgou.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Description: 开始BB
 * @Author:      Zeki
 * @CreateDate:  2020/1/9 0009 下午 2:49
 */
@FeignClient(name = "search")
@RequestMapping("/search")
public interface SkuFeign {

    /**
     * 搜索
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map<String, String> searchMap);
}
