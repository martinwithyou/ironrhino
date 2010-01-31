package org.ironrhino.ums.security;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.ironrhino.core.aop.AopContext;
import org.ironrhino.core.cache.CacheAspect;
import org.ironrhino.core.spring.security.DefaultAuthenticationFailureHandler;
import org.ironrhino.core.spring.security.DefaultAuthenticationSuccessHandler;
import org.ironrhino.core.spring.security.DefaultUsernamePasswordAuthenticationFilter;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.ums.model.LoginRecord;
import org.ironrhino.ums.model.User;
import org.ironrhino.ums.service.UserManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class AuthenticationFilter extends
		DefaultUsernamePasswordAuthenticationFilter {

	public final static String COOKIE_NAME_LOGIN_USER = "U";

	@Inject
	private UserManager userManager;

	public AuthenticationFilter() {
		setAuthenticationSuccessHandler(new DefaultAuthenticationSuccessHandler() {

			@Override
			public void onAuthenticationSuccess(HttpServletRequest request,
					HttpServletResponse response, Authentication authentication)
					throws ServletException, IOException {
				super
						.onAuthenticationSuccess(request, response,
								authentication);
				String username = authentication.getName();
				if (request.isRequestedSessionIdFromCookie())
					RequestUtils.saveCookie(request, response,
							COOKIE_NAME_LOGIN_USER, username, 365 * 24 * 3600,
							true);
				User user = (User) authentication.getPrincipal();
				user.setLoginTimes(user.getLoginTimes() + 1);
				user.setLastLoginDate(new Date());
				user.setLastLoginAddress(RequestUtils.getRemoteAddr(request));
				AopContext.setBypass(CacheAspect.class);
				userManager.save(user);
				LoginRecord loginRecord = new LoginRecord();
				loginRecord.setUsername(user.getUsername());
				loginRecord
						.setLoginAddress(RequestUtils.getRemoteAddr(request));
				save(loginRecord);
			}

		});
		setAuthenticationFailureHandler(new DefaultAuthenticationFailureHandler() {

			@Override
			public void onAuthenticationFailure(HttpServletRequest request,
					HttpServletResponse response, AuthenticationException e)
					throws IOException, ServletException {
				super.onAuthenticationFailure(request, response, e);
				LoginRecord loginRecord = new LoginRecord();
				loginRecord.setUsername((String) request.getSession()
						.getAttribute(SPRING_SECURITY_LAST_USERNAME_KEY));
				loginRecord
						.setLoginAddress(RequestUtils.getRemoteAddr(request));
				loginRecord.setFailed(true);
				loginRecord.setCause(e.getMessage());
				save(loginRecord);
			}

		});
	}

	private void save(final LoginRecord loginRecord) {
		userManager.execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				session.save(loginRecord);
				return null;
			}
		});
	}

}
