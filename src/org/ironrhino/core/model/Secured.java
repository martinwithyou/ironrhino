package org.ironrhino.core.model;

import java.util.Collection;

import org.ironrhino.common.model.SimpleElement;


public interface Secured {
	public Collection<SimpleElement> getRoles();

	public String getRolesAsString();

	public void setRolesAsString(String rolesAsString);
}
