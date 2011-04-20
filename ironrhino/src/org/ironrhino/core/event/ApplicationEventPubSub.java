package org.ironrhino.core.event;

import java.io.IOException;

import javax.inject.Inject;

import org.ironrhino.core.rabbitmq.PubSubBase;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.SerializationUtils;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class ApplicationEventPubSub extends PubSubBase<ApplicationEvent> {

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
						consume(delivery.getBody());
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
		channel.basicPublish(exchangeName, getRoutingKey(), null,
				SerializationUtils.serialize(message));
	}

	protected void consume(byte[] message) {
		eventPublisher.publish(
				(ApplicationEvent) SerializationUtils.deserialize(message),
				false);
	}

}
