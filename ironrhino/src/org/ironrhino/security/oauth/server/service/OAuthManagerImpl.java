package org.ironrhino.security.oauth.server.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.metadata.DefaultProfile;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.oauth.server.model.Authorization;
import org.ironrhino.security.oauth.server.model.Client;
import org.springframework.scheduling.annotation.Scheduled;

@Singleton
@Named("oauthManager")
@DefaultProfile
public class OAuthManagerImpl implements OAuthManager {

	@Inject
	private BaseManager baseManager;

	private long expireTime = DEFAULT_EXPIRE_TIME;

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public Authorization generate(Client client, String redirectUri,
			String scope, String responseType) {
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

	public Authorization reuse(Authorization auth) {
		auth.setCode(CodecUtils.nextId());
		auth.setModifyDate(new Date());
		baseManager.save(auth);
		return auth;
	}

	public Authorization grant(String authorizationId, User grantor) {
		baseManager.setEntityClass(Authorization.class);
		Authorization auth = (Authorization) baseManager.get(authorizationId);
		if (auth == null)
			throw new IllegalArgumentException("BAD_AUTH");
		auth.setGrantor(grantor);
		auth.setModifyDate(new Date());
		if (!auth.isClientSide())
			auth.setCode(CodecUtils.nextId());
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
			if (auth.getLifetime() < 0)
				return null;
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

	@Scheduled(cron = "0 30 23 * * ?")
	public void removeExpired() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, (int) (-expireTime));
		baseManager.executeUpdate(
				"delete from Authorization a where a.modifyDate < ?",
				cal.getTime());
	}

	public void saveClient(Client client) {
		baseManager.save(client);
	}

	public void deleteClient(Client client) {
		baseManager.delete(client);
	}

	public Client findClientById(String clientId) {
		baseManager.setEntityClass(Client.class);
		return (Client) baseManager.get(clientId);
	}

	public List<Client> findClientByOwner(User owner) {
		baseManager.setEntityClass(Client.class);
		DetachedCriteria dc = baseManager.detachedCriteria();
		dc.add(Restrictions.eq("owner", owner));
		dc.addOrder(Order.asc("createDate"));
		return baseManager.findListByCriteria(dc);
	}
}
