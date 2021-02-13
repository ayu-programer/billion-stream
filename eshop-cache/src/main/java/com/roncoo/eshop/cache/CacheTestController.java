package com.roncoo.eshop.cache;

import com.roncoo.eshop.cache.model.ProductInfo;
import com.roncoo.eshop.cache.service.CacheService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class CacheTestController {

    @Resource
    private CacheService cacheService;


    @RequestMapping("/testPutCache")
    public String testPutCache(ProductInfo productInfo) {
        System.out.println(productInfo.getProductId() + ":" + productInfo.getName());
        cacheService.saveProductInfo2LocalCache(productInfo);
        return"success";
    }

    @RequestMapping("/testGetCache")
    public ProductInfo testGetCache(Long productId) {
        ProductInfo productInfo = cacheService.getProductInfoByLocalCache(productId);
        System.out.println(productInfo.getProductId() + ":" + productInfo.getName());
        return productInfo;
    }
}
