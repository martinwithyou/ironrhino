package org.ironrhino.online.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.SimpleElement;
import org.ironrhino.common.support.RegionTreeControl;
import org.ironrhino.common.util.DateUtils;
import org.ironrhino.core.cache.CheckCache;
import org.ironrhino.core.cache.FlushCache;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.online.model.Account;
import org.ironrhino.ums.model.Group;
import org.ironrhino.ums.model.Role;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public class AccountManagerImpl extends BaseManagerImpl<Account> implements
		AccountManager {

	private RegionTreeControl regionTreeControl;

	private Random random;

	public AccountManagerImpl() {
		random = new Random();
	}

	public void setRegionTreeControl(RegionTreeControl regionTreeControl) {
		this.regionTreeControl = regionTreeControl;
	}

	@Transactional
	@FlushCache("account_${args[0].username},account_${args[0].email},account_${args[0].openid}")
	public void save(Account account) {
		if (regionTreeControl != null && account.getRegion() == null)
			account.setRegion(regionTreeControl.parseByAddress(account
					.getAddress()));
		super.save(account);
	}

	@Transactional(readOnly = true)
	@CheckCache("account_${args[0]}")
	public Account loadUserByUsername(String username) {
		if (StringUtils.isEmpty(username))
			return null;
		Account account;
		if (username.indexOf('@') > 0)
			account = getAccountByEmail(username);
		else if (username.indexOf("://") > 0)
			account = getAccountByOpenid(username);
		else
			account = getAccountByUsername(username);
		if (account == null)
			throw new UsernameNotFoundException("No such Username");
		populateAuthorities(account);
		return account;
	}

	private void populateAuthorities(Account account) {
		Collection<Role> roles = new HashSet<Role>();
		List<String> names = new ArrayList<String>();
		for (SimpleElement sce : account.getRoles())
			names.add(sce.getValue());
		if (names.size() > 0) {
			final DetachedCriteria dc = DetachedCriteria.forClass(Role.class);
			dc.add(Restrictions.in("name", names));
			Collection<Role> userRoles = (Collection<Role>) execute(new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					return dc.getExecutableCriteria(session).list();
				}
			});
			roles.addAll(userRoles);
			names.clear();
		}
		for (SimpleElement sce : account.getGroups())
			names.add(sce.getValue());
		if (names.size() > 0) {
			final DetachedCriteria dc = DetachedCriteria.forClass(Group.class);
			dc.add(Restrictions.in("name", names));
			Collection<Group> userGroups = (Collection<Group>) execute(new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					return dc.getExecutableCriteria(session).list();
				}
			});
			for (Group group : userGroups)
				if (group.isEnabled())
					roles.addAll(group.getRoles());
		}
		List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
		auths.add(new GrantedAuthorityImpl(ROLE_BUILTIN_ANONYMOUS));
		auths.add(new GrantedAuthorityImpl(ROLE_BUILTIN_ACCOUNT));
		for (Role role : roles)
			if (role.isEnabled())
				auths.add(new GrantedAuthorityImpl(role.getName()));
		account.setAuthorities(auths
				.toArray(new GrantedAuthority[auths.size()]));
	}

	@CheckCache("account_${args[0]}")
	public Account getAccountByUsername(String username) {
		if (StringUtils.isEmpty(username))
			return null;
		return getByNaturalId(true, "username", username);
	}

	@CheckCache("account_${args[0]}")
	public Account getAccountByEmail(String email) {
		if (StringUtils.isEmpty(email))
			return null;
		return getByNaturalId(true, "email", email);
	}

	@CheckCache("account_${args[0]}")
	public Account getAccountByOpenid(String openid) {
		if (StringUtils.isEmpty(openid))
			return null;
		return getByNaturalId(true, "openid", openid);
	}

	@Transactional(readOnly = true)
	public String suggestUsername(String email) {
		String username = null;
		if (StringUtils.isNotBlank(email)) {
			username = email.substring(0, email.indexOf('@'));
			// if username contains non word char?
		} else
			username = "random" + random.nextInt(100);
		int i = 0;
		while (getAccountByUsername(username) != null)
			username = username + (++i);
		return username;
	}

	@Transactional
	public void deleteDisabledAccount() {
		String hql = "delete from Account a where a.enabled = ? and a.createDate <= ?";
		bulkUpdate(hql,
				new Object[] { false, DateUtils.addDays(new Date(), -7) });
	}
}
