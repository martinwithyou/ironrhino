package org.ironrhino.ums.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.RecordAware;
import org.ironrhino.core.model.BaseEntity;

@RecordAware
@AutoConfig
public class Group extends BaseEntity {

	private static final long serialVersionUID = 4843366757458708882L;

	@NaturalId
	private String name;

	private boolean enabled;

	private String description;

	@NotInCopy
	private Set<Role> roles = new HashSet<Role>(0);

	public Group() {
		this.enabled = true;
	}

	public Group(String name) {
		this();
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	@NotInCopy
	public String getRolesAsString() {
		return StringUtils.join(roles.iterator(), ",");
	}

	@Override
	public String toString() {
		return this.name;
	}

}
