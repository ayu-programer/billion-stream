package com.roncoo.eshop.cache.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.roncoo.eshop.cache.model.ProductInfo;
import com.roncoo.eshop.cache.model.ShopInfo;
import com.roncoo.eshop.cache.service.CacheService;
import com.roncoo.eshop.cache.spring.SpringContext;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.utils.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


public class KafkaMessageProcessor implements Runnable {

    private KafkaStream  kafkaStream;

    private CacheService cacheService;

    public KafkaMessageProcessor(KafkaStream kafkaStream) {
        this.kafkaStream = kafkaStream;
        this.cacheService = (CacheService) SpringContext.getApplicationContext().getBean("cacheService");
    }

    @Override
    public void run() {
        ConsumerIterator<byte[],byte[]> iterator = kafkaStream.iterator();
        while (iterator.hasNext()){
            String message = new String(iterator.next().message());

            //1 首先将message转成json对象
            JSONObject jsonObject = JSONObject.parseObject(message);

            String serviceId = jsonObject.getString("serviceId");

            //2 从json对象中提取出消息对应的服务标志
            if ("productInfoService".equals(serviceId)){
                processProductInfoChangeMessage(jsonObject);
            }else if ("shopInfoService".equals(serviceId)){
                processShopInfoChangeMessage(jsonObject);
            }

        }
    }

    private void processShopInfoChangeMessage(JSONObject jsonObject) {
       //1 获取shopid
        Long shopId = jsonObject.getLong("shopId");

        //2 模拟调用服务，查询数据库
        //**服务  getShopInfo?productId=1,传递过去，得到返回值
        String shopInfoJSON = "{\"shopId\": 1, \"name\": \"小王的手机店\", \"level\": 5, \"goodCommentRate\":0.99}";

        //3 转成json对象
        ShopInfo shopInfo = JSONObject.parseObject(shopInfoJSON, ShopInfo.class);

        //4 保存数据到本地缓存及redis中
        cacheService.saveShopInfo2LocalCache(shopInfo);
        System.out.println("===================获取刚保存到本地缓存的店铺信息：" + cacheService.getShopInfoByLocalCache(shopId));
        cacheService.saveShopInfo2RedisCache(shopInfo);


    }


    private void processProductInfoChangeMessage(JSONObject jsonObject) {
        //1 获取productId
        Long productId = jsonObject.getLong("productId");

        //2 模拟调用服务，查询数据库
        //**服务  getProductInfo?productId=1,传递过去，得到返回值
        String productInfoJSON = "{\"productId\": 1, \"name\": \"iphone7手机\", \"price\": 5599, \"pictureList\":\"a.jpg,b.jpg\", \"specification\": \"iphone7的规格\", \"service\": \"iphone7的售后服务\", \"color\": \"红色,白色,黑色\", \"size\": \"5.5\", \"shopId\": 1}";
       //3 转成productinfo对象
        ProductInfo productInfo= JSONObject.parseObject(productInfoJSON, ProductInfo.class);

        //4 保存到本地缓存及redis中
        cacheService.saveProductInfo2LocalCache(productInfo);
        System.out.println("===================获取刚保存到本地缓存的商品信息：" + cacheService.getProductInfoByLocalCache(productId));
        cacheService.saveProductInfo2RedisCache(productInfo);
    }


}
