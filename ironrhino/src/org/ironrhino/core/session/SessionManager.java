package org.ironrhino.core.session;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class SessionManager implements BeanFactoryAware {

	private BeanFactory beanFactory;

	public void save(Session sesion) {
		SessionStore sessionStore = (SessionStore) beanFactory
				.getBean("sessionStore");
		sessionStore.save(sesion);
	}

	public void initialize(Session session) {
		SessionStore sessionStore = (SessionStore) beanFactory
				.getBean("sessionStore");
		sessionStore.initialize(session);
	}

	public void invalidate(Session sesion) {
		SessionStore sessionStore = (SessionStore) beanFactory
				.getBean("sessionStore");
		sessionStore.invalidate(sesion);
	}

	public SessionStore getSessionStore() {
		return (SessionStore) beanFactory.getBean("sessionStore");
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}
