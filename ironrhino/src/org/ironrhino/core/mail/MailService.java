package org.ironrhino.core.mail;

import java.io.StringWriter;
import java.util.Map;

import org.ironrhino.core.struts.TemplateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.SimpleMailMessage;

import freemarker.template.Template;

public class MailService {

	@Autowired(required = false)
	private TemplateProvider templateProvider;

	@Autowired(required = false)
	private JmsTemplate jmsTemplate;

	@Autowired
	private MailSender mailSender;

	public void send(SimpleMailMessage smm) {
		send(smm, true);
	}

	public void send(SimpleMailMessage smm, boolean useHtmlFormat) {
		try {
			// asynchronized
			jmsTemplate.convertAndSend(new SimpleMailMessageWrapper(smm,
					useHtmlFormat));
		} catch (Exception e) {
			e.printStackTrace();
			// synchronized
			mailSender.send(smm, useHtmlFormat);
		}
	}

	public void send(SimpleMailMessage smm, String templateName, Map model) {
		send(smm, templateName, model, true);
	}

	public void send(SimpleMailMessage smm, String templateName, Map model,
			boolean useHtmlFormat) {
		if (templateProvider == null)
			throw new RuntimeException("No templateProvider setted");
		model.put("sendmail", true);
		StringWriter writer = new StringWriter();
		try {
			Template template = templateProvider.getTemplate(templateName);
			template.process(model, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
		smm.setText(writer.toString());
		send(smm, useHtmlFormat);
	}

}
