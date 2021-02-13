package com.roncoo.eshop.cache.spring;


import org.springframework.context.ApplicationContext;

/**
 * spring的应用上下文
 * 用于与spring的整合
 */
public class SpringContext {

    private static ApplicationContext ac;

    public static ApplicationContext getApplicationContext() {
        return ac;
    }

    public static void setApplicationContext(ApplicationContext ac) {
        SpringContext.ac = ac;
    }
}
