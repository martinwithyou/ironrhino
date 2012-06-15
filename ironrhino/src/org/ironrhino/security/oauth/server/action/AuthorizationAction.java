package org.ironrhino.security.oauth.server.action;

import javax.inject.Inject;

import org.ironrhino.core.struts.EntityAction;
import org.ironrhino.security.oauth.server.model.Authorization;
import org.ironrhino.security.oauth.server.model.Client;
import org.ironrhino.security.oauth.server.service.OAuthManager;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

public class AuthorizationAction extends EntityAction {

	private static final long serialVersionUID = 2920367147774798742L;
	
	@Inject
	private OAuthManager oauthManager;

	private Authorization authorization;

	public Authorization getAuthorization() {
		return authorization;
	}

	public void setAuthorization(Authorization authorization) {
		this.authorization = authorization;
	}

	@InputConfig(methodName = "inputcreate")
	public String create() {
		if (authorization == null)
			return ACCESSDENIED;
		oauthManager.create(authorization);
		addActionMessage(getText("operate.success"));
		return SUCCESS;
	}

	public String inputcreate() {
		if (authorization == null)
			authorization = new Authorization();
		Client client = authorization.getClient();
		if (client != null) {
			client = getEntityManager(Client.class).get(client.getId());
			authorization.setClient(client);
			if (client.getOwner() != null)
				authorization.setGrantor(client.getOwner());
		}
		return "create";
	}

}
