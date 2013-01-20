package org.ironrhino.core.remoting;

import java.util.Map;

public interface ServiceRegistry {

	public Map<String, Object> getExportServices();

	public void register(String serviceName);

	public void unregister(String serviceName);

	public String discover(String serviceName);

	public void evict(String host);

}