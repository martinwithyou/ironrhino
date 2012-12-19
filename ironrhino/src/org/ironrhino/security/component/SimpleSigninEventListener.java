package org.ironrhino.security.component;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.security.event.SigninEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

@Singleton
@Named
public class SimpleSigninEventListener implements
		ApplicationListener<SigninEvent> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onApplicationEvent(SigninEvent event) {
		logger.info(event.getUser().getUsername()
				+ " signin"
				+ (event.getProvider() == null ? "" : ",via "
						+ event.getProvider()));
	}

}
