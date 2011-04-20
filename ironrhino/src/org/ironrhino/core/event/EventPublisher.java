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
	private ApplicationEventPubSub applicationEventPubSub;

	public void publish(ApplicationEvent event, boolean global) {
		if (applicationEventPubSub != null && global) {
			try {
				applicationEventPubSub.publish(event);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			publisher.publishEvent(event);
		}
	}

}
