package org.ironrhino.security.oauth.server.service;

import java.util.List;

import org.ironrhino.security.model.User;
import org.ironrhino.security.oauth.server.model.Authorization;
import org.ironrhino.security.oauth.server.model.Client;

public interface OAuthManager {

	public Authorization generate(String clientId, String redirectUri,
			String scope, String responseType) throws Exception;

	public Authorization grant(String authorizationId, User grantor)
			throws Exception;

	public void deny(String authorizationId);

	public Authorization authenticate(String code, Client client)
			throws Exception;

	public Authorization retrieve(String accessToken);

	public Authorization refresh(String refreshToken);

	public void revoke(String accessToken);

	public List<Authorization> findAuthorizationsByGrantor(User grantor);

	public void removeExpired();

}
