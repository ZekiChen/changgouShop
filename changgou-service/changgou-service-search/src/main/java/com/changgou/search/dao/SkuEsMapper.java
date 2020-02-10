package com.changgou.search.dao;

import com.changgou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @Description: ES相关Mapper
 * @Author:      Zeki
 * @CreateDate:  2020/1/7 0007 下午 10:29
 */
@Repository                                                     // 主键类型
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo, Long> {}
