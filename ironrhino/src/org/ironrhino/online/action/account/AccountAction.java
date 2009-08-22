package org.ironrhino.online.action.account;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.hibernate.LockMode;
import org.ironrhino.common.model.Region;
import org.ironrhino.common.support.RegionTreeControl;
import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.common.util.BeanUtils;
import org.ironrhino.common.util.CodecUtils;
import org.ironrhino.common.util.RequestUtils;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.mail.MailService;
import org.ironrhino.core.metadata.Captcha;
import org.ironrhino.core.metadata.Redirect;
import org.ironrhino.ums.model.User;
import org.ironrhino.ums.service.UserManager;
import org.ironrhino.ums.servlet.UserAuthenticationProcessingFilter;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.ui.AbstractProcessingFilter;
import org.springframework.security.ui.savedrequest.SavedRequest;
import org.springframework.security.userdetails.UserDetails;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork2.validator.annotations.RegexFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

public class AccountAction extends BaseAction {

	private static final long serialVersionUID = 5768065836650582081L;

	public static final String EMAIL_IN_SESSION = "email";

	protected static Log log = LogFactory.getLog(AccountAction.class);

	private User user;

	private String currentPassword;

	private String password;

	private String confirmPassword;

	private String error;

	private String username;

	private transient RegionTreeControl regionTreeControl;

	private transient UserManager userManager;

	private transient MailService mailService;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

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

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	public void setRegionTreeControl(RegionTreeControl regionTreeControl) {
		this.regionTreeControl = regionTreeControl;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
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
						.parseByHost(ServletActionContext.getRequest()
								.getRemoteAddr());
				if (region != null)
					user.setAddress(region.getFullname());
			}
		} else if ("email".equals(originalActionName)) {
			user = new User();
			user.setEmail(AuthzUtils.getUserDetails(User.class).getEmail());
		} else if ("signup".equals(originalActionName)) {
			String email = (String) ServletActionContext.getRequest()
					.getSession().getAttribute(EMAIL_IN_SESSION);
			if (email != null) {
				user = new User();
				user.setEmail(email);
				ServletActionContext.getRequest().getSession().removeAttribute(
						EMAIL_IN_SESSION);
			}
		}
		return originalMethod;
	}

	public String manage() {
		user = new User();
		BeanUtils.copyProperties(AuthzUtils.getUserDetails(User.class), user);
		if (StringUtils.isBlank(user.getAddress())) {
			Region region = regionTreeControl.parseByHost(ServletActionContext
					.getRequest().getRemoteAddr());
			if (region != null)
				user.setAddress(region.getFullname());
		}
		return "manage";
	}

	@SkipValidation
	public String login() {
		if (StringUtils.isNotBlank(error))
			addActionError(getText(error));
		HttpServletRequest request = ServletActionContext.getRequest();
		SavedRequest savedRequest = (SavedRequest) request
				.getSession()
				.getAttribute(
						AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY);
		if (savedRequest != null) {
			targetUrl = savedRequest.getFullRequestUrl();
			if (isUseJson())
				ServletActionContext
						.getRequest()
						.getSession()
						.removeAttribute(
								AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY);
		}
		if (StringUtils.isBlank(targetUrl))
			targetUrl = request.getHeader("Referer");
		username = RequestUtils.getCookieValue(request,
				UserAuthenticationProcessingFilter.USERNAME_IN_COOKIE);
		if (StringUtils.isNotBlank(username))
			username = CodecUtils.decode(username);
		return "login";
	}

	@Redirect
	@InputConfig(methodName = "input")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.email", trim = true, key = "validation.required") }, regexFields = { @RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "user.username", expression = "^\\w{3,20}$", key = "validation.invalid") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "user.email", key = "validation.invalid") }, fieldExpressions = { @FieldExpressionValidator(expression = "password == confirmPassword", fieldName = "confirmPassword", key = "confirmPassword.error") })
	public String signup() {
		if (user != null) {
			if (userManager.getByEmail(user.getEmail()) != null)
				addFieldError("user.email",
						getText("validation.already.exists"));
			else if (userManager.getByUsername(user.getUsername()) != null)
				addFieldError("user.username",
						getText("validation.already.exists"));
			if (hasErrors())
				return INPUT;
			if (StringUtils.isBlank(password))
				password = CodecUtils.randomString(10);
			user.setLegiblePassword(password);
			userManager.save(user);
			user.setPassword(password);// for send mail
			addActionMessage(getText("signup.success"));
			sendActivationMail(user);
		}
		targetUrl = "/user/profile";
		return REDIRECT;
	}

	private void sendActivationMail(User user) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("user", user);
		model.put("url", "/user/activate/"
				+ CodecUtils.encode(user.getId() + "," + user.getEmail()));
		SimpleMailMessage smm = new SimpleMailMessage();
		smm.setTo(user.getFriendlyName() + "<" + user.getEmail() + ">");
		smm.setSubject(getText("activation.mail.subject"));
		mailService.send(smm, "template/user_activate.ftl", model);
		addActionMessage(getText("operation.success"));
	}

	@InputConfig(methodName = "input")
	@Captcha
	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.username", trim = true, key = "validation.required"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.email", trim = true, key = "validation.required") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "user.email", key = "validation.invalid") })
	// if have not receive activate email,can retry it
	public String resend() {
		String email = user.getEmail();
		user = userManager.getByNaturalId("username", user.getUsername());
		if (user != null) {
			if (!email.equals(user.getEmail())) {
				addFieldError("user.email", getText("user.email.error"));
			} else {
				if (user.isEnabled())
					addActionError(getText("user.already.activated"));
				else
					sendActivationMail(user);
			}
		} else {
			addFieldError("user.username", getText("validation.not.exists"));
		}
		return "resend";
	}

	@SkipValidation
	public String activate() {
		String u = getUid();
		if (u != null) {
			String[] array = CodecUtils.decode(u).split(",");
			user = userManager.get(array[0]);
			if (user != null && !user.isEnabled()
					&& user.getEmail().equals(array[1])) {
				user.setEnabled(true);
				userManager.save(user);
				// auto login
				UserDetails ud = userManager.loadUserByUsername(user
						.getUsername());
				AuthzUtils.autoLogin(ud);
				return "activate";
			}
		}
		return ACCESSDENIED;
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
		smm.setSubject(getText("invite.subject",
				"your friend {1} invite you to join us", new String[] { user
						.getFriendlyName() }));
		Map<String, Object> model = new HashMap<String, Object>(1);
		String url = "/user/signup?user.email=" + user.getEmail();
		model.put("url", url);
		mailService.send(smm, "template/user_invite.ftl", model);
		addActionMessage(getText("operation.success"));
		return "invite";
	}

	@InputConfig(methodName = "input")
	@Captcha
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "user.email", trim = true, key = "validation.required") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "user.email", key = "validation.invalid") })
	public String forgot() {
		user = userManager.getByEmail(user.getEmail());
		if (user == null) {
			addActionError(getText("validation.not.exists"));
		} else {
			password = CodecUtils.randomString(10);
			user.setLegiblePassword(password);
			userManager.save(user);
			user.setPassword(password);
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("user", user);
			model.put("url", "/user/manage?tab=password");
			SimpleMailMessage smm = new SimpleMailMessage();
			smm.setTo(user.getFriendlyName() + "<" + user.getEmail() + ">");
			smm.setSubject("this is your username and password");
			mailService.send(smm, "template/user_forgot.ftl", model);
			addActionMessage(getText("operation.success"));
		}
		return "forgot";
	}

}
