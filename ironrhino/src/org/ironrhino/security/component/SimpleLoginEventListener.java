package org.ironrhino.security.component;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.security.event.LoginEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

@Singleton
@Named
public class SimpleLoginEventListener implements
		ApplicationListener<LoginEvent> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onApplicationEvent(LoginEvent event) {
		logger.info(event.getUser().getUsername()
				+ " login"
				+ (event.getProvider() == null ? "" : ",via "
						+ event.getProvider()));
	}

}
