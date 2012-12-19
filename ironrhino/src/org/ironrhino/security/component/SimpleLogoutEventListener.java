package org.ironrhino.security.component;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.security.event.LogoutEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

@Singleton
@Named
public class SimpleLogoutEventListener implements
		ApplicationListener<LogoutEvent> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onApplicationEvent(LogoutEvent event) {
		logger.info(event.getUser().getUsername() + " logout");
	}

}
