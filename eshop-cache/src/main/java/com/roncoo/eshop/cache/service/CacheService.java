package com.roncoo.eshop.cache.service;

import com.roncoo.eshop.cache.model.ProductInfo;
import com.roncoo.eshop.cache.model.ShopInfo;

import javax.print.attribute.standard.PrinterURI;

public interface CacheService {

    public ProductInfo findById(Long id);


    public ProductInfo saveProductInfo2LocalCache(ProductInfo productInfo);

    public void saveProductInfo2RedisCache(ProductInfo productInfo);

    public ShopInfo saveShopInfo2LocalCache(ShopInfo shopInfo);

    public void saveShopInfo2RedisCache(ShopInfo shopInfo);

    public ProductInfo getProductInfoByLocalCache(Long id);

    public ShopInfo getShopInfoByLocalCache(Long id);

    public ProductInfo getProductInfoByRedisCache(Long id);

    public ShopInfo getShopInfoByRedisCache(Long id);

}
