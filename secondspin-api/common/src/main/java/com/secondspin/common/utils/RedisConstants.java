package com.secondspin.common.utils;

public class RedisConstants {
    public static final String APP_NAME = "secondspin";
    public static final String USER_SERVICE_PREFIX = ":users:";
    public static final String PRODUCT_SERVICE_PREFIX = ":products:";

    //  User-service
    public static final String VERIFY_CODE_KEY = APP_NAME + USER_SERVICE_PREFIX + "verify:";
    public static final Long VERIFY_CODE_TTL = 10L;

    //  Product-service
    public static final String CATEGORIES_KEY = APP_NAME + PRODUCT_SERVICE_PREFIX + "categories:all";
    public static final String PRODUCT_INFO_KEY = APP_NAME + PRODUCT_SERVICE_PREFIX + "product:";
}
