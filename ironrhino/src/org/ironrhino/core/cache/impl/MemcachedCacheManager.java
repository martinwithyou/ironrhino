package org.ironrhino.core.cache.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.HashAlgorithm;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.metadata.PostPropertiesReset;
import org.springframework.util.Assert;

public class MemcachedCacheManager implements CacheManager {

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
			memcached.shutdown();
	}

	private MemcachedClient build(String serverAddress) throws IOException {
		Assert.hasLength(serverAddress);
		ConnectionFactory cf = new BinaryConnectionFactory(
				DefaultConnectionFactory.DEFAULT_OP_QUEUE_LEN,
				DefaultConnectionFactory.DEFAULT_READ_BUFFER_SIZE,
				HashAlgorithm.KETAMA_HASH);
		return new MemcachedClient(cf, AddrUtil.getAddresses(serverAddress));
	}

	public void put(String key, Object value, int timeToIdle, int timeToLive,
			String namespace) {
		if (key == null || value == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		// TODO timeToIdle doesn't supported
		memcached.set(generateKey(key, namespace), timeToLive, value);
	}

	public Serializable get(String key, String namespace) {
		if (key == null)
			return null;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		return (Serializable) memcached.get(generateKey(key, namespace));
	}

	public void delete(String key, String namespace) {
		if (key == null)
			return;
		if (StringUtils.isBlank(namespace))
			namespace = DEFAULT_NAMESPACE;
		memcached.delete(generateKey(key, namespace));
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
		return memcached.getBulk(list);
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

}
