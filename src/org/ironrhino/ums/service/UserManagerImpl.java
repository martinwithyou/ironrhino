package org.ironrhino.ums.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.SimpleElement;
import org.ironrhino.core.metadata.CheckCache;
import org.ironrhino.core.metadata.FlushCache;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.core.util.DateUtils;
import org.ironrhino.ums.model.User;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("userManager")
public class UserManagerImpl extends BaseManagerImpl<User> implements
		UserManager {

	@Override
	@Transactional
	@FlushCache(key = "${[args[0].username,args[0].email]}", namespace = "user")
	public void save(User user) {
		super.save(user);
	}

	@Override
	@Transactional
	@FlushCache(key = "${[args[0].username,args[0].email]}", namespace = "user")
	public void delete(User user) {
		super.delete(user);
	}

	@CheckCache(key = "${args[0]}", namespace = "user", onHit = "${org.ironrhino.core.monitor.Monitor.add({'cache','user','hit'})}", onMiss = "${org.ironrhino.core.monitor.Monitor.add({'cache','user','miss'})}")
	@Transactional(readOnly = true)
	public User loadUserByUsername(String username) {
		if (StringUtils.isEmpty(username))
			return null;
		User user;
		if (username.indexOf('@') > 0)
			user = getByEmail(username);
		else
			user = getByUsername(username);
		if (user == null)
			throw new UsernameNotFoundException("No such Username");
		populateAuthorities(user);
		return user;
	}

	private void populateAuthorities(User user) {
		List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
		auths.add(new GrantedAuthorityImpl(ROLE_BUILTIN_ANONYMOUS));
		auths.add(new GrantedAuthorityImpl(ROLE_BUILTIN_USER));
		for (SimpleElement sce : user.getRoles())
			auths.add(new GrantedAuthorityImpl(sce.getValue()));
		user.setAuthorities(auths.toArray(new GrantedAuthority[auths.size()]));
	}

	@CheckCache(key = "${args[0]}", namespace = "user", onHit = "${org.ironrhino.core.monitor.Monitor.add({'cache','user','hit'})}", onMiss = "${org.ironrhino.core.monitor.Monitor.add({'cache','user','miss'})}")
	@Transactional(readOnly = true)
	public User getByUsername(String username) {
		if (StringUtils.isEmpty(username))
			return null;
		return getByNaturalId(true, "username", username);
	}

	@CheckCache(key = "${args[0]}", namespace = "user", onHit = "${org.ironrhino.core.monitor.Monitor.add({'cache','user','hit'})}", onMiss = "${org.ironrhino.core.monitor.Monitor.add({'cache','user','miss'})}")
	@Transactional(readOnly = true)
	public User getByEmail(String email) {
		if (StringUtils.isEmpty(email))
			return null;
		return getByNaturalId(true, "email", email);
	}

	public String suggestName(String candidate) {
		// email
		if (candidate.indexOf('@') > 0)
			return candidate.replace('.', '-').replace('@', '-');
		// todo some other
		return candidate;
	}

	public boolean isActivationRequired(String email) {
		if (email.endsWith("@gmail.com"))
			return false;
		return true;
	}

	@Transactional
	public void deleteDisabled() {
		String hql = "delete from Account a where a.enabled = ? and a.createDate <= ?";
		bulkUpdate(hql,
				new Object[] { false, DateUtils.addDays(new Date(), -7) });
	}
}
