package com.changgou.search.controller;

import com.changgou.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description: ES相关Controller
 * @Author:      Zeki
 * @CreateDate:  2020/1/7 0007 下午 10:40
 */
@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 搜索
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map<String, String> searchMap) {
        return skuService.search(searchMap);
    }

    /**
     * 查出数据并导入到索引库中
     */
    @GetMapping(value = "/import")
    public Result importData() {
        skuService.importData();
        return new Result(true, StatusCode.OK, "执行导入操作成功！");
    }
}
