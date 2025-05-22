package com.secondspin.common.utils;

public class RedisConstants {
    public static final String APP_NAME = "secondspin";
    public static final String USER_SERVICE_PREFIX = ":users:";
    public static final String PRODUCT_SERVICE_PREFIX = ":products:";

    //  Null value
    public static final String NULL_VALUE = "NULL";
    public static final Long NULL_CACHE_TTL = 5L;

    //  Lock
    public static final String LOCK_KEY = "lock:";
    public static final Long LOCK_TTL = 30L;

    //  User-service
    public static final String VERIFY_CODE_KEY = APP_NAME + USER_SERVICE_PREFIX + "verify:";
    public static final Long VERIFY_CODE_TTL = 10L;

    //  Product-service
    public static final String CATEGORIES_KEY = APP_NAME + PRODUCT_SERVICE_PREFIX + "categories:all";
    public static final String PRODUCT_INFO_KEY = APP_NAME + PRODUCT_SERVICE_PREFIX + "product:";
    public static final Long PRODUCT_INFO_TTL = 60L;
    public static final String PRODUCT_INFO_LOCK_KEY = PRODUCT_INFO_KEY + LOCK_KEY;
}
