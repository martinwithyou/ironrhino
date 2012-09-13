package org.ironrhino.core.event;

import java.io.IOException;

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
	private ApplicationEventRedisTopic applicationEventRedisTopic;

	@Autowired(required = false)
	private ApplicationEventRabbitTopic applicationEventRabbitTopic;

	public void publish(ApplicationEvent event, boolean global) {
		if (applicationEventRedisTopic != null && global) {
			applicationEventRedisTopic.publish(event);
		} else if (applicationEventRabbitTopic != null && global) {
			try {
				applicationEventRabbitTopic.publish(event);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			publisher.publishEvent(event);
		}
	}

}
