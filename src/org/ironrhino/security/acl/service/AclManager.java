package org.ironrhino.security.acl.service;

import java.util.List;

import org.ironrhino.core.service.BaseManager;
import org.ironrhino.security.acl.model.Acl;

public interface AclManager extends BaseManager<Acl> {

	public void delete(Acl acl) ;
	
	public void save(Acl acl);

	public Acl findAcl(String role, String resource);

	public List<Acl> findAclsByRole(String role);

}
