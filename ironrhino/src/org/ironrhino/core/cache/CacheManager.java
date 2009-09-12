package org.ironrhino.core.cache;

import java.io.Serializable;

public interface CacheManager {

	public static final String DEFAULT_TIME_TO_LIVE = "3600";

	public static final String DEFAULT_TIME_TO_IDLE = "-1";

	public static final String DEFAULT_NAMESPACE = "default";

	public void put(Serializable key, Serializable value, int timeToIdle,
			int timeToLive, String namespace);

	public Serializable get(Serializable key, String namespace);

	public void remove(Serializable key, String namespace);

}
