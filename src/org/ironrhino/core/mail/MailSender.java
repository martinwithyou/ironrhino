package org.ironrhino.core.mail;

import java.io.UnsupportedEncodingException;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * send synchronized mail
 * 
 * @author zym
 * 
 */
public class MailSender {

	private Log log = LogFactory.getLog(MailSender.class);

	private String defaultFrom = "billgates@gmail.com";

	private String defaultTo = "zhouyanming@gmail.com";

	@Inject
	private JavaMailSenderImpl javaMailSender;

	public String getDefaultFrom() {
		return defaultFrom;
	}

	public void setDefaultFrom(String defaultFrom) {
		this.defaultFrom = defaultFrom;
	}

	public String getDefaultTo() {
		return defaultTo;
	}

	public void setDefaultTo(String defaultTo) {
		this.defaultTo = defaultTo;
	}

	public void send(final SimpleMailMessage smm) {
		if (smm.getTo() == null || smm.getTo().length == 0)
			smm.setTo(defaultTo);
		send(smm, true);
	}

	public void send(final SimpleMailMessage smm, final boolean useHtmlFormat) {
		if (smm == null || smm.getTo() == null)
			return;
		for (final String to : smm.getTo()) {
			try {
				javaMailSender.send(new MimeMessagePreparator() {
					public void prepare(MimeMessage mimeMessage)
							throws MessagingException {
						MimeMessageHelper message = new MimeMessageHelper(
								mimeMessage, true, "UTF-8");
						if (StringUtils.isNotBlank(smm.getFrom()))
							message.setFrom(encode(smm.getFrom()));
						else
							message.setFrom(encode(defaultFrom));
						if (StringUtils.isNotBlank(smm.getReplyTo()))
							message.setReplyTo(encode(smm.getReplyTo()));
						message.setTo(encode(to));
						message.setSubject(smm.getSubject());
						message.setText(smm.getText(), useHtmlFormat);
					}
				});
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public static String encode(String to) {
		if (StringUtils.isBlank(to))
			return to;
		String[] array = to.split(",");
		for (int i = 0; i < array.length; i++) {
			if (array[i].indexOf('<') < 0)
				continue;
			String name = array[i].substring(0, array[i].indexOf('<'));
			name = name.trim();
			name = StringUtils.remove(name, "\"");
			name = StringUtils.remove(name, "'");
			String email = array[i].substring(array[i].indexOf('<'));
			try {
				array[i] = MimeUtility.encodeWord(name, "UTF-8", "Q") + email;
			} catch (UnsupportedEncodingException e) {
				email = email.substring(1, email.length());
				array[i] = email;
			}
		}
		return StringUtils.join(array, ",");
	}

}
