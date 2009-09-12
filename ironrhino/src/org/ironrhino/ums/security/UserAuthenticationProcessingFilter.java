package org.ironrhino.ums.security;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.ironrhino.common.util.CodecUtils;
import org.ironrhino.common.util.RequestUtils;
import org.ironrhino.core.aop.AopContext;
import org.ironrhino.core.aop.CacheAspect;
import org.ironrhino.core.security.Blowfish;
import org.ironrhino.core.session.Constants;
import org.ironrhino.ums.model.LoginRecord;
import org.ironrhino.ums.model.User;
import org.ironrhino.ums.service.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilterEntryPoint;

public class UserAuthenticationProcessingFilter extends
		AuthenticationProcessingFilter {

	@Autowired
	private UserManager userManager;

	@Autowired
	private AuthenticationProcessingFilterEntryPoint entryPoint;

	@Override
	protected void onSuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, Authentication authResult)
			throws IOException {
		String username = authResult.getName();
		RequestUtils.saveCookie(request, response,
				Constants.COOKIE_NAME_LOGIN_USER, username, 365 * 24 * 3600,
				true);
		RequestUtils.saveCookie(request, response,
				Constants.COOKIE_NAME_ENCRYPT_LOGIN_USER, Blowfish
						.encrypt(username), true);
		username = CodecUtils.encode(username);
		User user = (User) authResult.getPrincipal();
		user.setLoginTimes(user.getLoginTimes() + 1);
		user.setLastLoginDate(new Date());
		user.setLastLoginAddress(request.getRemoteAddr());
		AopContext.setBypass(CacheAspect.class);
		userManager.save(user);
		LoginRecord loginRecord = new LoginRecord();
		loginRecord.setUsername(username);
		loginRecord.setLoginAddress(request.getRemoteAddr());
		save(loginRecord);
	}

	@Override
	protected void onUnsuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException failed)
			throws IOException {
		LoginRecord loginRecord = new LoginRecord();
		loginRecord.setUsername((String) request.getSession().getAttribute(
				SPRING_SECURITY_LAST_USERNAME_KEY));
		loginRecord.setLoginAddress(request.getRemoteAddr());
		loginRecord.setFailed(true);
		loginRecord.setCause(failed.getMessage());
		save(loginRecord);
	}

	protected String determineTargetUrl(HttpServletRequest request) {
		return entryPoint.getLoginFormUrl();
	}

	protected void sendRedirect(HttpServletRequest request,
			HttpServletResponse response, String url) throws IOException {
		try {
			request.getRequestDispatcher(url).forward(request, response);
		} catch (ServletException e) {
			e.printStackTrace();
		}
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
