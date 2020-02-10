package com.changgou.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.changgou.item.feign.PageFeign;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

/**
 * @Description: 实现MySQL数据监听
 * @Author:      Zeki
 * @CreateDate:  2020/1/6 0006 下午 4:43
 */
@CanalEventListener
public class CanalDataEventListener {

    @Autowired
    private PageFeign pageFeign;

    @Autowired
    private ContentFeign contentFeign;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 自定义数据库的操作来监听
    @ListenPoint(
            destination = "example",
            schema = {"changgou_content"},
            table = {"tb_content", "tb_content_category"},
            eventType = {
                    CanalEntry.EventType.INSERT,
                    CanalEntry.EventType.DELETE,
                    CanalEntry.EventType.UPDATE
            }
    )
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        String categoryId = getColumnValue(eventType, rowData);
        Result<List<Content>> categoryResult = contentFeign.findByCategory(Long.valueOf(categoryId));
        List<Content> contentList = categoryResult.getData();
        stringRedisTemplate.boundValueOps("content_" + categoryId).set(JSON.toJSONString(contentList));
    }

    private String getColumnValue(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        String categoryId = "";
        if (eventType == CanalEntry.EventType.DELETE) {
            for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
                if (column.getName().equalsIgnoreCase("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        } else {
            for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                if (column.getName().equalsIgnoreCase("category_id")) {
                    categoryId = column.getValue();
                    return categoryId;
                }
            }
        }
        return categoryId;
    }

    // 监听类中,监听商品数据库的tb_spu的数据变化,当数据变化的时候生成静态页或者删除静态页
    @ListenPoint(destination = "example",
            schema = "changgou_goods",
            table = {"tb_spu"},
            eventType = {CanalEntry.EventType.UPDATE, CanalEntry.EventType.INSERT, CanalEntry.EventType.DELETE})
    public void onEventCustomSpu(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {

        //判断操作类型
        if (eventType == CanalEntry.EventType.DELETE) {
            String spuId = "";
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();//spuid
                    break;
                }
            }
            //todo 删除静态页

        }else{
            //新增 或者 更新
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            String spuId = "";
            for (CanalEntry.Column column : afterColumnsList) {
                if (column.getName().equals("id")) {
                    spuId = column.getValue();
                    break;
                }
            }
            //更新 生成静态页
            pageFeign.createHtml(Long.valueOf(spuId));
        }
    }

//    /**
//     * 新增监听
//     * @param eventType：当前操作的类型，如新增数据
//     * @param rowData：发送变更的一行数据
//     * rowData.getAfterColumnsList() : 新增、修改
//     * rowData.getBeforeColumnsList() : 删除、修改
//     */
//    @InsertListenPoint
//    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
//        for (CanalEntry.Column column : rowData.getAfterColumnsList()) { // 只有增加后的数据
//            System.out.println("列名：" + column.getName() + "----变更的数据：" + column.getValue());
//        }
//    }
//
//    /**
//     * 修改监听
//     */
//    @UpdateListenPoint
//    public void onEventUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
//        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) { // 只有增加后的数据
//            System.out.println("修改前列名：" + column.getName() + "----变更的数据：" + column.getValue());
//        }
//
//        for (CanalEntry.Column column : rowData.getAfterColumnsList()) { // 只有增加后的数据
//            System.out.println("修改后列名：" + column.getName() + "----变更的数据：" + column.getValue());
//        }
//    }
//
//    /**
//     * 删除监听
//     */
//    @DeleteListenPoint
//    public void onEventDelete(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
//        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) { // 只有增加后的数据
//            System.out.println("删除前列名：" + column.getName() + "----变更的数据：" + column.getValue());
//        }
//    }
//
//    /**
//     * 自定义监听
//     */
//    @ListenPoint(
//            eventType = {CanalEntry.EventType.DELETE, CanalEntry.EventType.UPDATE},  // 监听类型
//            schema = {"changgou_content"},  // 指定监听数据库
//            table = {"tb_content"},  // 指定监听的表
//            destination = "example"  // 指定实例的地址
//    )
//    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
//        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) { // 只有增加后的数据
//            System.out.println("自定义操作前列名：" + column.getName() + "----变更的数据：" + column.getValue());
//        }
//
//        for (CanalEntry.Column column : rowData.getAfterColumnsList()) { // 只有增加后的数据
//            System.out.println("自定义操作后列名：" + column.getName() + "----变更的数据：" + column.getValue());
//        }
//    }
}
