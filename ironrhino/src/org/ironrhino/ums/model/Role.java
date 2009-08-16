package org.ironrhino.ums.model;

import java.util.HashSet;
import java.util.Set;

import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.RecordAware;
import org.ironrhino.core.model.BaseEntity;

@RecordAware
@AutoConfig
public class Role extends BaseEntity {

	@NaturalId
	private String name;

	private String description;

	private boolean enabled;

	@NotInCopy
	private Set<Group> groups = new HashSet<Group>(0);

	public Role() {
		this.enabled = true;
	}

	public Role(String name) {
		this();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Set<Group> getGroups() {
		return groups;
	}

	public void setGroups(Set<Group> groups) {
		this.groups = groups;
	}

	public String toString() {
		return this.name;
	}
}
