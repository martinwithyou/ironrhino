package org.ironrhino.core.cache;

import java.util.Collection;
import java.util.Map;

public interface CacheManager {

	public static final String DEFAULT_TIME_TO_LIVE = "3600";

	public static final String DEFAULT_TIME_TO_IDLE = "-1";

	public static final String DEFAULT_NAMESPACE = "default";

	public void put(String key, Object value, int timeToLive, String namespace);

	public void put(String key, Object value, int timeToIdle, int timeToLive,
			String namespace);

	public Object get(String key, String namespace);

	public Object get(String key, String namespace, int timeToLive);

	public void delete(String key, String namespace);

	public void mput(Map<String, Object> map, int timeToLive, String namespace);

	public void mput(Map<String, Object> map, int timeToIdle, int timeToLive,
			String namespace);

	public Map<String, Object> mget(Collection<String> keys, String namespace);

	public void mdelete(Collection<String> keys, String namespace);

	public boolean containsKey(String key, String namespace);

	public boolean putIfAbsent(String key, Object value, int timeToLive,
			String namespace);

	public boolean supportsTimeToIdle();

	public boolean supportsUpdateTimeToLive();

}
