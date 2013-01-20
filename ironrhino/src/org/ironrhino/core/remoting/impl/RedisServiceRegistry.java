package org.ironrhino.core.remoting.impl;

import static org.ironrhino.core.metadata.Profiles.CLOUD;
import static org.ironrhino.core.metadata.Profiles.DUAL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.event.EventPublisher;
import org.ironrhino.core.remoting.ExportServicesEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Singleton
@Named("serviceRegistry")
@Profile({ DUAL, CLOUD })
public class RedisServiceRegistry extends AbstractServiceRegistry {

	@Inject
	@Named("stringRedisTemplate")
	private RedisTemplate<String, String> stringRedisTemplate;

	@Inject
	private EventPublisher eventPublisher;

	protected void onReady() {
		Set<String> services = getExportServices().keySet();
		if (!services.isEmpty()) {
			ExportServicesEvent event = new ExportServicesEvent(
					new ArrayList<String>(services));
			eventPublisher.publish(event, true);
		}
	}

	public String discover(String serviceName) {
		List<String> hosts = getImportServices().get(serviceName);
		if (hosts == null || hosts.size() == 0)
			lookup(serviceName);
		return super.discover(serviceName);
	}

	protected void lookup(String serviceName) {
		List<String> list = stringRedisTemplate.opsForList().range(serviceName,
				0, -1);
		if (list != null && list.size() > 0)
			importServices.put(serviceName, list);
	}

	protected void doRegister(String serviceName, String host) {
		stringRedisTemplate.opsForList().rightPush(serviceName, host);
	}

	protected void doUnregister(String serviceName, String host) {
		stringRedisTemplate.opsForList().remove(serviceName, 0, host);
	}

}
