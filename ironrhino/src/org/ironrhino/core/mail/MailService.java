package org.ironrhino.core.mail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;

import org.ironrhino.core.struts.TemplateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;

import freemarker.template.Template;

public class MailService {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired(required = false)
	private TemplateProvider templateProvider;

	@Autowired(required = false)
	private SimpleMailMessageWrapperRedisQueue simpleMailMessageWrapperRedisQueue;

	@Autowired(required = false)
	private SimpleMailMessageWrapperRabbitQueue simpleMailMessageWrapperRabbitQueue;

	@Inject
	private MailSender mailSender;

	@Inject
	@Named("executorService")
	private ExecutorService executorService;

	private boolean forceLocalAsync;

	private int forceLocalAsyncFailureThreshold = 3;

	private int _failureCount;

	public boolean isForceSync() {
		return forceLocalAsync;
	}

	public void setForceLocalAsync(boolean forceLocalAsync) {
		this.forceLocalAsync = forceLocalAsync;
	}

	public void setForceLocalAsyncFailureThreshold(
			int forceLocalAsyncFailureThreshold) {
		this.forceLocalAsyncFailureThreshold = forceLocalAsyncFailureThreshold;
	}

	public void send(SimpleMailMessage smm) {
		send(smm, true);
	}

	public void send(final SimpleMailMessage smm, final boolean useHtmlFormat) {
		if ((simpleMailMessageWrapperRedisQueue == null && simpleMailMessageWrapperRabbitQueue == null)
				|| forceLocalAsync) {
			// localAsync
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					mailSender.send(smm, useHtmlFormat);
				}
			});
			return;
		}
		try {
			if (simpleMailMessageWrapperRedisQueue != null)
				simpleMailMessageWrapperRedisQueue
						.produce(new SimpleMailMessageWrapper(smm,
								useHtmlFormat));
			else if (simpleMailMessageWrapperRabbitQueue != null)
				simpleMailMessageWrapperRabbitQueue
						.produce(new SimpleMailMessageWrapper(smm,
								useHtmlFormat));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			_failureCount++;
			if (_failureCount >= forceLocalAsyncFailureThreshold) {
				_failureCount = 0;
				forceLocalAsync = true;
			}
			send(smm, useHtmlFormat);
		}
	}

	public void send(SimpleMailMessage smm, String templateName, Map<String,Object> model) {
		send(smm, templateName, model, true);
	}

	public void send(SimpleMailMessage smm, String templateName, Map<String,Object> model,
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
