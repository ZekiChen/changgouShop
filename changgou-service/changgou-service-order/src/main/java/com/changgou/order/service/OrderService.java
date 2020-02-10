package com.changgou.order.service;

import com.changgou.pojo.Order;
import com.github.pagehelper.PageInfo;

import java.text.ParseException;
import java.util.List;

/****
 * @Author:shenkunlin
 * @Description:Order业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface OrderService {

    /**
     * 删除订单回滚库存（实际是逻辑删除，只需修改订单状态）
     */
    void deleteOrder(String outTradeNo);

    /**
     * 修改订单状态
     * 1、修改支付时间
     * 2、修改支付状态
     * @param outTradeNo：订单号
     * @param payTime：支付时间
     * @param transactionId：交易流水号
     */
    void updateStatus(String outTradeNo, String payTime, String transactionId) throws ParseException;

    /***
     * Order多条件分页查询
     * @param order
     * @param page
     * @param size
     * @return
     */
    PageInfo<Order> findPage(Order order, int page, int size);

    /***
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Order> findPage(int page, int size);

    /***
     * Order多条件搜索方法
     * @param order
     * @return
     */
    List<Order> findList(Order order);

    /***
     * 删除Order
     * @param id
     */
    void delete(String id);

    /***
     * 修改Order数据
     * @param order
     */
    void update(Order order);

    /***
     * 添加订单
     * @param order
     */
    void add(Order order);

    /**
     * 根据ID查询Order
     * @param id
     * @return
     */
     Order findById(String id);

    /***
     * 查询所有Order
     * @return
     */
    List<Order> findAll();
}
