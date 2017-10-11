package com.redis.redisAction;

import java.util.ArrayList;
import java.util.Set;

import redis.clients.jedis.Jedis;

/**
 * 
 * @author jing.ming
 *
 */
public class CleanFullSessionsThread implements Runnable{
	
	private Jedis conn ;
	private boolean quit ;
	private int limit ;
	
	public CleanFullSessionsThread(int limit){
		this.conn = new Jedis("localhost") ;
		this.conn.select(15) ;
		this.limit = limit ;
	}
	
	public void quit(){
		quit = true ;
	}

	public void run() {
		while(!quit){
			long size = conn.zcard("recent:") ;
			if(size<=limit){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					e.printStackTrace();
				}
				continue ;
			}
			//只保留limit条记录
			long endIndex = Math.min(100, size-limit) ;
			Set<String> sessionSet = conn.zrange("recent:", 0, endIndex-1) ;
			String[] sessions = sessionSet.toArray(new String[sessionSet.size()]) ;
			ArrayList<String> sessionKeys = new ArrayList<String>() ;
			for(String sess:sessions){
				sessionKeys.add("viewed:"+sess) ;
				sessionKeys.add("cart:"+sess) ;
			}
			
			conn.del(sessionKeys.toArray(new String[sessionKeys.size()]));
            conn.hdel("login:", sessions);
            conn.zrem("recent:", sessions);
		} 
		
	}

}
