package org.ironrhino.security.oauth.server.service;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.core.util.DateUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.oauth.server.model.Authorization;
import org.ironrhino.security.oauth.server.model.Client;

@Singleton
@Named("oauthManager")
public class OAuthManagerImpl implements OAuthManager {

	@Inject
	private BaseManager baseManager;

	public Authorization generate(String clientId, String redirectUri,
			String scope, String responseType) {
		baseManager.setEntityClass(Client.class);
		Client client = (Client) baseManager.get(clientId);
		if (client == null)
			throw new IllegalArgumentException("CLIENT_ID_INVALID");
		if (!client.supportsRedirectUri(redirectUri))
			throw new IllegalArgumentException("REDIRECT_URI_MISMATCH");
		Authorization auth = new Authorization();
		auth.setClient(client);
		if (StringUtils.isNotBlank(scope))
			auth.setScope(scope);
		if (StringUtils.isNotBlank(responseType))
			auth.setResponseType(responseType);
		baseManager.save(auth);
		return auth;
	}

	public Authorization grant(String authorizationId, User grantor) {
		baseManager.setEntityClass(Authorization.class);
		Authorization auth = (Authorization) baseManager.get(authorizationId);
		if (auth == null)
			throw new IllegalArgumentException("BAD_AUTH");
		if (auth.isClientSide()) {
			auth.setAccessToken(CodecUtils.nextId());
		} else {
			auth.setCode(CodecUtils.nextId());
		}
		auth.setGrantor(grantor);
		baseManager.save(auth);
		return auth;
	}

	public void deny(String authorizationId) {
		baseManager.setEntityClass(Authorization.class);
		Authorization auth = (Authorization) baseManager.get(authorizationId);
		if (auth != null)
			baseManager.delete(auth);
	}

	public Authorization authenticate(String code, Client client) {
		baseManager.setEntityClass(Authorization.class);
		Authorization auth = (Authorization) baseManager.findByNaturalId(
				"code", code);
		if (auth == null)
			throw new IllegalArgumentException("CODE_INVALID");
		if (auth.isClientSide())
			throw new IllegalArgumentException("NOT_SERVER_SIDE");
		if (auth.getGrantor() == null)
			throw new IllegalArgumentException("USER_NOT_GRANTED");
		Client orig = auth.getClient();
		if (!orig.getId().equals(client.getId()))
			throw new IllegalArgumentException("CLIENT_ID_MISMATCH");
		if (!orig.getSecret().equals(client.getSecret()))
			throw new IllegalArgumentException("CLIENT_SECRET_MISMATCH");
		if (!orig.supportsRedirectUri(client.getRedirectUri()))
			throw new IllegalArgumentException("REDIRECT_URI_MISMATCH");
		auth.setCode(null);
		auth.setRefreshToken(CodecUtils.nextId());
		auth.setModifyDate(new Date());
		baseManager.save(auth);
		return auth;
	}

	public Authorization retrieve(String accessToken) {
		baseManager.setEntityClass(Authorization.class);
		Authorization auth = (Authorization) baseManager
				.findByNaturalId(accessToken);
		if (auth != null) {
			if (!auth.getClient().isEnabled()) {
				baseManager.delete(auth);
				return null;
			}
			if (auth.getExpiresIn() > 0) {
				long offset = System.currentTimeMillis()
						- auth.getModifyDate().getTime();
				if (offset > auth.getExpiresIn() * 1000)
					return null;
			}
		}
		return auth;
	}

	public Authorization refresh(String refreshToken) {
		baseManager.setEntityClass(Authorization.class);
		Authorization auth = (Authorization) baseManager.findByNaturalId(
				"refreshToken", refreshToken);
		if (auth != null) {
			if (!auth.getClient().isEnabled()) {
				baseManager.delete(auth);
				return null;
			}
			auth.setAccessToken(CodecUtils.nextId());
			auth.setModifyDate(new Date());
			baseManager.save(auth);
		}
		return auth;
	}

	public void revoke(String accessToken) {
		baseManager.setEntityClass(Authorization.class);
		Authorization auth = (Authorization) baseManager
				.findByNaturalId(accessToken);
		if (auth != null)
			baseManager.delete(auth);
	}

	public List<Authorization> findAuthorizationsByGrantor(User grantor) {
		baseManager.setEntityClass(Authorization.class);
		DetachedCriteria dc = baseManager.detachedCriteria();
		dc.add(Restrictions.eq("grantor", grantor));
		dc.addOrder(Order.desc("modifyDate"));
		return baseManager.findListByCriteria(dc);
	}

	public void removeExpired() {
		baseManager.executeUpdate(
				"delete from Authorization a where a.modifyDate < ?",
				DateUtils.addDays(new Date(), -7));
	}

}
