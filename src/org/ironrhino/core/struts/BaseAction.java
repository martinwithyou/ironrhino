package org.ironrhino.core.struts;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.Captcha;
import org.ironrhino.core.metadata.Csrf;
import org.ironrhino.core.security.captcha.CaptchaManager;
import org.ironrhino.core.security.csrf.CsrfManager;
import org.ironrhino.core.util.AuthzUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.annotations.Before;
import com.opensymphony.xwork2.interceptor.annotations.BeforeResult;
import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

public class BaseAction extends ActionSupport {

	private static final long serialVersionUID = -3183957331611790404L;

	public static final String LIST = "list";
	public static final String VIEW = "view";
	public static final String REFERER = "referer";
	public static final String JSON = "json";
	public static final String REDIRECT = "redirect";
	public static final String FEED = "feed";
	public static final String SUGGEST = "suggest";
	public static final String ACCESSDENIED = "accessDenied";

	private boolean returnInput;

	// logic id or natrual id
	private String[] id;

	protected String originalActionName;

	protected String originalMethod;

	protected String targetUrl;

	protected boolean captchaRequired;

	private boolean firstReachCaptchaThreshold;

	protected String csrf;

	protected boolean csrfRequired;

	@Autowired
	protected transient CaptchaManager captchaManager;

	@Autowired
	protected transient CsrfManager csrfManager;

	public String getCsrf() {
		if (csrfRequired && csrf == null)
			csrf = csrfManager.createToken(ServletActionContext.getRequest());
		return csrf;
	}

	public boolean isCsrfRequired() {
		return csrfRequired;
	}

	public boolean isCaptchaRequired() {
		return captchaRequired;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public String getUid() {
		if (id != null && id.length > 0)
			return id[0];
		else
			return null;
	}

	public void setUid(String id) {
		this.id = new String[] { id };
	}

	public void setId(String[] id) {
		this.id = id;
	}

	public String[] getId() {
		return id;
	}

	public boolean isUseJson() {
		HttpServletRequest request = ServletActionContext.getRequest();
		return (request.getHeader("Accept") != null && request.getHeader(
				"Accept").indexOf("json") > -1)
				|| JSON.equalsIgnoreCase(request.getParameter("_result_type_"));
	}

	public boolean isAjax() {
		return "XMLHttpRequest".equalsIgnoreCase(ServletActionContext
				.getRequest().getHeader("X-Requested-With"))
				|| StringUtils.isNotEmpty(getAjaxTransport());
	}

	public String getAjaxTransport() {
		return ServletActionContext.getRequest().getParameter(
				"_transport_type_");
	}

	@Override
	public String execute() {
		return list();
	}

	@Override
	public String input() {
		return INPUT;
	}

	public String list() {
		return NONE;
	}

	public String save() {
		return NONE;
	}

	public String create() {
		return save();
	}

	public String update() {
		return save();
	}

	public String view() {
		return NONE;
	}

	public String delete() {
		return NONE;
	}

	@Before(priority = 20)
	public String preAction() throws Exception {
		Authorize authorize = getAnnotation(Authorize.class);
		if (authorize == null)
			authorize = getClass().getAnnotation(Authorize.class);
		if (authorize != null) {
			boolean passed = AuthzUtils.authorize(authorize.ifAllGranted(),
					authorize.ifAnyGranted(), authorize.ifNotGranted(),
					authorize.expression());
			if (!passed) {
				getActionErrors().add(getText("access.denied"));
				return ACCESSDENIED;
			}
		}
		Captcha captcha = getAnnotation(Captcha.class);
		if (captcha != null) {
			boolean[] array = captchaManager.isCaptchaRequired(
					ServletActionContext.getRequest(), captcha);
			captchaRequired = array[0];
			firstReachCaptchaThreshold = array[1];
		}
		csrfRequired = !captchaRequired && getAnnotation(Csrf.class) != null;
		return null;
	}

	@Before(priority = 10)
	public String returnInput() throws Exception {
		InputConfig inputConfig = getAnnotation(InputConfig.class);
		if (inputConfig == null)
			return null;
		if (!"POST".equalsIgnoreCase(ServletActionContext.getRequest()
				.getMethod())) {
			returnInput = true;
			if (!inputConfig.methodName().equals("")) {
				ActionInvocation ai = ActionContext.getContext()
						.getActionInvocation();
				originalActionName = ai.getProxy().getActionName();
				originalMethod = ai.getProxy().getMethod();
				// ai.getProxy().setMethod(annotation.methodName());
				Method method = this.getClass().getMethod(
						inputConfig.methodName());
				return (String) method.invoke(this);
			} else {
				return inputConfig.resultName();
			}
		} else {
			return null;
		}
	}

	@Override
	public void validate() {
		if (captchaRequired && !firstReachCaptchaThreshold
				&& !captchaManager.validate(ServletActionContext.getRequest()))
			addFieldError(CaptchaManager.KEY_CAPTCHA, getText("captcha.error"));
		if (csrfRequired
				&& !csrfManager
						.validateToken(ServletActionContext.getRequest()))
			addActionError(getText("csrf.error"));
	}

	@BeforeResult
	public void preResult() throws Exception {
		if (StringUtils.isNotBlank(targetUrl) && !hasErrors()) {
			targetUrl = ServletActionContext.getResponse().encodeRedirectURL(
					targetUrl);
			ServletActionContext.getResponse().setHeader("X-Redirect-To",
					targetUrl);
		}
		if (!(returnInput || !isAjax()
				|| (captchaRequired && firstReachCaptchaThreshold) || !(isUseJson() || hasErrors())))
			ActionContext.getContext().getActionInvocation()
					.setResultCode(JSON);
	}

	private <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		Method method = BeanUtils.findMethod(getClass(), ActionContext
				.getContext().getActionInvocation().getProxy().getMethod(),
				null);
		if (method == null)
			return null;
		return method.getAnnotation(annotationClass);
	}

}