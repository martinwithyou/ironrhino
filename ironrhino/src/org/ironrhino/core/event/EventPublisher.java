package org.ironrhino.core.event;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

@Singleton
@Named("eventPublisher")
public class EventPublisher {

	@Inject
	private ApplicationEventPublisher publisher;

	@Autowired(required = false)
	private ApplicationEventTopic applicationEventTopic;

	public void publish(ApplicationEvent event, boolean global) {
		if (applicationEventTopic != null && global)
			applicationEventTopic.publish(event);
		else
			publisher.publishEvent(event);
	}

}
