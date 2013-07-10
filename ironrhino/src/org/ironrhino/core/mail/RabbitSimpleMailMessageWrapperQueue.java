package org.ironrhino.core.mail;

import org.ironrhino.core.rabbitmq.RabbitQueue;
import org.springframework.beans.factory.annotation.Autowired;

public class RabbitSimpleMailMessageWrapperQueue extends
		RabbitQueue<SimpleMailMessageWrapper> implements
		SimpleMailMessageWrapperQueue {

	@Autowired(required = false)
	private MailSender mailSender;

	@Override
	public void consume(SimpleMailMessageWrapper smmw) {
		if (mailSender != null)
			mailSender
					.send(smmw.getSimpleMailMessage(), smmw.isUseHtmlFormat());
	}

}
