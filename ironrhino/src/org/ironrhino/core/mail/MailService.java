package org.ironrhino.core.mail;

import java.io.StringWriter;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.struts.TemplateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.SimpleMailMessage;

import freemarker.template.Template;

public class MailService {

	private Log log = LogFactory.getLog(getClass());

	@Autowired(required = false)
	private TemplateProvider templateProvider;

	@Autowired(required = false)
	private JmsTemplate jmsTemplate;

	@Inject
	private MailSender mailSender;

	private boolean forceSync;

	private int forceSyncFailureThreshold = 3;

	private int _failureCount;

	public boolean isForceSync() {
		return forceSync;
	}

	public void setForceSync(boolean forceSync) {
		this.forceSync = forceSync;
	}

	public void setForceSyncFailureThreshold(int forceSyncFailureThreshold) {
		this.forceSyncFailureThreshold = forceSyncFailureThreshold;
	}

	public void send(SimpleMailMessage smm) {
		send(smm, true);
	}

	public void send(SimpleMailMessage smm, boolean useHtmlFormat) {
		if (jmsTemplate == null || forceSync) {
			// synchronized
			mailSender.send(smm, useHtmlFormat);
			return;
		}
		try {
			// asynchronized
			jmsTemplate.convertAndSend(new SimpleMailMessageWrapper(smm,
					useHtmlFormat));
		} catch (JmsException e) {
			log.error(e.getMessage(), e);
			_failureCount++;
			if (_failureCount >= forceSyncFailureThreshold) {
				_failureCount = 0;
				forceSync = true;
			}
			send(smm, useHtmlFormat);
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
