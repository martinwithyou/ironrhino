package org.ironrhino.core.mail;

import javax.inject.Inject;

import org.ironrhino.core.rabbitmq.RabbitQueue;

public class RabbitSimpleMailMessageWrapperQueue extends
		RabbitQueue<SimpleMailMessageWrapper> implements
		SimpleMailMessageWrapperQueue {

	@Inject
	private MailSender mailSender;

	@Override
	public void consume(SimpleMailMessageWrapper smmw) {
		mailSender.send(smmw.getSimpleMailMessage(), smmw.isUseHtmlFormat());
	}

}
