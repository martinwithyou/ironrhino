package org.ironrhino.core.event;

import javax.inject.Inject;

import org.ironrhino.core.redis.RedisTopic;
import org.springframework.context.ApplicationEvent;

public class ApplicationEventRedisTopic extends RedisTopic<ApplicationEvent> {

	@Inject
	private EventPublisher eventPublisher;

	@Override
	public void subscribe(ApplicationEvent event) {
		eventPublisher.publish(event, false);
	}

}
