package org.ironrhino.core.spring;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.FactoryBean;

@Singleton
@Named("executorService")
public class ExecutorServiceFactoryBean implements
		FactoryBean<ExecutorService> {

	private ExecutorService executorService;

	@PostConstruct
	public void init() {
		executorService = Executors.newCachedThreadPool();
	}

	@PreDestroy
	public void destroy() {
		executorService.shutdown();
	}

	public ExecutorService getObject() throws Exception {
		return executorService;
	}

	public Class<? extends ExecutorService> getObjectType() {
		return ExecutorService.class;
	}

	public boolean isSingleton() {
		return true;
	}
}