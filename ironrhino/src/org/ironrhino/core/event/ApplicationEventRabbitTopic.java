package org.ironrhino.core.event;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.ironrhino.core.rabbitmq.RabbitTopic;
import org.springframework.context.ApplicationEvent;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class ApplicationEventRabbitTopic extends RabbitTopic<ApplicationEvent> {

	@Inject
	private EventPublisher eventPublisher;

	protected void postExchangeDeclare() throws Exception {
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, exchangeName, getRoutingKey());
		final QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				QueueingConsumer.Delivery delivery = null;
				while (true) {
					try {
						delivery = consumer.nextDelivery();
						ApplicationEvent event = (ApplicationEvent) SerializationUtils
								.deserialize(delivery.getBody());
						subscribe(event);
					} catch (ShutdownSignalException e) {
						break;
					} catch (ConsumerCancelledException e) {
						break;
					} catch (InterruptedException e) {
						break;
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		};
		consumerThread = new Thread(runnable, getClass().getSimpleName());
		consumerThread.start();
	}

	public void publish(ApplicationEvent message) throws IOException {
		lock.lock();
		try {
			channel.basicPublish(exchangeName, getRoutingKey(), null,
					SerializationUtils.serialize(message));
		} finally {
			lock.unlock();
		}
	}

	public void subscribe(ApplicationEvent event) {
		eventPublisher.publish(event, false);
	}

}
