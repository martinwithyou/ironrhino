package org.ironrhino.core.remoting.impl;

import static org.ironrhino.core.metadata.Profiles.CLOUD;
import static org.ironrhino.core.metadata.Profiles.DUAL;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;

@Singleton
@Named("serviceRegistry")
@Profile({ DUAL, CLOUD })
public class RedisServiceRegistry extends AbstractServiceRegistry {

	@Inject
	@Named("stringRedisTemplate")
	private RedisTemplate<String, String> stringRedisTemplate;

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
