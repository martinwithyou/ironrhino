package org.ironrhino.core.session;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class SessionManager implements BeanFactoryAware {

	private BeanFactory beanFactory;

	private SessionStore sessionStore;

	private ThreadLocal session = new ThreadLocal();

	public void setHttpSession(Session s) {
		this.session.set(s);
		sessionStore = (SessionStore) beanFactory.getBean("sessionStore");
	}

	public Session getHttpSession() {
		return (Session) this.session.get();
	}

	public void save() {
		sessionStore.save(getHttpSession());
	}

	public void initialize() {
		sessionStore.initialize(getHttpSession());
	}

	public void invalidate() {
		sessionStore.invalidate(getHttpSession());
	}

	public SessionStore getSessionStore() {
		return sessionStore;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}
