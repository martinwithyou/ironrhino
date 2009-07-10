package org.ironrhino.online.support;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.support.TemplateProvider;
import org.ironrhino.core.mail.MailService;
import org.ironrhino.online.model.Account;
import org.ironrhino.online.service.AccountManager;
import org.ironrhino.online.service.ProductFacade;
import org.ironrhino.pms.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.orm.hibernate3.HibernateCallback;

import freemarker.template.Template;

public class NewArrivalProductNotifier {

	private int batchSize = 50;

	private String subject;

	private String templateName = "template/product_list.ftl";

	private String extraEmails;

	@Autowired
	private ProductFacade productFacade;

	@Autowired
	private TemplateProvider templateProvider;

	@Autowired
	private MailService mailService;

	@Autowired
	private AccountManager accountManager;

	private Lock lock = new ReentrantLock();

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public String getExtraEmails() {
		return extraEmails;
	}

	public void setExtraEmails(String extraEmails) {
		this.extraEmails = extraEmails;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void send() throws IOException {
		lock.lock();
		try {
			List<String> result = getTo();
			if (result.size() == 0)
				return;
			Template template = templateProvider.getTemplate(templateName);
			Map<String, List<Product>> model = new HashMap<String, List<Product>>(
					1);
			model.put("productList", productFacade.getNewArrivalProducts(
					Integer.MAX_VALUE, 7));
			StringWriter writer = new StringWriter();
			try {
				template.process(model, writer);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String text = writer.toString();
			SimpleMailMessage smm = new SimpleMailMessage();
			smm.setSubject(subject);
			smm.setText(text);
			String[] to = new String[batchSize];
			int index = -1;
			for (int i = 0; i < result.size(); i++) {
				index++;
				to[index] = result.get(i);
				if (index == batchSize - 1) {
					smm.setTo(to);
					mailService.send(smm);
				}
				if (i == result.size() - 1) {
					String[] array = new String[index + 1];
					System.arraycopy(to, 0, array, 0, index + 1);
					smm.setTo(array);
					mailService.send(smm);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	private List<String> getTo() {
		List<String> emails = (List<String>) accountManager
				.execute(new HibernateCallback() {
					public Object doInHibernate(Session session)
							throws HibernateException, SQLException {
						List<String> emails = new ArrayList<String>();
						Criteria c = session.createCriteria(Account.class);
						c.setProjection(Projections.projectionList().add(
								Projections.property("name")).add(
								Projections.property("email")));
						c.add(Restrictions.eq("subscribed", true));
						List<Object[]> result = c.list();
						for (Object[] array : result) {
							String name = (String) array[0];
							String email = (String) array[1];
							if (StringUtils.isNotBlank(name))
								email = name + "<" + email + ">";
							emails.add(email);
						}
						return emails;
					}
				});
		if (extraEmails != null)
			emails.addAll(Arrays.asList(extraEmails.split(",")));
		return emails;
	}
}
