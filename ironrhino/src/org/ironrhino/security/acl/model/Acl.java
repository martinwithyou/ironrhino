package org.ironrhino.security.acl.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.NaturalId;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.CaseInsensitive;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.security.model.UserRole;

@AutoConfig(order = "role asc")
@Authorize(ifAllGranted = UserRole.ROLE_ADMINISTRATOR)
@Entity
@Table(name = "acl")
public class Acl extends BaseEntity {

	private static final long serialVersionUID = 7186455276739721437L;

	@UiConfig(displayOrder = 1)
	@CaseInsensitive
	@NaturalId(mutable = true)
	@Column(length = 50, nullable = false)
	private String role;

	@UiConfig(displayOrder = 2)
	@CaseInsensitive
	@NaturalId(mutable = true)
	@Column(name = "`resource`", length = 150, nullable = false)
	private String resource;

	@UiConfig(displayOrder = 3)
	private boolean permitted;

	public Acl() {
	}

	public Acl(String role, String resource, boolean permitted) {
		this.role = role;
		this.resource = resource;
		this.permitted = permitted;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public boolean isPermitted() {
		return permitted;
	}

	public void setPermitted(boolean permitted) {
		this.permitted = permitted;
	}

}
