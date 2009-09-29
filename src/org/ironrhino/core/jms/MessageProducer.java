package org.ironrhino.core.jms;

import javax.jms.Queue;

import org.springframework.jms.core.JmsTemplate;

public class MessageProducer {

	private JmsTemplate template;

	private Queue destination;

	public void setTemplate(JmsTemplate template) {
		this.template = template;
	}

	public void setDestination(Queue destination) {
		this.destination = destination;
	}

	public void produce(Object object) {
		template.convertAndSend(this.destination, object);
	}

}
