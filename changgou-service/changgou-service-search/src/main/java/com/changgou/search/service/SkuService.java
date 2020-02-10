package com.changgou.search.service;

import java.util.Map;

/**
 * @Description: ES相关接口
 * @Author:      Zeki
 * @CreateDate:  2020/1/7 0007 下午 10:24
 */
public interface SkuService {

    /**
     * 条件搜索
     */
    Map<String, Object> search(Map<String, String> searchMap);

    /**
     * 查出数据并导入到索引库中
     */
    void importData();
}
