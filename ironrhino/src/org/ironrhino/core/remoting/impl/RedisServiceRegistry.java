package org.ironrhino.core.remoting.impl;

import static org.ironrhino.core.metadata.Profiles.CLOUD;
import static org.ironrhino.core.metadata.Profiles.DUAL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.event.EventPublisher;
import org.ironrhino.core.metadata.Scope;
import org.ironrhino.core.remoting.ExportServicesEvent;
import org.ironrhino.core.util.AppInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Singleton
@Named("serviceRegistry")
@Profile({ DUAL, CLOUD })
public class RedisServiceRegistry extends AbstractServiceRegistry {

	private static final String NAMESPACE = "remoting:";

	@Inject
	@Named("stringRedisTemplate")
	private RedisTemplate<String, String> stringRedisTemplate;

	@Inject
	private EventPublisher eventPublisher;

	@Autowired(required = false)
	private ExecutorService executorService;

	private Map<String, String> discoveredServices = new HashMap<String, String>();

	private boolean ready;

	@Override
	protected void onReady() {
		Set<String> services = getExportServices().keySet();
		if (!services.isEmpty()) {
			ExportServicesEvent event = new ExportServicesEvent(
					new ArrayList<String>(services));
			eventPublisher.publish(event, Scope.GLOBAL);
		}
		writeDiscoveredServices();
		ready = true;
	}

	@Override
	public String discover(String serviceName) {
		List<String> hosts = getImportServices().get(serviceName);
		if (hosts == null || hosts.size() == 0)
			lookup(serviceName);
		return super.discover(serviceName);
	}

	@Override
	protected void lookup(String serviceName) {
		List<String> list = stringRedisTemplate.opsForList().range(
				NAMESPACE + serviceName, 0, -1);
		if (list != null && list.size() > 0)
			importServices.put(serviceName, list);
	}

	@Override
	protected void doRegister(String serviceName, String host) {
		stringRedisTemplate.opsForList().rightPush(NAMESPACE + serviceName,
				host);
	}

	@Override
	protected void doUnregister(String serviceName, String host) {
		stringRedisTemplate.opsForList().remove(NAMESPACE + serviceName, 0,
				host);
	}

	@Override
	protected void onDiscover(String serviceName, String host) {
		super.onDiscover(serviceName, host);
		discoveredServices.put(serviceName, host);
		if (ready)
			writeDiscoveredServices();
	}

	protected void writeDiscoveredServices() {
		if (discoveredServices.size() == 0)
			return;
		String s = AppInfo.getHostAddress();
		if (AppInfo.getHttpPort() > 0)
			s += ":" + AppInfo.getHttpPort();
		final String host = s;
		Runnable task = new Runnable() {
			@Override
			public void run() {
				stringRedisTemplate.opsForHash().putAll(NAMESPACE + host,
						discoveredServices);
			}
		};
		if (executorService != null)
			executorService.execute(task);
		else
			task.run();
	}

}
