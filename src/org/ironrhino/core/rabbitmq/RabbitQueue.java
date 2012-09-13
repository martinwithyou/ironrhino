package org.ironrhino.core.rabbitmq;

import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.ironrhino.core.message.Queue;
import org.ironrhino.core.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public abstract class RabbitQueue<T extends Serializable> implements Queue<T> {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	protected Connection connection;

	protected Channel channel;

	protected String queueName = "";

	protected boolean durable = true;

	protected Thread consumerThread;

	protected Lock lock = new ReentrantLock();

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public RabbitQueue() {
		Class<?> clazz = ReflectionUtils.getGenericClass(getClass());
		if (clazz != null)
			queueName = clazz.getName();
	}

	@PostConstruct
	public void init() throws Exception {
		channel = connection.createChannel();
		channel.queueDeclare(queueName, durable, false, false, null);
		postQueueDeclare();
	}

	@PreDestroy
	public void destroy() throws Exception {
		try {
			if (consumerThread != null)
				consumerThread.interrupt();
		} finally {
			channel.close();
		}
	}

	protected void postQueueDeclare() throws Exception {

	}

}
