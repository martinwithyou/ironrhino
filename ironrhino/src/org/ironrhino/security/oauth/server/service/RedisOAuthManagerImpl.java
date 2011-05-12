package org.ironrhino.security.oauth.server.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.oauth.server.model.Authorization;
import org.ironrhino.security.oauth.server.model.Client;
import org.springframework.data.keyvalue.redis.core.RedisTemplate;


public class RedisOAuthManagerImpl implements OAuthManager {

	@Inject
	private RedisTemplate<String, Authorization> redisTemplate;

	@Inject
	@Named("stringRedisTemplate")
	private RedisTemplate<String, String> stringRedisTemplate;

	private static final String namespace = "oauth:authorization:";

	private long expireTime = DEFAULT_EXPIRE_TIME;

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setRedisTemplate(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public Authorization generate(Client client, String redirectUri,
			String scope, String responseType) {
		if (!client.supportsRedirectUri(redirectUri))
			throw new IllegalArgumentException("REDIRECT_URI_MISMATCH");
		Authorization auth = new Authorization();
		auth.setId(CodecUtils.nextId());
		auth.setClient(client);
		if (StringUtils.isNotBlank(scope))
			auth.setScope(scope);
		if (StringUtils.isNotBlank(responseType))
			auth.setResponseType(responseType);
		redisTemplate.opsForValue().set(namespace + auth.getId(), auth,
				expireTime, TimeUnit.SECONDS);
		return auth;
	}

	public Authorization grant(String authorizationId, User grantor) {
		String key = namespace + authorizationId;
		Authorization auth = redisTemplate.opsForValue().get(key);
		if (auth == null)
			throw new IllegalArgumentException("BAD_AUTH");
		auth.setGrantor(grantor);
		auth.setModifyDate(new Date());
		if (auth.isClientSide()) {
			redisTemplate.delete(key);
			redisTemplate.opsForValue().set(namespace + auth.getAccessToken(),
					auth, expireTime, TimeUnit.SECONDS);
			stringRedisTemplate.opsForValue().set(
					namespace + auth.getRefreshToken(),
					namespace + auth.getAccessToken());
		} else {
			auth.setCode(CodecUtils.nextId());
			redisTemplate.delete(key);
			redisTemplate.opsForValue().set(namespace + auth.getCode(), auth,
					expireTime, TimeUnit.SECONDS);
		}
		stringRedisTemplate.opsForList().leftPush(
				new StringBuilder(namespace).append("grantor:")
						.append(auth.getGrantor().getUsername()).toString(),
				namespace + auth.getAccessToken());
		return auth;
	}

	public void deny(String authorizationId) {
		redisTemplate.delete(namespace + authorizationId);
	}

	public Authorization authenticate(String code, Client client) {
		String key = namespace + code;
		Authorization auth = redisTemplate.opsForValue().get(key);
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
		redisTemplate.delete(key);
		redisTemplate.opsForValue().set(namespace + auth.getAccessToken(),
				auth, expireTime, TimeUnit.SECONDS);
		stringRedisTemplate.opsForValue().set(
				namespace + auth.getRefreshToken(),
				namespace + auth.getAccessToken());
		return auth;
	}

	public Authorization retrieve(String accessToken) {
		String key = namespace + accessToken;
		Authorization auth = redisTemplate.opsForValue().get(key);
		redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
		if (auth != null) {
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
		String keyRefreshToken = namespace + refreshToken;
		Authorization auth = redisTemplate.opsForValue().get(
				stringRedisTemplate.opsForValue().get(keyRefreshToken));
		String keyAccessToken = namespace + auth.getAccessToken();
		redisTemplate.delete(keyAccessToken);
		auth.setAccessToken(CodecUtils.nextId());
		auth.setModifyDate(new Date());
		redisTemplate.opsForValue().set(namespace + auth.getAccessToken(),
				auth, expireTime, TimeUnit.SECONDS);
		stringRedisTemplate.opsForValue().set(
				namespace + auth.getRefreshToken(),
				namespace + auth.getAccessToken());
		return auth;
	}

	public void revoke(String accessToken) {
		String key = namespace + accessToken;
		Authorization auth = redisTemplate.opsForValue().get(key);
		redisTemplate.delete(key);
		redisTemplate.delete(namespace + auth.getRefreshToken());
		stringRedisTemplate.opsForList().remove(
				new StringBuilder(namespace).append("grantor:")
						.append(auth.getGrantor().getUsername()).toString(), 0,
				namespace + accessToken);
	}

	public List<Authorization> findAuthorizationsByGrantor(User grantor) {
		String keyForList = new StringBuilder(namespace).append("grantor:")
				.append(grantor.getUsername()).toString();
		List<String> tokens = stringRedisTemplate.opsForList().range(
				keyForList, 0, -1);
		if (tokens == null || tokens.isEmpty())
			return Collections.EMPTY_LIST;
		return redisTemplate.opsForValue().multiGet(tokens);
	}

}
