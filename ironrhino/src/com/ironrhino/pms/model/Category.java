package com.ironrhino.pms.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.compass.annotations.Index;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.PublishAware;
import org.ironrhino.core.metadata.RecordAware;
import org.ironrhino.core.model.BaseTreeableEntity;
import org.ironrhino.core.model.Secured;
import org.ironrhino.core.model.SimpleElement;

@RecordAware
@PublishAware
@Searchable(root = false, alias = "category")
@AutoConfig
public class Category extends BaseTreeableEntity<Category> implements Secured {

	private static final long serialVersionUID = 6550078798855703442L;

	@NaturalId
	@SearchableProperty(index = Index.NOT_ANALYZED)
	private String code;

	private String description;

	private Set<SimpleElement> roles = new HashSet<SimpleElement>(0);

	public Category() {

	}

	public Category(String code) {
		this.code = code;
	}

	public String getCode() {
		return code != null ? code : name;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<SimpleElement> getRoles() {
		return roles;
	}

	public void setRoles(Set<SimpleElement> roles) {
		this.roles = roles;
	}

	public Category getDescendantOrSelfByCode(String code) {
		if (code == null)
			throw new IllegalArgumentException("code must not be null");
		if (code.equals(this.getCode()))
			return this;
		for (Category c : getChildren()) {
			if (code.equals(c.getCode())) {
				return c;
			} else {
				Category cc = c.getDescendantOrSelfByCode(code);
				if (cc != null)
					return cc;
			}
		}
		return null;
	}

	@NotInCopy
	public String getRolesAsString() {
		return StringUtils.join(roles.iterator(), ',');
	}

	public void setRolesAsString(String rolesAsString) {
		SimpleElement.fillCollectionWithString(roles, rolesAsString);
	}

	@Override
	@NotInCopy
	// @NotInJson
	public Collection<Category> getChildren() {
		return children;
	}

}
