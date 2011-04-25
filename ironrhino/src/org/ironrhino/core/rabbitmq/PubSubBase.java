package org.ironrhino.core.rabbitmq;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class PubSubBase<T> {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	public static final String EXCHANGE_TYPE = "direct";

	@Inject
	protected Connection connection;

	protected Channel channel;

	protected String exchangeName = AppInfo.getAppName();

	protected String routingKey = "";

	protected Thread consumerThread;
	
	protected Lock lock = new ReentrantLock();

	public PubSubBase() {
		Class clazz = ReflectionUtils.getGenericClass(getClass());
		if (clazz != null)
			routingKey = clazz.getName();
	}

	@PostConstruct
	public void init() throws Exception {
		channel = connection.createChannel();
		channel.exchangeDeclare(exchangeName, EXCHANGE_TYPE, true);
		postExchangeDeclare();
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

	protected void postExchangeDeclare() throws Exception {

	}

	public String getRoutingKey() {
		return routingKey;
	}

}
