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
import org.ironrhino.core.spring.security.DefaultAuthenticationSuccessHandler;
import org.ironrhino.core.util.RequestUtils;
import org.ironrhino.security.model.LoginRecord;
import org.ironrhino.security.model.User;
import org.ironrhino.security.service.UserManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.core.Authentication;

@Singleton
@Named
public class AuthenticationSuccessHandler extends
		DefaultAuthenticationSuccessHandler {

	@Inject
	private UserManager userManager;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication)
			throws ServletException, IOException {
		super.onAuthenticationSuccess(request, response, authentication);
		User user = (User) authentication.getPrincipal();
		LoginRecord loginRecord = new LoginRecord();
		loginRecord.setUsername(user.getUsername());
		loginRecord.setAddress(RequestUtils.getRemoteAddr(request));
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
