package org.ironrhino.core.event;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.core.metadata.Scope;
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

	@Autowired(required = false)
	private ExecutorService executorService;

	public void publish(final ApplicationEvent event, final Scope scope) {
		Runnable task = new Runnable() {
			public void run() {
				if (applicationEventTopic != null && scope != null
						&& scope != Scope.LOCAL)
					applicationEventTopic.publish(event, scope);
				else
					publisher.publishEvent(event);
			}
		};
		if (executorService == null)
			task.run();
		else
			executorService.submit(task);
	}

	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		if (event.getApplicationContext().getParent() != null)
			return;
		if (event instanceof ContextRefreshedEvent) {
			if (applicationEventTopic != null)
				applicationEventTopic.publish(new InstanceStartupEvent(),
						Scope.GLOBAL);
			else
				publisher.publishEvent(new InstanceStartupEvent());
		} else if (event instanceof ContextClosedEvent) {
			if (applicationEventTopic != null)
				applicationEventTopic.publish(new InstanceShutdownEvent(),
						Scope.GLOBAL);
			else
				publisher.publishEvent(new InstanceShutdownEvent());
		}
	}

}
