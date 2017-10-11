package com.redis.redisAction;

import redis.clients.jedis.Jedis;

/**
 * 
 * @author jing.ming
 *
 */
public class RedisJava{
    public static void main( String[] args ){
    	Jedis jedis = new Jedis("localhost") ;
    	System.out.println("Connection to server sucessfully");
        //查看服务是否运行
        System.out.println("Server is running: "+jedis.ping());
        jedis.set("today", "2017-06-05") ;
        System.out.println("today="+jedis.get("today")) ;
    }
}
