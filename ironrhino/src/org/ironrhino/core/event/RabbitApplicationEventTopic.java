package org.ironrhino.core.event;

import javax.inject.Inject;

import org.ironrhino.core.metadata.Scope;
import org.ironrhino.core.rabbitmq.RabbitTopic;
import org.springframework.context.ApplicationEvent;

public class RabbitApplicationEventTopic extends RabbitTopic<ApplicationEvent>
		implements ApplicationEventTopic {

	@Inject
	private EventPublisher eventPublisher;

	public void subscribe(ApplicationEvent event) {
		eventPublisher.publish(event, Scope.LOCAL);
	}

}
