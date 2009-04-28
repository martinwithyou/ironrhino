package org.ironrhino.online.action.account;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.hibernate.LockMode;
import org.ironrhino.common.model.Region;
import org.ironrhino.common.support.RegionTreeControl;
import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.common.util.CodecUtils;
import org.ironrhino.common.util.RequestUtils;
import org.ironrhino.core.annotation.Captcha;
import org.ironrhino.core.annotation.Redirect;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.mail.MailService;
import org.ironrhino.core.util.BeanUtils;
import org.ironrhino.online.model.Account;
import org.ironrhino.online.service.AccountManager;
import org.ironrhino.online.servlet.AuthenticationProcessingFilter;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.ui.AbstractProcessingFilter;
import org.springframework.security.ui.savedrequest.SavedRequest;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.EmailValidator;
import com.opensymphony.xwork2.validator.annotations.FieldExpressionValidator;
import com.opensymphony.xwork2.validator.annotations.RegexFieldValidator;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

public class AccountAction extends BaseAction {

	public static final String TARGET_URL_IN_SESSION = "targetUrl";
	public static final String OPENID_DISCOVERY_IN_SESSION = "openid_discovry";
	public static final String OPENID_IN_SESSION = "openid";
	public static final String EMAIL_IN_SESSION = "email";
	public static final String OPENID_IN_COOKIE = "OIC";

	protected Log log = LogFactory.getLog(getClass());

	private RegionTreeControl regionTreeControl;

	private AccountManager accountManager;

	private MailService mailService;

	private Account account;

	private String currentPassword;

	private String password;

	private String confirmPassword;

	private String error;

	private String username;

	private String openid;

	private AuthRequest authRequest;

	private ConsumerManager consumerManager;

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

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

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public AuthRequest getAuthRequest() {
		return authRequest;
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

	public void setAccountManager(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	public void setConsumerManager(ConsumerManager consumerManager) {
		this.consumerManager = consumerManager;
	}

	public String execute() {
		return SUCCESS;
	}

	public String input() {
		if ("profile".equals(originalActionName)) {
			account = new Account();
			BeanUtils.copyProperties(AuthzUtils.getUserDetails(Account.class),
					account);
			if (StringUtils.isBlank(account.getAddress())) {
				Region region = regionTreeControl
						.parseByHost(ServletActionContext.getRequest()
								.getRemoteAddr());
				if (region != null)
					account.setAddress(region.getFullname());
			}
		} else if ("email".equals(originalActionName)) {
			account = new Account();
			account.setEmail(AuthzUtils.getUserDetails(Account.class)
					.getEmail());
		} else if ("signup".equals(originalActionName)) {
			String email = (String) ServletActionContext.getRequest()
					.getSession().getAttribute(EMAIL_IN_SESSION);
			if (email != null) {
				account = new Account();
				account.setEmail(email);
				ServletActionContext.getRequest().getSession().removeAttribute(
						EMAIL_IN_SESSION);
			}
		}
		return originalMethod;
	}

	public String manage() {
		account = new Account();
		BeanUtils.copyProperties(AuthzUtils.getUserDetails(Account.class),
				account);
		if (StringUtils.isBlank(account.getAddress())) {
			Region region = regionTreeControl.parseByHost(ServletActionContext
					.getRequest().getRemoteAddr());
			if (region != null)
				account.setAddress(region.getFullname());
		}
		return "manage";
	}

	@SkipValidation
	public String login() {
		if (StringUtils.isNotBlank(error))
			addActionError(getText(error));
		HttpServletRequest request = ServletActionContext.getRequest();
		SavedRequest savedRequest = (SavedRequest) request.getSession()
				.getAttribute(AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY);
		if (savedRequest != null) {
			targetUrl = savedRequest.getFullRequestUrl();
			if (isUseJson())
				ServletActionContext.getRequest().getSession().removeAttribute(
						AbstractProcessingFilter.SPRING_SECURITY_SAVED_REQUEST_KEY);
		}
		if (StringUtils.isBlank(targetUrl))
			targetUrl = request.getHeader("Referer");
		username = RequestUtils.getCookieValue(request,
				AuthenticationProcessingFilter.USERNAME_IN_COOKIE);
		if (StringUtils.isNotBlank(username))
			username = CodecUtils.decode(username);
		openid = RequestUtils.getCookieValue(request, OPENID_IN_COOKIE);
		if (StringUtils.isNotBlank(openid))
			openid = CodecUtils.decode(openid);
		return "login";
	}

	@Redirect
	@InputConfig(methodName="input")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "account.email", trim = true, key = "account.email.required", message = "请输入email") }, regexFields = { @RegexFieldValidator(type = ValidatorType.FIELD, fieldName = "account.username", expression = "^\\w{3,20}$", key = "account.username.invalid", message = "username不合法") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "account.email", key = "account.email.invalid", message = "请输入正确的email") }, fieldExpressions = { @FieldExpressionValidator(expression = "password == confirmPassword", fieldName = "confirmPassword", key = "confirmPassword.error", message = "两次输入密码不一致") })
	public String signup() {
		if (account != null) {
			if (accountManager.getAccountByEmail(account.getEmail()) != null)
				addFieldError("account.email", getText("account.email.exists"));
			if (StringUtils.isBlank(account.getUsername()))
				account.setUsername(accountManager.suggestUsername(account
						.getEmail()));
			else if (accountManager.getAccountByUsername(account.getUsername()) != null)
				addFieldError("account.username",
						getText("account.username.exists"));
			if (hasErrors())
				return INPUT;
			if (StringUtils.isBlank(password))
				password = CodecUtils.randomString(10);
			account.setLegiblePassword(password);
			String openid = (String) ServletActionContext.getRequest()
					.getSession().getAttribute(OPENID_IN_SESSION);
			if (openid != null) {
				account.setOpenid(openid);
				ServletActionContext.getRequest().getSession().removeAttribute(
						OPENID_IN_SESSION);
			}
			accountManager.save(account);
			account.setPassword(password);// for send mail
			addActionMessage(getText("signup.success"));
			sendActivationMail(account);
		}
		targetUrl = "/account/profile";
		return REDIRECT;
	}

	private void sendActivationMail(Account account) {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("account", account);
		model
				.put("url", "/account/activate/"
						+ CodecUtils.encode(account.getId() + ","
								+ account.getEmail()));
		SimpleMailMessage smm = new SimpleMailMessage();
		smm.setTo(account.getFriendlyName() + "<" + account.getEmail() + ">");
		smm.setSubject(getText("activation.mail.subject",
				"activate your account"));
		mailService.send(smm, "account_activate.ftl", model);
		addActionMessage(getText("activation.mail.send.success"));
	}

	@InputConfig(methodName="input")
	@Captcha
	@Validations(requiredStrings = {
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "account.username", trim = true, key = "account.username.required", message = "请输入用户名"),
			@RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "account.email", trim = true, key = "account.email.required", message = "请输入email") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "account.email", key = "account.email.invalid", message = "请输入正确的email") })
	// if have not receive activate email,can retry it
	public String resend() {
		String email = account.getEmail();
		account = accountManager.getByNaturalId("username", account
				.getUsername());
		if (account != null) {
			if (!email.equals(account.getEmail())) {
				addFieldError("account.email", getText("account.email.error"));
			} else {
				if (account.isEnabled())
					addActionError(getText("account.already.activated"));
				else
					sendActivationMail(account);
			}
		} else {
			addFieldError("account.username",
					getText("account.username.nonexist"));
		}
		return "resend";
	}

	@SkipValidation
	public String activate() {
		String u = getUid();
		if (u != null) {
			String[] array = CodecUtils.decode(u).split(",");
			account = accountManager.get(array[0]);
			if (account != null && !account.isEnabled()
					&& account.getEmail().equals(array[1])) {
				account.setEnabled(true);
				accountManager.save(account);
				// auto login
				UserDetails ud = accountManager.loadUserByUsername(account
						.getUsername());
				AuthzUtils.autoLogin(ud);	
				return "activate";
			}
		}
		return ACCESSDENIED;
	}

	@InputConfig(methodName="input")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "password", trim = true, key = "password.required", message = "请输入6-20位密码") }, stringLengthFields = { @StringLengthFieldValidator(type = ValidatorType.FIELD, trim = true, minLength = "6", maxLength = "20", fieldName = "password", key = "password.required", message = "密码的长度为6-20") }, fieldExpressions = { @FieldExpressionValidator(expression = "password == confirmPassword", fieldName = "confirmPassword", key = "confirmPassword.error", message = "两次输入密码不一致") })
	public String password() {
		Account currentAccount = AuthzUtils.getUserDetails(Account.class);
		if (!currentAccount.isPasswordValid(currentPassword)) {
			addFieldError("currentPassword", getText("currentPassword.error"));
			return INPUT;
		}
		currentAccount.setLegiblePassword(password);
		log.info("'" + currentAccount.getUsername() + "' edited password");
		accountManager.save(currentAccount);
		addActionMessage(getText("password.edit.success"));
		return "password";
	}

	@InputConfig(methodName="input")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "account.email", trim = true, key = "account.email.required", message = "请输入email") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "account.email", key = "account.email.invalid", message = "请输入正确的email") })
	public String email() {
		Account currentAccount = AuthzUtils.getUserDetails(Account.class);
		if (!currentAccount.getEmail().equalsIgnoreCase(account.getEmail())) {
			if (!currentAccount.isPasswordValid(currentPassword)) {
				addFieldError("currentPassword",
						getText("currentPassword.error"));
				return INPUT;
			}
			Account acc = accountManager.getAccountByEmail(account.getEmail());
			if (acc != null) {
				addActionError(getText("account.email.exists"));
				return SUCCESS;
			} else {
				currentAccount.setEmail(account.getEmail());
				currentAccount.setEnabled(false);
				accountManager.save(currentAccount);
				addActionMessage(getText("account.email.modified"));
				sendActivationMail(currentAccount);
			}
		} else {
			addActionError(getText("account.email.not.modified"));
		}
		return "email";
	}

	@InputConfig(methodName="input")
	public String unbindopenid() {
		Account currentAccount = AuthzUtils.getUserDetails(Account.class);
		if (!currentAccount.isPasswordValid(currentPassword)) {
			addFieldError("currentPassword", getText("currentPassword.error"));
			return INPUT;
		}
		currentAccount.setOpenid(null);
		accountManager.save(currentAccount);
		return "unbindopenid";
	}

	@InputConfig(methodName="input")
	public String profile() {
		Account currentAccount = AuthzUtils.getUserDetails(Account.class);
		accountManager.lock(currentAccount, LockMode.NONE);
		currentAccount.setNickname(account.getNickname());
		currentAccount.setSex(account.getSex());
		currentAccount.setBirthDate(account.getBirthDate());
		currentAccount.setName(account.getName());
		currentAccount.setAddress(account.getAddress());
		currentAccount.setZip(account.getZip());
		currentAccount.setTelephone(account.getTelephone());
		currentAccount.setSubscribed(account.isSubscribed());
		accountManager.save(currentAccount);
		addActionMessage(getText("profile.edit.success"));
		return "profile";
	}

	@InputConfig(methodName="input")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "account.email", trim = true, key = "account.email.required", message = "请输入email") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "account.email", key = "account.email.invalid", message = "请输入正确的email") })
	public String invite() {
		Account currentAccount = AuthzUtils.getUserDetails(Account.class);
		SimpleMailMessage smm = new SimpleMailMessage();
		smm.setFrom(currentAccount.getFriendlyName() + "<"
				+ currentAccount.getEmail() + ">");
		smm.setTo(account.getEmail());
		smm.setSubject(getText("invite.subject",
				"your friend {1} invite you to join us", new String[] { account
						.getFriendlyName() }));
		Map<String, Object> model = new HashMap<String, Object>(1);
		String url = "/account/signup?account.email=" + account.getEmail();
		model.put("url", url);
		mailService.send(smm, "account_invite.ftl", model);
		addActionMessage(getText("invite.successfully"));
		return "invite";
	}

	@InputConfig(methodName="input")
	@Captcha
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "account.email", trim = true, key = "account.email.required", message = "请输入email") }, emails = { @EmailValidator(type = ValidatorType.FIELD, fieldName = "account.email", key = "account.email.invalid", message = "请输入正确的email") })
	public String forgot() {
		account = accountManager.getAccountByEmail(account.getEmail());
		if (account == null) {
			addActionError(getText("account.email.nonexists"));
		} else {
			password = CodecUtils.randomString(10);
			account.setLegiblePassword(password);
			accountManager.save(account);
			account.setPassword(password);
			Map<String, Object> model = new HashMap<String, Object>();
			model.put("account", account);
			model.put("url", "/account/manage?tab=password");
			SimpleMailMessage smm = new SimpleMailMessage();
			smm.setTo(account.getFriendlyName() + "<" + account.getEmail()
					+ ">");
			smm.setSubject("this is your username and password");
			mailService.send(smm, "account_forgot.ftl", model);
			addActionMessage(getText("find.success"));
		}
		return "forgot";
	}

	@Redirect
	public String openid() throws IOException {
		if (ServletActionContext.getRequest().getParameter("openid.mode") != null)
			return openidCallback();
		if (username != null)
			return openidBind();
		else
			return openidCheck();
	}

	public String openidBind() throws IOException {
		account = (Account) accountManager.loadUserByUsername(username);
		if (account == null || !account.isPasswordValid(password)) {
			addActionError(getText("username.password.not.matched"));
			return INPUT;
		}
		openid = (String) ServletActionContext.getRequest().getSession()
				.getAttribute(OPENID_IN_SESSION);
		if (openid != null) {
			account.setOpenid(openid);
			ServletActionContext.getRequest().getSession().removeAttribute(
					OPENID_IN_SESSION);
		}
		accountManager.save(account);
		doLogin();
		return "signup";
	}

	public String openidCheck() throws IOException {
		HttpServletRequest request = ServletActionContext.getRequest();
		String returnToUrl = RequestUtils.getBaseUrl(request, true)
				+ "/account/openid";
		if (StringUtils.isNotBlank(targetUrl))
			request.getSession().setAttribute(TARGET_URL_IN_SESSION, targetUrl);
		try {
			List discoveries = consumerManager.discover(openid);
			DiscoveryInformation discovered = consumerManager
					.associate(discoveries);
			request.getSession().setAttribute(OPENID_DISCOVERY_IN_SESSION,
					discovered);
			authRequest = consumerManager.authenticate(discovered, returnToUrl);
			FetchRequest fetch = FetchRequest.createFetchRequest();
			fetch.addAttribute("email",
					"http://schema.openid.net/contact/email", true);
			fetch.addAttribute("nickname",
					"http://schema.openid.net/contact/nickname", false);
			fetch.addAttribute("fullname",
					"http://schema.openid.net/contact/fullname", false);
			authRequest.addExtension(fetch);
			// if (!discovered.isVersion2()) {
			targetUrl = authRequest.getDestinationUrl(true);
			return REDIRECT;
			// } else {
			// use form redirect
			// return "openid-form-redirect";
			// }
		} catch (OpenIDException e) {
			addActionError(getText("openid.invalid"));
			return "openid";
		}

	}

	public String openidCallback() throws IOException {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();
		try {
			ParameterList pl = new ParameterList(request.getParameterMap());
			DiscoveryInformation discovered = (DiscoveryInformation) request
					.getSession().getAttribute(OPENID_DISCOVERY_IN_SESSION);
			request.getSession().removeAttribute(OPENID_DISCOVERY_IN_SESSION);
			StringBuffer receivingURL = request.getRequestURL();
			String queryString = request.getQueryString();
			if (queryString != null && queryString.length() > 0)
				receivingURL.append("?").append(request.getQueryString());
			VerificationResult verification = consumerManager.verify(
					receivingURL.toString(), pl, discovered);
			if (verification != null) {
				Identifier verified = verification.getVerifiedId();
				if (verified != null) {
					openid = verified.getIdentifier();
					RequestUtils.saveCookie(request, response,
							OPENID_IN_COOKIE, CodecUtils.encode(discovered
									.getClaimedIdentifier().getIdentifier()),
							365 * 24 * 3600);
					AuthSuccess authSuccess = (AuthSuccess) verification
							.getAuthResponse();
					String email = null;
					if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
						FetchResponse fetchResp = (FetchResponse) authSuccess
								.getExtension(AxMessage.OPENID_NS_AX);
						List emails = fetchResp.getAttributeValues("email");
						email = (String) emails.get(0);
					}
					try {
						account = (Account) accountManager
								.loadUserByUsername(openid);
					} catch (UsernameNotFoundException e) {
					}
					if (account == null) {
						request.getSession().setAttribute(OPENID_IN_SESSION,
								openid);
						request.getSession().setAttribute(EMAIL_IN_SESSION,
								email);
						targetUrl = "/account/signup";
					} else {
						if (!account.isEnabled() || account.isLocked())
							return ACCESSDENIED;
						doLogin();
					}
					return REDIRECT;
				}else{
					addActionError(getText("openid.login.failed"));
					return "openid";
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			addActionError(getText("openid.invalid"));
		}
		return ERROR;
	}

	private void doLogin() {
		HttpServletRequest request = ServletActionContext.getRequest();
		AuthzUtils.autoLogin(account);
		accountManager.lock(account, LockMode.NONE);
		account.setLoginTimes(account.getLoginTimes() + 1);
		account.setLastLoginDate(new Date());
		account.setLastLoginAddress(request.getRemoteAddr());
		accountManager.save(account);
		targetUrl = (String) request.getSession().getAttribute(
				TARGET_URL_IN_SESSION);
		request.getSession().removeAttribute(TARGET_URL_IN_SESSION);
		if (StringUtils.isBlank(targetUrl))
			targetUrl = request.getContextPath();
	}
}
