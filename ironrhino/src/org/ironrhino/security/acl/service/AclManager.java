package org.ironrhino.security.acl.service;

import java.util.List;

import org.ironrhino.core.service.BaseManager;
import org.ironrhino.security.acl.model.Acl;

public interface AclManager extends BaseManager<Acl> {

	public Acl findAcl(String username, String resource);

	public List<Acl> findAclsByUsername(String username);

}
