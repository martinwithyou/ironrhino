package org.ironrhino.core.event;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

@Singleton
@Named("eventPublisher")
public class EventPublisher implements
		ApplicationListener<ApplicationContextEvent> {

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

	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event.getApplicationContext().getParent() != null)
			return;
		if (event instanceof ContextRefreshedEvent) {
			if (applicationEventTopic != null)
				applicationEventTopic.publish(new InstanceStartupEvent());
			else
				publisher.publishEvent(event);
		} else if (event instanceof ContextClosedEvent) {
			if (applicationEventTopic != null)
				applicationEventTopic.publish(new InstanceShutdownEvent());
			else
				publisher.publishEvent(event);
		}
	}

}
