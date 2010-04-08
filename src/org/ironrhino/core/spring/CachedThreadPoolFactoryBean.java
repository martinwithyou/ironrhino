package org.ironrhino.core.spring;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.FactoryBean;

@Singleton
@Named("cachedThreadPool")
public class CachedThreadPoolFactoryBean implements
		FactoryBean<ExecutorService> {

	private ExecutorService cachedThreadPool;

	@PostConstruct
	public void init() {
		cachedThreadPool = Executors.newCachedThreadPool();
	}

	@PreDestroy
	public void destroy() {
		cachedThreadPool.shutdown();
	}

	@Override
	public ExecutorService getObject() throws Exception {
		return cachedThreadPool;
	}

	@Override
	public Class<? extends ExecutorService> getObjectType() {
		return ExecutorService.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}