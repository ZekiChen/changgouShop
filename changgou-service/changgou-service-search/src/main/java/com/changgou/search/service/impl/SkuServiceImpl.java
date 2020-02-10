package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    // 可以实现索引库的CRUD（高级搜索）
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    /**
     * 条件搜索
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = buildBaseQuery(searchMap);  // 搜索条件封装

        Map<String, Object> resultMap = searchList(nativeSearchQueryBuilder);  // 集合搜索

        // 当用户已选择了分类，将分类作为搜索条件，则无需对分类进行分组搜索，因为分组搜索的数据是用于显示分类搜索条件的
        // 分类->searchMap->category
        // if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
        //     List<String> categoryList = searchCategoryList(nativeSearchQueryBuilder);  // 分类分组查询
        //     resultMap.put("categoryList", categoryList);
        // }

        // 当用户已选择了品牌，将品牌作为搜索条件，则无需对品牌进行分组搜索，因为分组搜索的数据是用于显示品牌搜索条件的
        // 分类->searchMap->brand
        // if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
        //     List<String> brandList = searchBrandList(nativeSearchQueryBuilder);  // 品牌分组查询
        //     resultMap.put("brandList", brandList);
        // }

        // Map<String, Set<String>> specList = searchSpecList(nativeSearchQueryBuilder);  // 规格分组查询
        // resultMap.put("specList", specList);

        Map<String, Object> groupMap = searchGroupList(nativeSearchQueryBuilder, searchMap);// 分组搜索
        resultMap.putAll(groupMap);
        return resultMap;
    }

    /**
     * 搜索条件封装
     * SELECT category_name FROM tb_sku WHERE name LIKE '%华为%'
     */
    private NativeSearchQueryBuilder buildBaseQuery(Map<String, String> searchMap) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();  // 搜索条件构建对象，用于封装各种搜索条件

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (searchMap != null && searchMap.size() > 0) {
            // 根据关键词搜索
            String keywords = searchMap.get("keywords");
            if (!StringUtils.isEmpty(keywords)) {
                // nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name"));
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }

            // 根据分类搜索
            String category = searchMap.get("category");
            if (!StringUtils.isEmpty(category)) {
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", category));
            }

            // 根据品牌搜索
            String brand = searchMap.get("brand");
            if (!StringUtils.isEmpty(brand)) {
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", brand));
            }

            // 根据规格搜索：spec_网络=移动3G&spec_颜色=红
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("spec_")) {
                    String value = entry.getValue().replace("\\", "");  // 特殊字符处理
                    // spec_网络，spec_ 前5个要去掉
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", value));
                }
            }

            // 根据价格区间搜索
            String price =searchMap.get("price");
            if (!StringUtils.isEmpty(price)) {
                // price：0-500元，500-100元，1000-1500元，1500-2000元，2000-2500元，2500-3000元，3000元以上
                price = price.replace("元", "").replace("以上", "");
                // 得去掉 "元" 和 "以上"，根据 "-" 分割，(0,500] (500,1000]...(3000]
                String[] prices = price.split("-");
                if (prices != null && prices.length > 0) {
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                    if (prices.length == 2) {
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(prices[1])));
                    }
                }
            }

            // 排序
            String sortField = searchMap.get("sortField");  // 指定排序的域
            String sortRule = searchMap.get("sortRule");  // 指定排序的规则
            if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)) {
                nativeSearchQueryBuilder.withSort(new FieldSortBuilder(sortField).order(SortOrder.valueOf(sortRule)));
            }
        }

        // 分页
        Integer pageNum = coverterPage(searchMap);  // 默认第1页
        Integer size = 30;  // 每页显示条数，默认30条
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum - 1, size));

        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        return nativeSearchQueryBuilder;
    }

    /**
     * 接收前端传入的分页参数
     */
    public Integer coverterPage(Map<String, String> searchMap) {
        if (searchMap != null) {
            String pageNum = searchMap.get("pageNum");
            try {
                return Integer.parseInt(pageNum);
            } catch (NumberFormatException e) {}
        }
        return 1;
    }

    /**
     * 集合搜索
     */
    private Map<String, Object> searchList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //配置高亮
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        field.preTags("<em style=\"color:red\">");  // 前缀
        field.postTags("</em>");  // 后缀
        field.fragmentOffset(100);  // 碎片长度：关键词数据的长度，有默认值
        // 添加高亮
        nativeSearchQueryBuilder.withHighlightFields(field);

        // 执行搜索
        // AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
         AggregatedPage<SkuInfo> page = elasticsearchTemplate
                 .queryForPage(nativeSearchQueryBuilder.build(),  // 搜索条件封装
                         SkuInfo.class,                           // 数据集合要转换的类型的字节码
                         new SearchResultMapper() {               // 执行搜索后，将数据结果集封装到该对象中
                             @Override
                             public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                                 List<T> list = new ArrayList<>();
                                 // 结果集中包括高亮数据和非高亮数据
                                 for (SearchHit hit : searchResponse.getHits()) {
                                     SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);  // 非高亮数据
                                     HighlightField highlightField = hit.getHighlightFields().get("name");  // 某个域的高亮数据

                                     if (highlightField != null && highlightField.getFragments() != null) {
                                         Text[] fragments = highlightField.getFragments();
                                         StringBuffer stringBuffer = new StringBuffer();
                                         for (Text fragment : fragments) {
                                             stringBuffer.append(fragment.toString());
                                         }
                                         skuInfo.setName(stringBuffer.toString());  // 把非高亮数据中指定的域替换成高亮数据
                                     }
                                     list.add((T) skuInfo);
                                 }
                                 return new AggregatedPageImpl<T>(list, pageable, searchResponse.getHits().totalHits);
                             }
                         });

        List<SkuInfo> skuInfoList = page.getContent();  // 获取数据结果集
        long totalElements = page.getTotalElements();  // 总记录数
        int totalPages = page.getTotalPages();  // 总页数

        // 封装一个Map存储所有数据并返回
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", skuInfoList);
        resultMap.put("total", totalElements);
        resultMap.put("totalPages", totalPages);

        // 获取搜索封装信息
        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        Pageable pageable = query.getPageable();
        int pageSize = pageable.getPageSize();  // 每页显示条数
        int pageNumber = pageable.getPageNumber();  // 当前页
        resultMap.put("pageSize",pageSize);
        resultMap.put("pageNumber",pageNumber);

        return resultMap;
    }

    /**
     * 分组查询->分类分组、品牌分组、规格分组
     */
    private Map<String, Object> searchGroupList(NativeSearchQueryBuilder nativeSearchQueryBuilder, Map<String, String> searchMap) {

        /**
         * 查询数据
         * .addAggregation()：添加一个聚合操作
         * 1)取别名
         * 2)表示根据哪个域进行分组查询
         */
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        Map<String, Object> groupMapResult = new HashMap<>();

        /**
         * 获取数据
         * page2.getAggregations()：获取的是集合，可以根据多个域进行分组
         * .get("skuCategory")：获取指定域的集合，[手机, 冰箱, 电视]
         */
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            StringTerms categoryTerms = page.getAggregations().get("skuCategory");
            List<String> categoryList = getGroupList(categoryTerms);
            groupMapResult.put("categoryList", categoryList);
        }

        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            StringTerms brandTerms = page.getAggregations().get("skuBrand");
            List<String> brandList = getGroupList(brandTerms);
            groupMapResult.put("brandList", brandList);
        }

        StringTerms specTerms = page.getAggregations().get("skuSpec");
        List<String> specList = getGroupList(specTerms);
        Map<String, Set<String>> specMap = putAllSpec(specList);
        groupMapResult.put("specList", specMap);
        return groupMapResult;
    }

    /**
     * 获取分组集合数据
     */
    private List<String> getGroupList(StringTerms stringTerms) {
        List<String> groupList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String fieldName = bucket.getKeyAsString();// 其中的一个分类名称
            groupList.add(fieldName);
        }
        return groupList;
    }

    /**
     * 规格分组查询
     * SELECT spec FROM tb_sku WHERE name LIKE '%华为%' GROUP BY spec
     * @return
     */
    private Map<String, Set<String>> searchSpecList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        /**
         * 查询数据
         * .addAggregation()：添加一个聚合操作
         * 1)取别名
         * 2)表示根据哪个域进行分组查询
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(1000));
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /**
         * 获取数据
         * page2.getAggregations()：获取的是集合，可以根据多个域进行分组
         * .get("skuSpec")：获取指定域的集合，[{'颜色': '紫色', '尺码': '150度'}, {'颜色': '黑色', '尺码': '250度'}]
         */
        StringTerms stringTerms = page.getAggregations().get("skuSpec");
        List<String> specList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String spec = bucket.getKeyAsString();  // 其中的一个规格名称，"{'颜色': '紫色', '尺码': '150度'}"
            specList.add(spec);
        }
        Map<String, Set<String>> allSpec = putAllSpec(specList);  // 规格汇总合并
        return allSpec;
    }

    /**
     * 规格汇总合并
     */
    private Map<String, Set<String>> putAllSpec(List<String> specList) {
        Map<String, Set<String>> allSpec = new HashMap<>();
        for (String spec : specList) {
            Map<String, String> specMap = JSON.parseObject(spec, Map.class);
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Set<String> specSet = allSpec.get(key);
                if (specSet == null) {
                    specSet = new HashSet<>();
                }
                specSet.add(value);
                allSpec.put(key, specSet);
            }
        }
        return allSpec;
    }

    /**
     * 品牌分组查询
     * SELECT brand_name FROM tb_sku WHERE name LIKE '%华为%' GROUP BY brand_name
     */
    private List<String> searchBrandList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        /**
         * 查询数据
         * .addAggregation()：添加一个聚合操作
         * 1)取别名
         * 2)表示根据哪个域进行分组查询
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /**
         * 获取数据
         * page2.getAggregations()：获取的是集合，可以根据多个域进行分组
         * .get("skuBrand")：获取指定域的集合，[华为, 小米, TCL]
         */
        StringTerms stringTerms = page.getAggregations().get("skuBrand");
        List<String> brandList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String brandName = bucket.getKeyAsString();// 其中的一个品牌名称
            brandList.add(brandName);
        }
        return brandList;
    }

    /**
     * 分类分组查询
     * SELECT category_name FROM tb_sku WHERE name LIKE '%华为%' GROUP BY category_name
     */
    private List<String> searchCategoryList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        /**
         * 查询数据
         * .addAggregation()：添加一个聚合操作
         * 1)取别名
         * 2)表示根据哪个域进行分组查询
         */
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        /**
         * 获取数据
         * page2.getAggregations()：获取的是集合，可以根据多个域进行分组
         * .get("skuCategory")：获取指定域的集合，[手机, 冰箱, 电视]
         */
        StringTerms stringTerms = page.getAggregations().get("skuCategory");
        List<String> categoryList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String categoryName = bucket.getKeyAsString();// 其中的一个分类名称
            categoryList.add(categoryName);
        }
        return categoryList;
    }

    @Override
    public void importData() {
        Result<List<Sku>> skuResult = skuFeign.findAll();
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuResult.getData()), SkuInfo.class);
        for (SkuInfo skuInfo : skuInfoList) {
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);
            // 如果需要生成动态的域，只需要将该域存入到一个Map<String, Object>对象中即可
            // 该Map的key会生成一个域，域的名字为该Map的key，value会作为该域对应的值
            skuInfo.setSpecMap(specMap);
        }
        skuEsMapper.saveAll(skuInfoList);
    }
}
