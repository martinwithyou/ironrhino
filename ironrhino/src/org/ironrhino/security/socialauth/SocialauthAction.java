package org.ironrhino.security.socialauth;

import java.net.URLEncoder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.security.Constants;
import org.ironrhino.security.model.User;
import org.ironrhino.security.service.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@AutoConfig(namespace = "/")
public class SocialauthAction extends BaseAction {

	private static final long serialVersionUID = 8175406892708878896L;

	private static final String SESSION_KEY_SOCIAL = "_social";

	protected static Logger log = LoggerFactory
			.getLogger(SocialauthAction.class);

	@Inject
	private transient UserManager userManager;

	@Inject
	private transient SettingControl settingControl;

	@Inject
	private transient AuthProviderManager authProviderManager;

	public String execute() {
		if (!settingControl.getBooleanValue(
				Constants.SETTING_KEY_SOCIALAUTH_ENABLED, false))
			return ACCESSDENIED;
		try {
			HttpServletRequest request = ServletActionContext.getRequest();
			AuthProvider provider = authProviderManager.lookup(getUid());
			String returnToUrl = "/socialauth/auth";
			if (StringUtils.isNotBlank(targetUrl))
				returnToUrl += "?targetUrl="
						+ URLEncoder.encode(targetUrl, "UTF-8");
			targetUrl = provider.getLoginRedirectURL(request, RequestUtils
					.getBaseUrl(request)
					+ returnToUrl);
			request.getSession().setAttribute(SESSION_KEY_SOCIAL, getUid());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return REDIRECT;
	}

	public String auth() {
		if (!settingControl.getBooleanValue(
				Constants.SETTING_KEY_SOCIALAUTH_ENABLED, false))
			return ACCESSDENIED;
		HttpServletRequest request = ServletActionContext.getRequest();
		try {
			AuthProvider provider = (AuthProvider) authProviderManager
					.lookup((String) request.getSession().getAttribute(
							SESSION_KEY_SOCIAL));
			request.getSession().removeAttribute(SESSION_KEY_SOCIAL);

			Profile p = provider.getProfile(request);
			if (p == null)
				return ACCESSDENIED;
			String id = p.getId();
			User user = null;
			try {
				user = (User) userManager.loadUserByUsername(id);
			} catch (UsernameNotFoundException e) {
				user = new User();
				user.setUsername(userManager.suggestUsername(id));
				user.setLegiblePassword(CodecUtils.randomString(10));
				if (StringUtils.isNotBlank(p.getEmail()))
					try {
						if (userManager.loadUserByUsername(p.getEmail()) == null)
							user.setEmail(p.getEmail());
					} catch (UsernameNotFoundException e1) {
						user.setEmail(p.getEmail());
					}
				user.setName(StringUtils.isNotBlank(p.getName()) ? p.getName()
						: p.getDisplayName());
				if (id.indexOf("://") > 0)
					user.setOpenid(id);
				userManager.save(user);
			}
			if (user != null)
				AuthzUtils.autoLogin(user);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return REDIRECT;
	}
}
