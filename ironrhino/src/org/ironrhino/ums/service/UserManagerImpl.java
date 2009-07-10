package org.ironrhino.ums.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.SimpleElement;
import org.ironrhino.core.cache.CheckCache;
import org.ironrhino.core.cache.FlushCache;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.ums.model.Group;
import org.ironrhino.ums.model.Role;
import org.ironrhino.ums.model.User;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public class UserManagerImpl extends BaseManagerImpl<User> implements
		UserManager {

	@Transactional
	@FlushCache("user_${args[0].username}")
	public void save(User user) {
		super.save(user);
	}

	@Transactional(readOnly = true)
	@CheckCache("user_${args[0]}")
	public User loadUserByUsername(String username) {
		User user = getUserByUsername(username);
		if (user == null)
			throw new UsernameNotFoundException("No such Username");
		populateAuthorities(user);
		return user;
	}

	public User getUserByUsername(String username) {
		return getByNaturalId(true, "username", username);
	}

	private void populateAuthorities(User user) {
		Collection<Role> roles = new HashSet<Role>();
		List<String> names = new ArrayList<String>();
		for (SimpleElement sce : user.getRoles())
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
		for (SimpleElement sce : user.getGroups())
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
		auths.add(new GrantedAuthorityImpl(ROLE_BUILTIN_USER));
		for (Role role : roles)
			if (role.isEnabled())
				auths.add(new GrantedAuthorityImpl(role.getName()));
		user.setAuthorities(auths.toArray(new GrantedAuthority[auths.size()]));
	}

}
