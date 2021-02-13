package com.roncoo.eshop.cache.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.roncoo.eshop.cache.model.ProductInfo;
import com.roncoo.eshop.cache.model.ShopInfo;
import com.roncoo.eshop.cache.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service("cacheService")
public class CacheServiceImpl implements CacheService {


    @Autowired
    private StringRedisTemplate redisTemplate;

    public static final String CACHE_NAME = "local";

    /**
     * 从本地缓存中获取商品信息
     * @param id
     * @return
     */
    @Cacheable(value = CACHE_NAME, key = "'key_'+#id")
    public ProductInfo findById(Long id) {
        return null;
    }

    /**
     * 查询本地缓存中的商品信息
     * @param id
     * @return
     */
    @Cacheable(value = CACHE_NAME,key="'key_'+#productId")
    public ProductInfo getProductInfoByLocalCache(Long productId){
        return null;
    }

    /**
     * 查询本地缓存中的店铺信息
     * @param id
     * @return
     */
    @Cacheable(value = CACHE_NAME,key="'key_'+#shopId")
    public ProductInfo getShopInfoByLocalCache(Long shopId){
        return null;
    }

    /**
     * 将商品信息保存到本地缓存
     * @param productInfo
     * @return
     */
    @CachePut(value = CACHE_NAME, key = "'key_'+#productInfo.getProductId()")
    public ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo) {
        return productInfo;
    }


    /**
     * 将商品信息保存到redis中
     * @param productInfo
     */
    public void saveProductInfo2RedisCache(ProductInfo productInfo){
        String key = "product_info_"+productInfo.getProductId();
        redisTemplate.opsForValue().set(key, JSONObject.toJSONString(productInfo));
    }

    /**
     * 将店铺信息保存到本地缓存中
     * @param shopInfo
     * @return
     */
    @CachePut(value = CACHE_NAME,key = "'key_'+#shopInfo.getShopId()")
    public ShopInfo saveShopInfo2LocalCache(ShopInfo shopInfo){
        return shopInfo;
    }

    /**
     * 将店铺信息保存到redis缓存中
     * @param shopInfo
     */
    public void saveShopInfo2RedisCache(ShopInfo shopInfo){
        String key = "shop_info_" + shopInfo.getShopId();
        redisTemplate.opsForValue().set(key,JSONObject.toJSONString(shopInfo));
    }
}
