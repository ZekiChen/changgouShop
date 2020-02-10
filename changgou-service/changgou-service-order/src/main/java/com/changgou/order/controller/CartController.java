package com.changgou.order.controller;

import com.changgou.order.service.CartService;
import com.changgou.pojo.OrderItem;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Description: 购物车操作
 * @Author:      Zeki
 * @CreateDate:  2020/1/15 0015 下午 2:05
 */
@RestController
@RequestMapping(value = "/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 购物车列表
     */
    @RequestMapping(value = "/list")
    public Result<List<OrderItem>> list() {
        // 用户的令牌信息->解析令牌信息->username
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        List<OrderItem> orderItems = cartService.list(username);
        return new Result<>(true, StatusCode.OK, "购物车列表查询成功！", orderItems);
    }

    /**
     * 加入购物车
     * @param num：加入购物车数量
     * @param id：商品id
     */
    @RequestMapping(value = "/add")
    public Result add(Integer num, Long id) {
        cartService.add(num, id, "szitheima");
        return new Result(true, StatusCode.OK, "加入购物车成功！");
    }


    /**
     * 购物车列表
     */
}
