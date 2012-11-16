package org.ironrhino.security.component;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.ironrhino.core.spring.security.DefaultAuthenticationFailureHandler;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.security.model.LoginRecord;
import org.ironrhino.security.service.UserManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Singleton
@Named
public class AuthenticationFailureHandler extends
		DefaultAuthenticationFailureHandler {

	@Inject
	private UserManager userManager;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException e)
			throws IOException, ServletException {
		super.onAuthenticationFailure(request, response, e);
		LoginRecord loginRecord = new LoginRecord();
		loginRecord
				.setUsername((String) request
						.getSession()
						.getAttribute(
								UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY));
		loginRecord.setAddress(RequestUtils.getRemoteAddr(request));
		loginRecord.setFailed(true);
		loginRecord.setCause(e.getMessage());
		save(loginRecord);
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