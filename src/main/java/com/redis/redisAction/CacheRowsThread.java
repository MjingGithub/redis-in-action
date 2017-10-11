package com.redis.redisAction;

import java.util.Set;

import com.google.gson.Gson;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

/**
 * 
 * @author jing.ming
 *
 */
public class CacheRowsThread implements Runnable{
	
	private Jedis conn ;
	private boolean quit ;

	public CacheRowsThread(){
		this.conn = new Jedis("localhost"); 
		this.conn.select(15) ;
	}
	
	public void quit() {
        quit = true;
	}

	public void run() {
		 Gson gson = new Gson();
		 while(!quit){
			 Set<Tuple> range = conn.zrangeWithScores("schedule:", 0, 0) ;
			 Tuple next = range.size() > 0 ? range.iterator().next() : null;
             long now = System.currentTimeMillis() / 1000;
             if (next == null || next.getScore() > now){
                 try {
                     Thread.sleep(50);
                 }catch(InterruptedException ie){
                     Thread.currentThread().interrupt();
                 }
                 continue;
             }
             String rowId = next.getElement();
             double delay = conn.zscore("delay:", rowId);
             if (delay <= 0) {
                 conn.zrem("delay:", rowId);
                 conn.zrem("schedule:", rowId);
                 conn.del("inv:" + rowId);
                 continue;
             }
             Inventory row = Inventory.get(rowId);
             conn.zadd("schedule:", now + delay, rowId);
             conn.set("inv:" + rowId, gson.toJson(row));

		 }
		
	}

}
