package com.changgou.seckill.controller;

import com.changgou.seckill.service.SeckillOrderService;
import entity.Result;
import entity.SeckillStatus;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/****
 * @Author:shenkunlin
 * @Description:
 * @Date 2019/6/14 0:18
 *****/

@RestController
@RequestMapping("/seckillOrder")
@CrossOrigin
public class SeckillOrderController {

    @Autowired
    private SeckillOrderService seckillOrderService;

    /**
     * 抢单状态查询
     */
    @GetMapping(value = "/query")
    public Result queryStatus() {
        String username = "szitheima";
        SeckillStatus seckillStatus = seckillOrderService.queryStatus(username);
        if (seckillStatus != null) {
            return new Result(true, StatusCode.OK, "查询状态成功！", seckillStatus);
        }
        return new Result(false, StatusCode.NOTFOUNDERROR, "抢单失败！");
    }

    /**
     * 添加秒杀订单
     */
    @RequestMapping(value = "/add")
    public Result add(String time, Long id, String username) {
        seckillOrderService.add(time, id, username);
        return new Result(true, StatusCode.OK, "正在排队...");
    }
}
