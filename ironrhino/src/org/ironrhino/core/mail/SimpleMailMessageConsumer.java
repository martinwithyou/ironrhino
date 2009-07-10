package org.ironrhino.core.mail;

import org.ironrhino.core.jms.MessageConsumer;
import org.springframework.beans.factory.annotation.Autowired;

public class SimpleMailMessageConsumer implements MessageConsumer {

	@Autowired
	private MailSender mailSender;

	public void consume(Object object) {
		SimpleMailMessageWrapper smmw = (SimpleMailMessageWrapper) object;
		mailSender.send(smmw.getSimpleMailMessage(), smmw.isUseHtmlFormat());
	}

	public boolean supports(Class clazz) {
		return (SimpleMailMessageWrapper.class.isAssignableFrom(clazz));
	}

}
