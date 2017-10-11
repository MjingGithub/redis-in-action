package com.redis.redisAction;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;

/**
 * 
 * @author jing.ming
 *
 */
public class TokensLogin {

	public String checkToken(String token, Jedis conn) {
		return conn.hget("login:", token);
	}

	public void updateToken(Jedis conn, String token, String user, String item) {
		long timestamp = System.currentTimeMillis() / 1000;
		// 保存token到登录用户的映射map
		conn.hset("login:", token, user);
		// 记录token最后被看到的时间
		conn.zadd("recent:", timestamp, token);
		if (item != null) {
			// 记录用户浏览item的时间
			conn.zadd("viewed:" + token, timestamp, item);
			// 移除旧的浏览记录,保留最近25个浏览记录
			conn.zremrangeByRank("viewed:" + token, 0, -26);
			// 对存在的item -1
			conn.zincrby("viewed:", -1, item);
		}
	}

	public void addToCart(String item, int count, Jedis conn, String session) {
		if (count <= 0) {
			conn.hdel("cart:" + session, item);
		} else {
			conn.hset("cart:" + session, item, String.valueOf(count));
		}
	}

	public interface Callback {
		public String call(String request);
	}

	/**
	 * 
	 * @param callback
	 * @param conn
	 * @param request
	 * @return
	 */
	public String cacheRequest(Callback callback, Jedis conn, String request) {
		if (!canCache(conn, request)) {
			return callback != null ? callback.call(request) : null;
		}

		String pageKey = "cache:" + hashRequest(request);
		String content = conn.get(pageKey);

		if (content == null && callback != null) {
			content = callback.call(request);
			conn.setex(pageKey, 300, content);
		}

		return content;

	}

	public String hashRequest(String request) {
		return String.valueOf(request.hashCode());
	}

	public String extractItemId(Map<String, String> params) {
		return params.get("item");
	}

	public boolean isDynamic(Map<String, String> params) {
		return params.containsKey("_");
	}

	/**
	 * 
	 * @param conn
	 * @param request
	 * @return
	 */
	public boolean canCache(Jedis conn, String request) {
		try {
			URL url = new URL(request);
			HashMap<String, String> params = new HashMap<String, String>();
			if (url.getQuery() != null) {
				for (String param : url.getQuery().split("&")) {
					String[] pair = param.split("=", 2);
					params.put(pair[0], pair.length == 2 ? pair[1] : null);
				}
			}
			String itemId = extractItemId(params);
			if (itemId == null || isDynamic(params)) {
				return false;
			}
			Long rank = conn.zrank("viewed:", itemId);
			return rank != null && rank < 10000;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void scheduleRowCache(Jedis conn,String rowId,int delay){
		conn.zadd("delay:", delay, rowId) ;
		conn.zadd("schedule:", System.currentTimeMillis()/1000, rowId) ;
	}

}
