package org.ironrhino.core.event;

import javax.jms.Destination;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component("eventPublisher")
public class EventPublisher {

	@Autowired
	private ApplicationEventPublisher publisher;

	@Autowired(required = false)
	private JmsTemplate jmsTemplate;

	@Autowired(required = false)
	@Qualifier("applicationEventDestination")
	private Destination applicationEventDestination;

	public void publish(ApplicationEvent event, boolean global) {
		if (jmsTemplate != null && global) {
			jmsTemplate.convertAndSend(applicationEventDestination, event);
		} else {
			publisher.publishEvent(event);
		}
	}

}
