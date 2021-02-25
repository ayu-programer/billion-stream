package com.roncoo.eshop.cache.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.roncoo.eshop.cache.model.ProductInfo;
import com.roncoo.eshop.cache.model.ShopInfo;
import com.roncoo.eshop.cache.service.CacheService;
import com.roncoo.eshop.cache.spring.SpringContext;
import com.roncoo.eshop.cache.zookeeper.ZooKeeperSession;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.utils.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class KafkaMessageProcessor implements Runnable {

    private KafkaStream  kafkaStream;

    private CacheService cacheService;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

        //todo 新增分布式锁功能 即将数据保存到本地缓存后
        // 4.1先获取zookeeper分布式锁，然后才能更新redis，同时要比较时间版本
        ZooKeeperSession zooKeeperSession = ZooKeeperSession.getZookeeperSession();
        zooKeeperSession.acquireDistributeLock(shopId);

        ShopInfo shopInfoRedisCache = cacheService.getShopInfoByRedisCache(shopId);
        if (shopInfoRedisCache != null){
            try {
                //获取 redis中已存在的shopinfo 中的时间戳
                Date redis_modifiedTime = simpleDateFormat.parse(shopInfoRedisCache.getModifiedTime());

                //获取当前线程中的shopinfo时间戳
                Date current_modifiedTime = simpleDateFormat.parse(shopInfo.getModifiedTime());
                // 如果获取zk分布式锁的线程的时间戳晚于已存在数据的时间戳，那么直接退出
                if (current_modifiedTime.before(redis_modifiedTime)){
                    System.out.println("current date"+shopInfo.getModifiedTime()+"is before existed date[" +
                            shopInfoRedisCache.getModifiedTime()+"]");
                    return;
                }
                System.out.println("current date"+shopInfo.getModifiedTime()+"is after existed date[" +
                        shopInfoRedisCache.getModifiedTime()+"]");
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("existed shop info is null.........");
        }
        System.out.println("===================获取刚保存到本地缓存的店铺信息：" + cacheService.getShopInfoByLocalCache(shopId));
        cacheService.saveShopInfo2RedisCache(shopInfo);
        //释放分布式锁
        zooKeeperSession.releaseDistributedLock(shopId);
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
