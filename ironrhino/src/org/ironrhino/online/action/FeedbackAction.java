package org.ironrhino.online.action;

import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.Captcha;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.online.model.Feedback;
import org.ironrhino.ums.model.User;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@AutoConfig(namespace = "/")
public class FeedbackAction extends BaseAction {

	private static final long serialVersionUID = 7297247656451307758L;

	private Feedback feedback;

	private transient BaseManager<Feedback> baseManager;

	public Feedback getFeedback() {
		return feedback;
	}

	public void setFeedback(Feedback feedback) {
		this.feedback = feedback;
	}

	public void setBaseManager(BaseManager<Feedback> baseManager) {
		this.baseManager = baseManager;
	}

	@Override
	public String input() {
		User user = AuthzUtils.getUserDetails(User.class);
		if (user != null) {
			feedback = new Feedback();
			feedback.setName(user.getName());
			feedback.setEmail(user.getEmail());
			feedback.setPhone(user.getPhone());
		}
		return SUCCESS;
	}

	@Override
	@Captcha(bypassLoggedInUser = true)
	@InputConfig(methodName = "input")
	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "feedback.name", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "feedback.title", trim = true, key = "validation.required") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "feedback.email", key = "validation.invalid") })
	public String execute() {
		if (feedback != null) {
			User user = AuthzUtils.getUserDetails(User.class);
			if (user != null)
				feedback.setUsername(user.getUsername());
			baseManager.save(feedback);
			addActionMessage(getText("save.success"));
		}
		return SUCCESS;
	}
}
