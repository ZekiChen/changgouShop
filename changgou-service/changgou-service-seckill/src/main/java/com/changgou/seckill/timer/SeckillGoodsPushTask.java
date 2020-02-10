package com.changgou.seckill.timer;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @Description: 定时将秒杀商品存入Redis缓存中
 * @Author:      Zeki
 * @CreateDate:  2020/1/18 0018 上午 10:42
 */
@Component
public class SeckillGoodsPushTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 定时操作
     */
    @Scheduled(cron = "0/5 * * * * ?")
    public void loadGoodsPushRedis() {
        /**
         * 1、查询符合当前参与秒杀的时间菜单
         * 2、审核状态：必须是审核通过
         * 3、秒杀商品库存>0
         * 4、时间菜单的开始时间<=秒杀开始时间 && 秒杀结束时间<时间菜单的开始时间+2小时
         */
        List<Date> dateMenus = DateUtil.getDateMenus();  // 得到时间菜单（5个区间）
        for (Date dateMenu : dateMenus) {  // 循环查询每个时间区间的秒杀商品
            String timespace = "SeckillGoods_" + DateUtil.data2str(dateMenu, "yyyyMMddHH");  // 当前时间区间为菜单的第1个区间
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("status", "1");
            criteria.andGreaterThan("stockCount", 0);
            criteria.andGreaterThanOrEqualTo("startTime", dateMenu);
            criteria.andLessThan("endTime", DateUtil.addDateHour(dateMenu, 2));

            // 排除当前已经存入到了Redis中的秒杀商品
            Set keys = redisTemplate.boundHashOps(timespace).keys();
            if (keys != null && keys.size() > 0) {
                criteria.andNotIn("id", keys);
            }

            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);  // 条件查询
            for (SeckillGoods seckillGood : seckillGoods) {  // 将每个时间区间内的秒杀商品存入Redis中
                System.out.println("商品id：" + seckillGood.getId() + "---存入到了Redis中---" + timespace);
                redisTemplate.boundHashOps(timespace).put(seckillGood.getId(), seckillGood);
                // 给每个商品做个库存数量统计队列，用于解决库存超卖问题
                redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillGood.getId())
                        .leftPushAll(putAllIds(seckillGood.getStockCount(), seckillGood.getId()));
            }
        }
    }

    /**
     * 获取每个商品的id集合
     */
    public Long[] putAllIds(Integer num, Long id) {
        Long[] ids = new Long[num];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = id;
        }
        return ids;
    }
}
