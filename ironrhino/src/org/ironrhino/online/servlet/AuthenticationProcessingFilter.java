package org.ironrhino.online.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.ironrhino.common.util.CodecUtils;
import org.ironrhino.common.util.RequestUtils;
import org.ironrhino.core.aspect.AopContext;
import org.ironrhino.core.aspect.CacheAspect;
import org.ironrhino.online.model.Account;
import org.ironrhino.online.model.LoginRecord;
import org.ironrhino.online.service.AccountManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;

public class AuthenticationProcessingFilter extends
		org.springframework.security.ui.webapp.AuthenticationProcessingFilter {

	public final static String USERNAME_IN_COOKIE = "UIC";

	private AccountManager accountManager;

	public void setAccountManager(AccountManager accountManager) {
		this.accountManager = accountManager;
	}

	@Override
	protected void onSuccessfulAuthentication(HttpServletRequest request,
			HttpServletResponse response, Authentication authResult)
			throws IOException {
		String username = authResult.getName();
		username = CodecUtils.encode(username);
		RequestUtils.saveCookie(request, response, USERNAME_IN_COOKIE,
				username, 365 * 24 * 3600);
		Account account = (Account) authResult.getPrincipal();
		account.setLoginTimes(account.getLoginTimes() + 1);
		account.setLastLoginDate(new Date());
		account.setLastLoginAddress(request.getRemoteAddr());
		AopContext.setBypass(CacheAspect.class);
		accountManager.save(account);

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

	private void save(final LoginRecord loginRecord) {
		accountManager.execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				session.save(loginRecord);
				return null;
			}
		});
	}

}
