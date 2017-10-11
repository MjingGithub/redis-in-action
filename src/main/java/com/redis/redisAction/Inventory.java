package com.redis.redisAction;
/**
 * 
 * @author jing.ming
 *
 */
public class Inventory {
	private String id;
    private String data;
    private long time;

    public Inventory (String id) {
        this.id = id;
        this.data = "data to cache...";
        this.time = System.currentTimeMillis() / 1000;
    }

    public static Inventory get(String id) {
        return new Inventory(id);
}
}
