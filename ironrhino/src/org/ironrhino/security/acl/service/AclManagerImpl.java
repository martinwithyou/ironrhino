package org.ironrhino.security.acl.service;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.cache.CheckCache;
import org.ironrhino.core.cache.FlushCache;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.security.acl.model.Acl;
import org.springframework.transaction.annotation.Transactional;

@Singleton
@Named("aclManager")
public class AclManagerImpl extends BaseManagerImpl<Acl> implements AclManager {

	@Override
	@Transactional
	@FlushCache(namespace = "acl", key = "${args[0].role+args[0].resource}")
	public void delete(Acl acl) {
		super.delete(acl);
	}

	@Override
	@Transactional
	@FlushCache(namespace = "acl", key = "${args[0].role+args[0].resource}")
	public void save(Acl acl) {
		super.save(acl);
	}

	@Transactional(readOnly = true)
	@CheckCache(namespace = "acl", key = "${args[0]+args[1]}")
	public Acl findAcl(String role, String resource) {
		return findByNaturalId("role", role, "resource", resource);
	}

	public List<Acl> findAclsByRole(String role) {
		DetachedCriteria dc = detachedCriteria();
		dc.add(Restrictions.eq("role", role));
		return findListByCriteria(dc);
	}

}
