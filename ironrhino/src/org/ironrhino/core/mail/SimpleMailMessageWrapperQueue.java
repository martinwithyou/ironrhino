package org.ironrhino.core.mail;

import java.io.IOException;

import javax.inject.Inject;

import org.ironrhino.core.rabbitmq.QueueBase;
import org.springframework.util.SerializationUtils;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class SimpleMailMessageWrapperQueue extends
		QueueBase<SimpleMailMessageWrapper> {

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
						consume(delivery.getBody());
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

	protected void consume(byte[] message) {
		SimpleMailMessageWrapper smmw = (SimpleMailMessageWrapper) SerializationUtils
				.deserialize(message);
		mailSender.send(smmw.getSimpleMailMessage(), smmw.isUseHtmlFormat());
	}

}
