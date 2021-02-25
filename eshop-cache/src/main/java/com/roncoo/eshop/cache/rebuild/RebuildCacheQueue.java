package com.roncoo.eshop.cache.rebuild;

import com.roncoo.eshop.cache.model.ProductInfo;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * 重建缓存的内存队列
 */
public class RebuildCacheQueue {

    //定义一个内存队列
    private ArrayBlockingQueue<ProductInfo> arrayBlockingQueue = new ArrayBlockingQueue<ProductInfo>(1000);

    public void putProductInfo(ProductInfo productInfo){
        arrayBlockingQueue.add(productInfo);
    }

    public ProductInfo getProductInfo(){
        try {
         return  arrayBlockingQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 创建一个内部单例类
     */
    private static class Singleton{
        private static RebuildCacheQueue singleton;
        static {
            singleton = new RebuildCacheQueue();
        }

        public static RebuildCacheQueue getSingleton(){
            return singleton;
        }
    }

    public static RebuildCacheQueue getInstance(){
        return Singleton.getSingleton();
    }

    public static void init(){
        getInstance();
    }
}
