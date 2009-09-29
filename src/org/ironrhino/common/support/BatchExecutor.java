package org.ironrhino.common.support;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.mail.MailService;
import org.ironrhino.core.spring.ApplicationContextConsole;
import org.ironrhino.core.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;

public class BatchExecutor {

	protected final Log log = LogFactory.getLog(getClass());

	private List<String> commands = Collections.EMPTY_LIST;

	@Autowired
	private ApplicationContextConsole applicationContextConsole;

	@Autowired(required = false)
	MailService mailService;

	public List<String> getCommands() {
		return commands;
	}

	public void setCommands(List<String> commands) {
		this.commands = commands;
	}

	public void execute() {
		for (String cmd : commands) {
			try {
				applicationContextConsole.execute(cmd);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				if (mailService != null) {
					try {
						SimpleMailMessage smm = new SimpleMailMessage();
						smm.setSubject(e.getMessage());
						smm.setText(ExceptionUtils.getStackTraceAsString(e));
						mailService.send(smm, false);
					} catch (Exception ee) {
						log.warn("send email failed", ee);
					}
				}
			}
		}
	}

}
