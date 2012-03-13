package org.ironrhino.security.oauth.server.action;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.spring.security.DefaultUsernamePasswordAuthenticationFilter;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.oauth.server.model.Authorization;
import org.ironrhino.security.oauth.server.model.Client;
import org.ironrhino.security.oauth.server.service.OAuthManager;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@AutoConfig(namespace = "/")
public class Oauth2Action extends BaseAction {

	private static final long serialVersionUID = 8175470892708878896L;

	@Inject
	private transient OAuthManager oauthManager;

	@Inject
	private transient DefaultUsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter;

	private String username;
	private String password;
	private String client_id;
	private String client_secret;
	private String redirect_uri;
	private String scope;
	private String code;
	private String response_type;
	private String grant_type;
	private String state;
	private Authorization authorization;
	private Client client;

	private Map<String, Object> tojson;
	private boolean displayForNative;
	private boolean granted;
	private boolean denied;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Map<String, Object> getTojson() {
		return tojson;
	}

	public Client getClient() {
		return client;
	}

	public Authorization getAuthorization() {
		return authorization;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getClient_id() {
		return client_id;
	}

	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}

	public String getClient_secret() {
		return client_secret;
	}

	public void setClient_secret(String client_secret) {
		this.client_secret = client_secret;
	}

	public String getGrant_type() {
		return grant_type;
	}

	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}

	public String getRedirect_uri() {
		return redirect_uri;
	}

	public void setRedirect_uri(String redirect_uri) {
		this.redirect_uri = redirect_uri;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getResponse_type() {
		return response_type;
	}

	public void setResponse_type(String response_type) {
		this.response_type = response_type;
	}

	public boolean isDisplayForNative() {
		return displayForNative;
	}

	public boolean isGranted() {
		return granted;
	}

	public boolean isDenied() {
		return denied;
	}

	public String execute() {
		return SUCCESS;
	}

	public String auth() {
		try {
			Client client = oauthManager.findClientById(client_id);
			if (client == null)
				throw new IllegalArgumentException("CLIENT_ID_INVALID");
			authorization = oauthManager.generate(client, redirect_uri, scope,
					response_type);
			client = authorization.getClient();
			displayForNative = client.isNative();
			setUid(authorization.getId());
		} catch (Exception e) {
			try {
				ServletActionContext.getResponse().sendError(
						HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return NONE;
		}
		return INPUT;
	}

	public String grant() {
		User grantor = AuthzUtils.getUserDetails(User.class);
		if (grantor == null) {
			HttpServletRequest request = ServletActionContext.getRequest();
			HttpServletResponse response = ServletActionContext.getResponse();
			Authentication authResult = null;
			try {
				authResult = usernamePasswordAuthenticationFilter
						.attemptAuthentication(request, response);
			} catch (AuthenticationException failed) {
				if (failed instanceof DisabledException)
					addFieldError("username", getText("user.disabled"));
				else if (failed instanceof LockedException)
					addFieldError("username", getText("user.locked"));
				else if (failed instanceof AccountExpiredException)
					addFieldError("username", getText("user.expired"));
				else if (failed instanceof BadCredentialsException)
					addFieldError("password", getText("user.bad.credentials"));
				else if (failed instanceof CredentialsExpiredException)
					addFieldError("password", getText("user.bad.expired"));
				captchaManager.addCaptachaThreshold(request);
				try {
					usernamePasswordAuthenticationFilter.unsuccess(request,
							response, failed);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return INPUT;
			}
			if (authResult != null)
				try {
					usernamePasswordAuthenticationFilter.success(request,
							response, authResult);
					grantor = (User) authResult.getPrincipal();
				} catch (Exception e) {
					e.printStackTrace();
				}

		}
		try {
			authorization = oauthManager.grant(getUid(), grantor);
			displayForNative = authorization.getClient().isNative();
			granted = true;
			if (displayForNative) {
				return INPUT;
			} else {
				StringBuilder sb = new StringBuilder(redirect_uri);
				if (authorization.isClientSide()) {
					sb.append("#");
					sb.append("access_token=").append(
							authorization.getAccessToken());
					sb.append("&expires_in=").append(
							authorization.getExpiresIn());
				} else {
					sb.append(sb.indexOf("?") > 0 ? "&" : "?").append("code=")
							.append(authorization.getCode());
				}
				if (StringUtils.isNotBlank(state))
					try {
						sb.append("&state=").append(
								URLEncoder.encode(state, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				targetUrl = sb.toString();
			}
		} catch (Exception e) {
			try {
				ServletActionContext.getResponse().sendError(
						HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
				return NONE;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return REDIRECT;
	}

	public String deny() {
		oauthManager.deny(getUid());
		denied = true;
		if (Client.OAUTH_OOB.equals(redirect_uri)) {
			displayForNative = true;
			return INPUT;
		}
		StringBuilder sb = new StringBuilder(redirect_uri);
		sb.append(sb.indexOf("?") > 0 ? "&" : "?")
				.append("error=access_denied");
		if (StringUtils.isNotBlank(state))
			try {
				sb.append("&state=").append(URLEncoder.encode(state, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		targetUrl = sb.toString();
		return REDIRECT;
	}

	@JsonConfig(root = "tojson")
	public String token() {
		if (!"authorization_code".equals(grant_type))
			try {
				ServletActionContext.getResponse().sendError(
						HttpServletResponse.SC_BAD_REQUEST,
						"grant_type must be authorization_code");
				return NONE;
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		Client client = new Client();
		client.setId(client_id);
		client.setSecret(client_secret);
		client.setRedirectUri(redirect_uri);
		try {
			authorization = oauthManager.authenticate(code, client);
			tojson = new HashMap<String, Object>();
			tojson.put("access_token", authorization.getAccessToken());
			tojson.put("expires_in", authorization.getExpiresIn());
			tojson.put("refresh_token", authorization.getRefreshToken());
		} catch (Exception e) {
			try {
				ServletActionContext.getResponse().sendError(
						HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return NONE;
		}
		return JSON;
	}

}
