package org.ironrhino.online.action;

import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.core.annotation.Captcha;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.online.model.Account;
import org.ironrhino.online.model.Feedback;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

public class FeedbackAction extends BaseAction {

	private Feedback feedback;

	private BaseManager<Feedback> baseManager;

	public Feedback getFeedback() {
		return feedback;
	}

	public void setFeedback(Feedback feedback) {
		this.feedback = feedback;
	}

	public void setBaseManager(BaseManager<Feedback> baseManager) {
		this.baseManager = baseManager;
	}

	public String input() {
		Account account = AuthzUtils.getUserDetails(Account.class);
		if (account != null) {
			feedback = new Feedback();
			feedback.setName(account.getName());
			feedback.setEmail(account.getEmail());
			feedback.setTelephone(account.getTelephone());
		}
		return INPUT;
	}

	@Captcha
	@InputConfig(methodName="input")
	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "feedback.name", trim = true, key = "feedback.name.required", message = "请输入您的名字"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "feedback.subject", trim = true, key = "feedback.subject.required", message = "请输入主题") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "feedback.email", key = "feedback.email.invalid", message = "请输入正确的email") })
	public String execute() {
		if (feedback != null) {
			Account account = AuthzUtils.getUserDetails(Account.class);
			if (account != null)
				feedback.setUsername(account.getUsername());
			baseManager.save(feedback);
			addActionMessage(getText("feedback.successfully"));
		}
		return REFERER;
	}
}
