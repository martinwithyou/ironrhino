package org.ironrhino.security.action;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.brickred.socialauth.AbstractProvider;
import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.AuthProviderFactory;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.provider.OpenIdImpl;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.security.Constants;
import org.ironrhino.security.component.SocialauthConsumerAssociationStore;
import org.ironrhino.security.model.User;
import org.ironrhino.security.service.UserManager;
import org.openid4java.consumer.ConsumerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@AutoConfig(namespace = "/")
public class SocialauthAction extends BaseAction {

	private static final long serialVersionUID = 8175406892708878896L;

	protected static Logger log = LoggerFactory
			.getLogger(SocialauthAction.class);

	@Inject
	private transient UserManager userManager;

	@Inject
	private transient SettingControl settingControl;

	public String execute() {
		if (!settingControl.getBooleanValue(
				Constants.SETTING_KEY_SOCIALAUTH_ENABLED, false))
			return ACCESSDENIED;
		try {
			HttpServletRequest request = ServletActionContext.getRequest();
			AuthProvider provider = AuthProviderFactory.getInstance(getUid());
			if (provider instanceof OpenIdImpl) {
				OpenIdImpl openIdImpl = (OpenIdImpl) provider;
				Field field = OpenIdImpl.class.getDeclaredField("manager");
				field.setAccessible(true);
				ConsumerManager manager = (ConsumerManager) field
						.get(openIdImpl);
				manager.setAssociations(new SocialauthConsumerAssociationStore());
			}
			String returnToUrl = "/socialauth/auth";
			if (StringUtils.isNotBlank(targetUrl))
				returnToUrl += "?targetUrl="
						+ URLEncoder.encode(targetUrl, "UTF-8");
			targetUrl = provider.getLoginRedirectURL(RequestUtils
					.getBaseUrl(request)
					+ returnToUrl);
			request.getSession().setAttribute("socialauth", getUid());
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
			AuthProvider provider = (AuthProvider) AuthProviderFactory
					.getInstance((String) request.getSession().getAttribute(
							"socialauth"));
			request.getSession().removeAttribute("socialauth");
			if (provider instanceof OpenIdImpl) {
				OpenIdImpl openIdImpl = (OpenIdImpl) provider;
				Field field = OpenIdImpl.class.getDeclaredField("manager");
				field.setAccessible(true);
				ConsumerManager manager = (ConsumerManager) field
						.get(openIdImpl);
				manager.setAssociations(new SocialauthConsumerAssociationStore());
			}
			if (provider instanceof AbstractProvider) {
				Method m = AbstractProvider.class.getDeclaredMethod(
						"setProviderState", new Class[] { Boolean.TYPE });
				m.setAccessible(true);
				m.invoke(provider, true);
			}
			Profile p = provider.verifyResponse(request);
			if (p == null)
				return ACCESSDENIED;
			String id = p.getValidatedId();
			User user = null;
			try {
				user = (User) userManager.loadUserByUsername(id);
			} catch (UsernameNotFoundException e) {
				user = new User();
				user.setUsername(userManager.suggestUsername(id));
				user.setLegiblePassword(CodecUtils.randomString(10));
				try {
					if (userManager.loadUserByUsername(p.getEmail()) == null)
						user.setEmail(p.getEmail());
				} catch (UsernameNotFoundException e1) {
					user.setEmail(p.getEmail());
				}
				user.setName(p.getFullName());
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
