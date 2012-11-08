package org.ironrhino.core.mail;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.lang3.SerializationUtils;
import org.ironrhino.core.rabbitmq.RabbitQueue;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class SimpleMailMessageWrapperRabbitQueue extends
		RabbitQueue<SimpleMailMessageWrapper> {

	@Inject
	private MailSender mailSender;

	protected void postQueueDeclare() throws Exception {
		final QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, false, consumer);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				QueueingConsumer.Delivery delivery = null;
				while (true) {
					try {
						delivery = consumer.nextDelivery();
						SimpleMailMessageWrapper smmw = (SimpleMailMessageWrapper) SerializationUtils
								.deserialize(delivery.getBody());
						consume(smmw);
						channel.basicAck(delivery.getEnvelope()
								.getDeliveryTag(), false);
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

	public void produce(SimpleMailMessageWrapper message) throws IOException {
		lock.lock();
		try {
			channel.basicPublish("", getQueueName(), null,
					SerializationUtils.serialize(message));
		} finally {
			lock.unlock();
		}
	}

	public void consume(SimpleMailMessageWrapper smmw) {
		mailSender.send(smmw.getSimpleMailMessage(), smmw.isUseHtmlFormat());
	}

}
