package org.ironrhino.core.event;

import org.ironrhino.core.jms.MessageConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;

public class ApplicationEventConsumer implements MessageConsumer {

	@Autowired
	private EventPublisher eventPublisher;

	public void consume(Object object) {
		eventPublisher.publish((ApplicationEvent) object, false);
	}

	public boolean supports(Class clazz) {
		return (ApplicationEvent.class.isAssignableFrom(clazz));
	}

}
