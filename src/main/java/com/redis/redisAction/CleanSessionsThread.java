package com.redis.redisAction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import redis.clients.jedis.Jedis;

/**
 * TokensLogin的辅助类
 * 
 * @author jing.ming
 *
 */
public class CleanSessionsThread implements Runnable {

	private Jedis conn;
	private int limit;
	private boolean quit;

	public CleanSessionsThread(int limit) {
		this.conn = new Jedis("localhost");
		this.conn.select(15);
		this.limit = limit;
	}

	public void quit() {
		quit = true;
	}

	public void run() {
		while(!quit){
			long size = conn.zcard("recent:") ;
			if(size<=limit){
				try {
					Thread.sleep(1000) ;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue ;
			}
			
			long endIndex = Math.min(size-limit, 100) ;
			Set<String> tokenSet = conn.zrange("recent:", 0, endIndex-1) ;
			String[] tokens = tokenSet.toArray(new String[tokenSet.size()]) ;
			ArrayList<String> sessionKeys = new ArrayList<String>();
			Iterator<String> it = tokenSet.iterator() ;
			while(it.hasNext()){
				sessionKeys.add(it.next()) ;
			}
			conn.del(sessionKeys.toArray(new String[sessionKeys.size()])) ;
			conn.hdel("login:", tokens) ;
			conn.zrem("recent:", tokens);
		}

	}

}
