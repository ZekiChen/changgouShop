package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.service.CartService;
import com.changgou.pojo.OrderItem;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    @Override
    public List<OrderItem> list(String username) {
        return redisTemplate.boundHashOps("Cart_" + username).values();
    }

    @Override
    public void add(Integer num, Long id, String username) {
        // 当添加购物车数量<=0的时候，需要移除改商品信息
        if (num <= 0) {
            redisTemplate.boundHashOps("Cart_" + username).delete(id);
            // 如果此时购物车数量为空，则连购物车一起移除
            Long size = redisTemplate.boundHashOps("Cart_" + username).size();
            if (size == null || size <= 0) {
                redisTemplate.delete("Cart_" + username);
            }
            return;
        }

        // 查询商品的详情
        Result<Sku> skuResult = skuFeign.findById(id.toString());
        Sku sku = skuResult.getData();
        Result<Spu> spuResult = spuFeign.findById(Long.parseLong(sku.getSpuId()));
        Spu spu = spuResult.getData();

        // 将加入购物车的商品信息封装成OrderItem
        OrderItem orderItem = createOrderItem(num, id, sku, spu);

        redisTemplate.boundHashOps("Cart_" + username).put(id, orderItem);
    }

    /**
     * 将加入购物车的商品信息封装成OrderItem
     */
    public OrderItem createOrderItem(Integer num, Long id, Sku sku, Spu spu) {
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(Long.parseLong(spu.getId()));
        orderItem.setSkuId(id);
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num * orderItem.getPrice());
        orderItem.setImage(sku.getImage());
        return orderItem;
    }
}
