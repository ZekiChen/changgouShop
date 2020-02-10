package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Description: 商品Feign
 * @Author:      Zeki
 * @CreateDate:  2020/1/7 0007 下午 10:13
 */
@RequestMapping("/sku")
@FeignClient(value = "goods")
public interface SkuFeign {

    /**
     * 商品库存递减，key:商品id，value:递减数量
     */
    @GetMapping(value = "/decr/count")
    Result decrCount(@RequestParam Map<String, Integer> decrMap);

    /***
     * 查询Sku全部数据
     * @return
     */
    @GetMapping
    Result<List<Sku>> findAll();

    /**
     * 根据条件搜索
     */
    @PostMapping(value = "/search" )
    Result<List<Sku>> findList(@RequestBody(required = false) Sku sku);

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Sku> findById(@PathVariable String id);
}
