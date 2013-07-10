package org.ironrhino.core.mail;

import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.ironrhino.core.redis.RedisQueue;
import org.springframework.beans.factory.annotation.Autowired;

public class RedisSimpleMailMessageWrapperQueue extends
		RedisQueue<SimpleMailMessageWrapper> implements
		SimpleMailMessageWrapperQueue {

	@Autowired(required = false)
	private ExecutorService executorService;

	@Autowired(required = false)
	private MailSender mailSender;

	private boolean stop;

	@Override
	@PostConstruct
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (mailSender != null) {
			Runnable task = new Runnable() {

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
				executorService.execute(task);
			else
				new Thread(task).start();
		}
	}

	@PreDestroy
	public void destroy() {
		stop = true;
	}

	@Override
	public void consume(SimpleMailMessageWrapper smmw) {
		if (mailSender != null)
			mailSender
					.send(smmw.getSimpleMailMessage(), smmw.isUseHtmlFormat());
	}

}
