package org.ironrhino.core.rabbitmq;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.ironrhino.core.message.Topic;
import org.ironrhino.core.metadata.Scope;
import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.ReflectionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;

public abstract class RabbitTopic<T extends Serializable> implements Topic<T> {

	@Inject
	protected AmqpTemplate amqpTemplate;

	@Inject
	protected RabbitAdmin rabbitAdmin;

	@Value("${rabbitmq.exchangeName:ironrhino}")
	protected String exchangeName;

	protected String routingKey = "";

	protected String queueName;

	public String getQueueName() {
		return queueName;
	}

	public RabbitTopic() {
		Class<?> clazz = ReflectionUtils.getGenericClass(getClass());
		if (clazz != null)
			routingKey = clazz.getName();
	}

	@PostConstruct
	public void init() {
		rabbitAdmin
				.declareExchange(new TopicExchange(exchangeName, true, false));
		Queue queue = rabbitAdmin.declareQueue();
		queueName = queue.getName();
		rabbitAdmin.declareBinding(new Binding(queueName,
				DestinationType.QUEUE, exchangeName,
				getRoutingKey(Scope.GLOBAL) + "#", null));
	}

	@PreDestroy
	public void destroy() {
		rabbitAdmin.deleteQueue(queueName);
	}

	protected String getRoutingKey(Scope scope) {
		if (scope == null || scope == Scope.LOCAL)
			return null;
		StringBuilder sb = new StringBuilder(routingKey).append("@");
		if (scope == Scope.APPLICATION)
			sb.append(AppInfo.getAppName());
		return sb.toString();
	}

	public void publish(T message, Scope scope) {
		if (scope == null || scope == Scope.LOCAL)
			subscribe(message);
		else
			amqpTemplate.convertAndSend(exchangeName, getRoutingKey(scope),
					message);
	}
}
