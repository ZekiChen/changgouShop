package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @Description: 搜索服务页面渲染Controller
 * @Author:      Zeki
 * @CreateDate:  2020/1/9 0009 下午 3:26
 */
@Controller
@RequestMapping(value = "/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    /**
     * 搜索
     */
    @GetMapping(value = "/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model) {
        handlerSearchMap(searchMap);  // 特殊字符处理，例如请求参数spec_规格=8GB+64GB中的"+"

        Map<String, Object> resultMap = skuFeign.search(searchMap);
        model.addAttribute("result", resultMap);
        model.addAttribute("searchMap", searchMap);

        Page<SkuInfo> pageInfo = new Page<>(
                Long.parseLong(resultMap.get("total").toString()),
                Integer.parseInt(resultMap.get("pageNumber").toString()) + 1,
                Integer.parseInt(resultMap.get("pageSize").toString())
        );
        model.addAttribute("pageInfo", pageInfo);

        String[] urls = url(searchMap);
        model.addAttribute("url", urls[0]);
        model.addAttribute("sortUrl", urls[1]);
        return "search";
    }

    /**
     * 拼接组装用户请求的URL地址
     * http://localhlst:18086/search/list?keywords=华为&category=手机
     * @return
     */
    public String[] url(Map<String, String> searchMap) {
        String url = "/search/list";
        String sortUrl = "/search/list";
        if (searchMap != null && searchMap.size() > 0) {
            url += "?";
            sortUrl += "?";
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                if (key.equalsIgnoreCase("pageNum")) {
                    continue;
                }

                String value = entry.getValue();
                url += key + "=" + value + "&";

                // 排序参数，直接跳过
                if (key.equalsIgnoreCase("sortField") || key.equalsIgnoreCase("sortRule")) {
                    continue;
                }
                sortUrl += key + "=" + value + "&";
            }
            url = url.substring(0, url.length() - 1);
            sortUrl = sortUrl.substring(0, sortUrl.length() - 1);
        }
        return new String[] {url, sortUrl};
    }

    /**
     * 替换特殊字符
     */
    private void handlerSearchMap(Map<String, String> searchMap) {
        if (searchMap != null) {
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                if (entry.getKey().startsWith("spec_")) {
                    entry.setValue(entry.getValue().replace("+", "%2B"));
                }
            }
        }
    }
}
