package org.ironrhino.core.spring.configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@Configuration
public class SchedulingConfiguration implements SchedulingConfigurer {

	@Value("${taskScheduler.poolSize:5}")
	private int taskSchedulerPoolSize = 5;

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setTaskScheduler(taskScheduler());
	}

	@Bean(destroyMethod = "shutdown")
	public ScheduledExecutorService taskSchedulerThreadPool() {
		return Executors.newScheduledThreadPool(taskSchedulerPoolSize);
	}

	@Bean
	public TaskScheduler taskScheduler() {
		return new ConcurrentTaskScheduler(taskSchedulerThreadPool());
	}

}
