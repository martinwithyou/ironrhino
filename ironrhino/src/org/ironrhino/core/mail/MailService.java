package org.ironrhino.core.mail;

import java.io.StringWriter;
import java.util.Map;

import org.ironrhino.core.ext.struts.TemplateProvider;
import org.ironrhino.core.jms.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;

import freemarker.template.Template;

public class MailService {

	@Autowired(required = false)
	private TemplateProvider templateProvider;

	@Autowired(required = false)
	private MessageProducer messageProducer;

	@Autowired
	private MailSender mailSender;

	public void send(SimpleMailMessage smm) {
		send(smm, true);
	}

	public void send(SimpleMailMessage smm, boolean useHtmlFormat) {
		if (messageProducer != null) {
			// asynchronized
			messageProducer.produce(new SimpleMailMessageWrapper(smm,
					useHtmlFormat));
		} else {
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
		if (messageProducer != null) {
			messageProducer.produce(new SimpleMailMessageWrapper(smm,
					useHtmlFormat));
		} else {
			mailSender.send(smm, useHtmlFormat);
		}
	}

}
