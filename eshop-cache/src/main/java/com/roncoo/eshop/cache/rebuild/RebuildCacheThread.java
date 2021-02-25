package com.roncoo.eshop.cache.rebuild;

import com.roncoo.eshop.cache.model.ProductInfo;
import com.roncoo.eshop.cache.service.CacheService;
import com.roncoo.eshop.cache.spring.SpringContext;
import com.roncoo.eshop.cache.zookeeper.ZooKeeperSession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 创建重建缓存线程
 */
public class RebuildCacheThread implements Runnable {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public void run() {
        //1 先获取重建缓存队列
        RebuildCacheQueue instance = RebuildCacheQueue.getInstance();

        //2 创建分布式锁
        ZooKeeperSession zookeeperSession = ZooKeeperSession.getZookeeperSession();

        //3 从spring容器获取redisService数据
        CacheService cacheService =(CacheService) SpringContext.getApplicationContext().getBean("cacheService");

        while (true){
            //4 从缓存队列中获取productinfo数据
            ProductInfo productInfo = instance.getProductInfo();
            zookeeperSession.acquireDistributeLock(productInfo.getProductId());

            //5尝试从reids中获取数据
            ProductInfo productInfoByRedisCache = cacheService.getProductInfoByRedisCache(productInfo.getProductId());
            if (productInfoByRedisCache != null){
                //比较时间版本
                try {
                    //获取 redis中已存在的shopinfo 中的时间戳
                    Date redis_modifiedTime = simpleDateFormat.parse(productInfoByRedisCache.getModifiedTime());

                    //获取当前线程中的shopinfo时间戳
                    Date current_modifiedTime = simpleDateFormat.parse(productInfo.getModifiedTime());
                    // 如果获取zk分布式锁的线程的时间戳晚于已存在数据的时间戳，那么直接退出
                    if (current_modifiedTime.before(redis_modifiedTime)){
                        System.out.println("current date"+productInfo.getModifiedTime()+"is before existed date[" +
                                productInfoByRedisCache.getModifiedTime()+"]");
                        return;
                    }
                    System.out.println("current date"+productInfo.getModifiedTime()+"is after existed date[" +
                            productInfoByRedisCache.getModifiedTime()+"]");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            cacheService.saveProductInfo2RedisCache(productInfo);
            zookeeperSession.releaseDistributedLock(productInfo.getProductId());
        }
    }

}
