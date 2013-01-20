package org.ironrhino.core.mail;

import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.ironrhino.core.redis.RedisQueue;
import org.springframework.beans.factory.annotation.Autowired;

public class RedisSimpleMailMessageWrapperQueue extends
		RedisQueue<SimpleMailMessageWrapper> implements
		SimpleMailMessageWrapperQueue {

	@Autowired(required = false)
	private ExecutorService executorService;

	@Inject
	private MailSender mailSender;

	private boolean stop;

	@PostConstruct
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		Runnable ruannble = new Runnable() {

			@Override
			public void run() {
				while (!stop) {
					try {
						SimpleMailMessageWrapper smmw = queue.take();
						consume(smmw);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}
			}

		};
		if (executorService != null)
			executorService.execute(ruannble);
		else
			new Thread(ruannble).start();
	}

	@PreDestroy
	public void destroy() {
		stop = true;
	}

	public void consume(SimpleMailMessageWrapper smmw) {
		mailSender.send(smmw.getSimpleMailMessage(), smmw.isUseHtmlFormat());
	}

}
