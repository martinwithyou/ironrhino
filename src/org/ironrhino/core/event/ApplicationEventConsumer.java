package org.ironrhino.core.event;

import javax.inject.Inject;

import org.ironrhino.core.jms.MessageConsumer;
import org.springframework.context.ApplicationEvent;

public class ApplicationEventConsumer implements MessageConsumer {

	@Inject
	private EventPublisher eventPublisher;

	public void consume(Object object) {
		eventPublisher.publish((ApplicationEvent) object, false);
	}

	public boolean supports(Class clazz) {
		return (ApplicationEvent.class.isAssignableFrom(clazz));
	}

}
