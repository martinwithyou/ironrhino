package com.ironrhino.online.action.account;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.hibernate.LockMode;
import org.ironrhino.common.model.Region;
import org.ironrhino.common.support.RegionTreeControl;
import org.ironrhino.core.mail.MailService;
import org.ironrhino.core.security.util.Blowfish;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.BeanUtils;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.ums.model.User;
import org.ironrhino.ums.service.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

public class AccountAction extends BaseAction {

	private static final long serialVersionUID = 5923401396213171705L;

	protected static Log log = LogFactory.getLog(AccountAction.class);

	private User user;

	private String currentPassword;

	private String password;

	private String confirmPassword;

	@Autowired
	private transient RegionTreeControl regionTreeControl;
	@Autowired
	private transient UserManager userManager;
	@Autowired
	private transient MailService mailService;

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String execute() {
		return SUCCESS;
	}

	@Override
	public String input() {
		if ("profile".equals(originalActionName)) {
			user = new User();
			BeanUtils.copyProperties(AuthzUtils.getUserDetails(User.class),
					user);
			if (StringUtils.isBlank(user.getAddress())) {
				Region region = regionTreeControl
						.parseByHost(RequestUtils.getRemoteAddr(ServletActionContext.getRequest()));
				if (region != null)
					user.setAddress(region.getFullname());
			}
		} else if ("email".equals(originalActionName)) {
			user = new User();
			user.setEmail(AuthzUtils.getUserDetails(User.class).getEmail());
		}
		return originalMethod;
	}

	public String manage() {
		user = new User();
		BeanUtils.copyProperties(AuthzUtils.getUserDetails(User.class), user);
		if (StringUtils.isBlank(user.getAddress())) {
			Region region = regionTreeControl.parseByHost(RequestUtils.getRemoteAddr(ServletActionContext
					.getRequest()));
			if (region != null)
				user.setAddress(region.getFullname());
		}
		return "manage";
	}

	@InputConfig(methodName = "input")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "password", trim = true, key = "validation.required") }, stringLengthFields = { @StringLengthFieldValidator(type = ValidatorType.FIELD, trim = true, minLength = "6", maxLength = "20", fieldName = "password", key = "validation.invalid") }, fieldExpressions = { @FieldExpressionValidator(expression = "password == confirmPassword", fieldName = "confirmPassword", key = "confirmPassword.error") })
	public String password() {
		User currentUser = AuthzUtils.getUserDetails(User.class);
		if (!currentUser.isPasswordValid(currentPassword)) {
			addFieldError("currentPassword", getText("currentPassword.error"));
			return INPUT;
		}
		currentUser.setLegiblePassword(password);
		log.info("'" + currentUser.getUsername() + "' edited password");
		userManager.save(currentUser);
		addActionMessage(getText("save.success"));
		return "password";
	}

	@InputConfig(methodName = "input")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.email", trim = true, key = "validation.required") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "user.email", key = "validation.invalid") })
	public String email() {
		User currentUser = AuthzUtils.getUserDetails(User.class);
		if (!currentUser.getEmail().equalsIgnoreCase(user.getEmail())) {
			if (!currentUser.isPasswordValid(currentPassword)) {
				addFieldError("currentPassword",
						getText("currentPassword.error"));
				return INPUT;
			}
			User acc = userManager.getByEmail(user.getEmail());
			if (acc != null) {
				addActionError(getText("validation.already.exists"));
				return SUCCESS;
			} else {
				currentUser.setEmail(user.getEmail());
				currentUser.setEnabled(false);
				userManager.save(currentUser);
				addActionMessage(getText("save.success"));
				sendActivationMail(currentUser);
			}
		}
		return "email";
	}

	@InputConfig(methodName = "input")
	public String profile() {
		User currentUser = AuthzUtils.getUserDetails(User.class);
		userManager.lock(currentUser, LockMode.NONE);
		currentUser.setSex(user.getSex());
		currentUser.setBirthday(user.getBirthday());
		currentUser.setName(user.getName());
		currentUser.setAddress(user.getAddress());
		currentUser.setPostcode(user.getPostcode());
		currentUser.setPhone(user.getPhone());
		userManager.save(currentUser);
		addActionMessage(getText("save.success"));
		return "profile";
	}

	@InputConfig(methodName = "input")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.email", trim = true, key = "validation.required") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "user.email", key = "validation.invalid") })
	public String invite() {
		User currentUser = AuthzUtils.getUserDetails(User.class);
		SimpleMailMessage smm = new SimpleMailMessage();
		smm.setFrom(currentUser.getFriendlyName() + "<"
				+ currentUser.getEmail() + ">");
		smm.setTo(user.getEmail());
		smm.setSubject(getText("invite.subject", new String[] { user
				.getFriendlyName() }));
		Map<String, Object> model = new HashMap<String, Object>(1);
		String url = "/signup?email=" + user.getEmail();
		model.put("url", url);
		mailService.send(smm, "template/account_invite.ftl", model);
		addActionMessage(getText("operate.success"));
		return "invite";
	}

	private void sendActivationMail(User user) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("user", user);
		model.put("url", "/signup/activate/"
				+ Blowfish.encrypt(user.getId() + "," + user.getEmail()));
		SimpleMailMessage smm = new SimpleMailMessage();
		smm.setTo(user.getFriendlyName() + "<" + user.getEmail() + ">");
		smm.setSubject(getText("activation.mail.subject"));
		mailService.send(smm, "template/account_activate.ftl", model);
		addActionMessage(getText("operate.success"));
	}

}
