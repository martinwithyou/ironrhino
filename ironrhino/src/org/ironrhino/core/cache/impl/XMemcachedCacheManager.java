package org.ironrhino.core.cache.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.metadata.PostPropertiesReset;
import org.springframework.util.Assert;

public class XMemcachedCacheManager implements CacheManager {

	private Log log = LogFactory.getLog(getClass());

	private String serverAddress;

	private MemcachedClient memcached;

	private boolean rebuild; // reserve last set

	public void setServerAddress(String val) {
		if (val != null && serverAddress != null && !val.equals(serverAddress))
			rebuild = true;
		serverAddress = val;
	}

	@PostConstruct
	public void afterPropertiesSet() throws IOException {
		memcached = build(serverAddress);
	}

	@PostPropertiesReset
	public void rebuild() throws IOException {
		if (rebuild) {
			rebuild = false;
			MemcachedClient temp = memcached;
			memcached = build(serverAddress);
			temp.shutdown();
		}
	}

	@PreDestroy
	public void destroy() {
		if (memcached != null)
			try {
				memcached.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private MemcachedClient build(String serverAddress) throws IOException {
		Assert.hasLength(serverAddress);
		MemcachedClientBuilder builder = new XMemcachedClientBuilder(
				AddrUtil.getAddresses(serverAddress));
		builder.setSessionLocator(new KetamaMemcachedSessionLocator());
		builder.setCommandFactory(new BinaryCommandFactory());
		return builder.build();
	}

	public void put(String key, Object value, int timeToLive, String namespace) {
		put(key, value, -1, timeToLive, namespace);
	}

	public void put(String key, Object value, int timeToIdle, int timeToLive,
			String namespace) {
		if (key == null || value == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		try {
			memcached.set(generateKey(key, namespace), timeToLive, value);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Object get(String key, String namespace) {
		if (key == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		try {
			return memcached.get(generateKey(key, namespace));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public void delete(String key, String namespace) {
		if (key == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		try {
			memcached.delete(generateKey(key, namespace));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void mput(Map<String, Object> map, int timeToIdle, int timeToLive,
			String namespace) {
		if (map == null)
			return;
		for (Map.Entry<String, Object> entry : map.entrySet())
			put(entry.getKey(), entry.getValue(), timeToIdle, timeToLive,
					namespace);
	}

	public Map<String, Object> mget(Collection<String> keys, String namespace) {
		if (keys == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		List<String> list = new ArrayList<String>();
		for (String key : keys)
			list.add(generateKey(key, namespace));
		try {
			return memcached.get(list);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public void mdelete(Collection<String> keys, String namespace) {
		if (keys == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		for (String key : keys)
			delete(key, namespace);
	}

	private String generateKey(String key, String namespace) {
		StringBuilder sb = new StringBuilder();
		sb.append(namespace);
		sb.append(':');
		sb.append(key);
		return sb.toString();
	}

	public boolean supportsTimeToIdle() {
		return false;
	}

}
