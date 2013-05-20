package org.ironrhino.core.event;

import javax.inject.Inject;

import org.ironrhino.core.metadata.Scope;
import org.ironrhino.core.redis.RedisTopic;
import org.springframework.context.ApplicationEvent;

public class RedisApplicationEventTopic extends RedisTopic<ApplicationEvent>
		implements ApplicationEventTopic {

	@Inject
	private EventPublisher eventPublisher;

	@Override
	public void subscribe(ApplicationEvent event) {
		eventPublisher.publish(event, Scope.LOCAL);
	}

}
