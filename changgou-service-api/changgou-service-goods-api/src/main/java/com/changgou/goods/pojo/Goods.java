package com.changgou.goods.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * @Description: 商品信息组合对象 = Spu + List<Sku>
 * @Author:      Zeki
 * @CreateDate:  2020/1/4 0004 下午 7:41
 */
public class Goods implements Serializable {

    private Spu spu;

    private List<Sku> skuList;

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
