package org.ironrhino.core.rabbitmq;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.ironrhino.core.message.Topic;
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

	public void setQueueName(String queueName) {
		this.queueName = queueName;
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
		Queue q = rabbitAdmin.declareQueue();
		queueName = q.getName();
		rabbitAdmin.declareBinding(new Binding(queueName,
				DestinationType.QUEUE, exchangeName, routingKey, null));
	}

	@PreDestroy
	public void destroy() {
		rabbitAdmin.deleteQueue(queueName);
	}

	public String getRoutingKey() {
		return routingKey;
	}

	public void publish(T message) {
		amqpTemplate.convertAndSend(exchangeName, getRoutingKey(), message);
	}
}
