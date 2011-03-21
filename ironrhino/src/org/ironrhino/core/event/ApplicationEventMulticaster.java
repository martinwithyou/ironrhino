package org.ironrhino.core.event;

import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.security.access.event.AuthorizedEvent;

@Named
@Singleton
public class ApplicationEventMulticaster extends
		SimpleApplicationEventMulticaster {

	@Autowired(required = false)
	@Named("cachedThreadPool")
	private ExecutorService taskExecutor;

	@PostConstruct
	public void afterPropertiesSet() {
		setTaskExecutor(taskExecutor);
	}

	@Override
	public void multicastEvent(ApplicationEvent event) {
		if (!(event instanceof AuthorizedEvent))
			super.multicastEvent(event);
	}

}
