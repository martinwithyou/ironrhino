package org.ironrhino.security.oauth.client.action;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.security.Constants;
import org.ironrhino.security.model.User;
import org.ironrhino.security.oauth.client.model.OAuthToken;
import org.ironrhino.security.oauth.client.model.Profile;
import org.ironrhino.security.oauth.client.service.OAuthProvider;
import org.ironrhino.security.oauth.client.service.OAuthProviderManager;
import org.ironrhino.security.service.UserManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.fasterxml.jackson.core.type.TypeReference;

@AutoConfig
public class ConnectAction extends BaseAction {

	private static final long serialVersionUID = 8175406892708878896L;

	private List<OAuthProvider> providers;

	@Inject
	private transient UserManager userManager;

	@Inject
	private transient SettingControl settingControl;

	@Inject
	private transient OAuthProviderManager oauthProviderManager;

	public List<OAuthProvider> getProviders() {
		return providers;
	}

	public String execute() {
		if (!isEnabled())
			return ACCESSDENIED;
		String id = getUid();
		if (StringUtils.isBlank(id)) {
			providers = oauthProviderManager.getProviders();
			return SUCCESS;
		} else {
			HttpServletRequest request = ServletActionContext.getRequest();
			try {
				OAuthProvider provider = (OAuthProvider) oauthProviderManager
						.lookup(getUid());
				if (provider == null)
					return ACCESSDENIED;
				StringBuilder sb = new StringBuilder(
						RequestUtils.getBaseUrl(request));
				sb.append("/oauth/connect/auth/").append(provider.getName());
				if (StringUtils.isNotBlank(targetUrl))
					sb.append("?targetUrl=").append(
							URLEncoder.encode(targetUrl, "UTF-8"));
				targetUrl = provider.getAuthRedirectURL(request, sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return REDIRECT;
		}
	}

	public String auth() {
		if (!isEnabled())
			return ACCESSDENIED;
		HttpServletRequest request = ServletActionContext.getRequest();
		try {
			OAuthProvider provider = (OAuthProvider) oauthProviderManager
					.lookup(getUid());
			if (provider == null)
				return ACCESSDENIED;
			User loginUser = AuthzUtils.getUserDetails(User.class);
			if (loginUser == null) {
				Profile p = provider.getProfile(request);
				if (p == null) {
					targetUrl = "/oauth/connect";
					return REDIRECT;
				}
				String id = p.getUid();
				User user = null;
				try {
					user = (User) userManager.loadUserByUsername(id);
					if (user != null)
						loginUser = user;
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
					user.setName(StringUtils.isNotBlank(p.getName()) ? p
							.getName() : p.getDisplayName());
					user.setAttribute(
							OAuthProvider.USER_ATTRIBUTE_NAME_PROVIDER,
							provider.getName());
					Map<String, String> tokens = new HashMap<String, String>();
					tokens.put(provider.getName(), provider.getToken(request)
							.getSource());
					user.setAttribute(OAuthProvider.USER_ATTRIBUTE_NAME_TOKENS,
							JsonUtils.toJson(tokens));
					userManager.save(user);
				}
				if (user != null)
					AuthzUtils.autoLogin(user);
			}
			if (loginUser != null) {
				OAuthToken token = provider.getToken(request);
				if (token != null) {
					String str = loginUser
							.getAttribute(OAuthProvider.USER_ATTRIBUTE_NAME_TOKENS);
					Map<String, String> tokens;
					if (StringUtils.isNotBlank(str)) {
						tokens = JsonUtils.fromJson(str,
								new TypeReference<Map<String, String>>() {
								});
					} else
						tokens = new HashMap<String, String>();
					tokens.put(provider.getName(), token.getSource());
					loginUser.setAttribute(
							OAuthProvider.USER_ATTRIBUTE_NAME_TOKENS,
							JsonUtils.toJson(tokens));
					userManager.save(loginUser);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// OAuth2.0
		String state = request.getParameter("state");
		if (StringUtils.isNotBlank(state))
			targetUrl = state;
		if (StringUtils.isBlank(targetUrl))
			targetUrl = "/";
		return REDIRECT;
	}

	private boolean isEnabled() {
		return settingControl.getBooleanValue(
				Constants.SETTING_KEY_SIGNUP_ENABLED, false)
				&& settingControl.getBooleanValue(
						Constants.SETTING_KEY_OAUTH_ENABLED, false);
	}
}
