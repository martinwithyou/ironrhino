package org.ironrhino.core.session.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.cache.CacheManager;
import org.ironrhino.core.security.util.Blowfish;
import org.ironrhino.core.session.Constants;
import org.ironrhino.core.session.HttpWrappedSession;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.Authentication;
import org.springframework.security.context.HttpSessionContextIntegrationFilter;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component("sessionManager")
public class CacheBasedSessionManager extends AbstractSessionManager {

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private UserDetailsService userDetailsService;

	public void doInitialize(HttpWrappedSession session) {
		Map attrMap = (Map) cacheManager.get(session.getId(),
				Constants.CACHE_NAMESPACE);
		String username = RequestUtils.getCookieValue(session.getHttpContext()
				.getRequest(), Constants.COOKIE_NAME_ENCRYPT_LOGIN_USER);
		if (StringUtils.isNotBlank(username)
				&& (attrMap == null || !attrMap
						.containsKey(HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY))) {
			username = Blowfish.decrypt(username);

			UserDetails ud = null;
			try {
				ud = userDetailsService.loadUserByUsername(username);
			} catch (UsernameNotFoundException e) {
				RequestUtils.deleteCookie(
						session.getHttpContext().getRequest(), session
								.getHttpContext().getResponse(),
						Constants.COOKIE_NAME_ENCRYPT_LOGIN_USER, true);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
			if (ud != null) {
				SecurityContext sc = new SecurityContextImpl();
				Authentication auth = new UsernamePasswordAuthenticationToken(
						ud, ud.getPassword(), ud.getAuthorities());
				sc.setAuthentication(auth);
				if (attrMap == null)
					attrMap = new HashMap<String, Object>();
				attrMap
						.put(
								HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY,
								sc);
			}
		}
		if (attrMap != null && attrMap.size() > 0)
			session.setAttrMap(attrMap);
	}

	public void doSave(HttpWrappedSession session) {
		Map attrMap = session.getAttrMap();
		if (attrMap != null)
			attrMap
					.remove(HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY);
		if (attrMap != null && attrMap.size() > 0)
			cacheManager.put(session.getId(), (Serializable) session
					.getAttrMap(), session.getMaxInactiveInterval(), -1,
					Constants.CACHE_NAMESPACE);
	}

	public void doInvalidate(HttpWrappedSession session) {
		cacheManager.delete(session.getId(), Constants.CACHE_NAMESPACE);
	}

}
