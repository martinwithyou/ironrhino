package org.ironrhino.core.cache;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public interface CacheManager {

	public static final String DEFAULT_TIME_TO_LIVE = "3600";

	public static final String DEFAULT_TIME_TO_IDLE = "-1";

	public static final String DEFAULT_NAMESPACE = "default";

	public void put(Serializable key, Serializable value, int timeToIdle,
			int timeToLive, String namespace);

	public Serializable get(Serializable key, String namespace);

	public void delete(Serializable key, String namespace);

	public void mput(Map<Serializable, Serializable> map, int timeToIdle,
			int timeToLive, String namespace);

	public Map<Serializable, Serializable> mget(Collection<Serializable> keys,
			String namespace);

	public void mdelete(Collection<Serializable> keys, String namespace);

}
