package org.ironrhino.core.spring;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

@Singleton
@Named("executorService")
public class ExecutorServiceFactoryBean implements
		FactoryBean<ExecutorService>, InitializingBean, DisposableBean {

	private ExecutorService executorService;

	public void afterPropertiesSet() {
		executorService = Executors.newCachedThreadPool();
	}

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