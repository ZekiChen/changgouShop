package com.changgou.order.service;

import com.changgou.pojo.OrderItem;

import java.util.List;

public interface CartService {

    /**
     * 加入购物车
     */
    void add(Integer num, Long id, String username);

    /**
     * 购物车集合
     */
    List<OrderItem> list(String username);
}
