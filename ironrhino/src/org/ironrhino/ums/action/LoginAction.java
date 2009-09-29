package org.ironrhino.ums.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.Captcha;
import org.ironrhino.core.metadata.Redirect;
import org.ironrhino.core.session.Constants;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.RequestUtils;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

@AutoConfig(namespace = "/")
public class LoginAction extends BaseAction {

	private static final long serialVersionUID = 2783386542815083811L;

	private String password;

	private String error;

	private String username;

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Redirect
	@InputConfig(methodName = INPUT)
	@Captcha(threshold = 3)
	public String execute() {
		HttpServletRequest request = ServletActionContext.getRequest();
		if (StringUtils.isNotBlank(error)) {
			addFieldError("password", getText(error));
			captchaManager.addCaptachaThreshold(request);
		}
		return SUCCESS;
	}

	public String input() {
		HttpServletRequest request = ServletActionContext.getRequest();
		if (StringUtils.isBlank(targetUrl))
			targetUrl = request.getHeader("Referer");
		username = RequestUtils.getCookieValue(request,
				Constants.COOKIE_NAME_LOGIN_USER);
		return SUCCESS;
	}

}
