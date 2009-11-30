package org.ironrhino.core.event;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.jms.Destination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.core.JmsTemplate;

@Singleton
@Named("eventPublisher")
public class EventPublisher {

	@Inject
	private ApplicationEventPublisher publisher;

	@Autowired(required = false)
	private JmsTemplate jmsTemplate;

	@Autowired(required = false)
	@Named("applicationEventDestination")
	private Destination applicationEventDestination;

	public void publish(ApplicationEvent event, boolean global) {
		if (jmsTemplate != null && global) {
			jmsTemplate.convertAndSend(applicationEventDestination, event);
		} else {
			publisher.publishEvent(event);
		}
	}

}
