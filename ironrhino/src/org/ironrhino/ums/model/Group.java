package org.ironrhino.ums.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.annotation.NaturalId;
import org.ironrhino.core.annotation.NotInCopy;
import org.ironrhino.core.annotation.Recordable;
import org.ironrhino.core.model.BaseEntity;


@Recordable
@AutoConfig
public class Group extends BaseEntity {
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

	public String toString() {
		return this.name;
	}

}
