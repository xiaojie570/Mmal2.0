package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by lenovo on 2018/12/10.
 */
public class RedisPool {
    private static RedisPool pool ;
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total","20"));
    private static Integer minIdel = Integer.parseInt(PropertiesUtil.getProperty("redis.min.idel","2"));
    private static Integer maxIdel = Integer.parseInt(PropertiesUtil.getProperty("redis.max.idel","10"));

    private static Boolean testOnBorrow = Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.on.borrow"));
    private static Boolean testOnReturn =  Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.on.retur"));

    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdel);
        config.setMinIdle(minIdel);

        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);

        config.setBlockWhenExhausted(true); // 当连接耗尽的时候，是否阻塞，false会抛出异常，true阻塞直到超时，默认为true。

    }

}
