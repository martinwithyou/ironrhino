package org.ironrhino.ums.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.ums.model.User;
import org.ironrhino.ums.service.UserManager;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.ExpressionValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@AutoConfig(namespace = "/backend")
public class ChangePasswordAction extends BaseAction {

	protected Log log = LogFactory.getLog(getClass());

	private String currentPassword;

	private String password;

	private String confirmPassword;

	private UserManager userManager;

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	@InputConfig
	@Validations(stringLengthFields = { @StringLengthFieldValidator(type = ValidatorType.FIELD, trim = true, minLength = "6", maxLength = "20", fieldName = "password", key = "password.required", message = "密码的长度为6-20") }, expressions = { @ExpressionValidator(expression = "password == confirmPassword", key = "confirmPassword.error", message = "两次输入密码不一致") })
	public String execute() {
		User user = AuthzUtils.getUserDetails(User.class);
		if (user == null || !user.isPasswordValid(currentPassword)) {
			addFieldError("currentPassword", getText("currentPassword.error"));
			return INPUT;
		}
		user.setLegiblePassword(password);
		log.info("'" + user.getUsername() + "' changed password");
		userManager.save(user);
		addActionMessage(getText("change.password.successfully"));
		return INPUT;
	}
}
