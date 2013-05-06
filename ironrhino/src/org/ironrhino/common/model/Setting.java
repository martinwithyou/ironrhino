package org.ironrhino.common.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.ironrhino.common.record.RecordAware;
import org.ironrhino.core.aop.PublishAware;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.CaseInsensitive;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Recordable;
import org.ironrhino.core.search.elasticsearch.annotations.Searchable;
import org.ironrhino.core.search.elasticsearch.annotations.SearchableProperty;
import org.springframework.security.core.userdetails.UserDetails;

@RecordAware
@PublishAware
@AutoConfig(searchable = true, order = "key asc")
@Searchable(type = "setting")
@Entity
@Table(name = "common_setting")
public class Setting extends BaseEntity implements Recordable<UserDetails> {

	private static final long serialVersionUID = -8352037603261222984L;

	@UiConfig(displayOrder = 1, width = "300px")
	@SearchableProperty(boost = 3)
	@CaseInsensitive
	@org.hibernate.annotations.NaturalId(mutable = true)
	@Column(name = "`key`", nullable = false)
	private String key = "";

	@UiConfig(displayOrder = 2, type = "textarea", width = "400px")
	@SearchableProperty
	private String value = "";

	@UiConfig(displayOrder = 3, type = "textarea")
	@SearchableProperty
	private String description = "";

	@NotInCopy
	@UiConfig(hidden = true)
	private Date createDate = new Date();

	@NotInCopy
	@UiConfig(hidden = true)
	private Date modifyDate;

	@NotInCopy
	@UiConfig(hidden = true)
	private String createUser;

	@NotInCopy
	@UiConfig(hidden = true)
	private String modifyUser;

	public Setting() {

	}

	public Setting(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(String modifyUser) {
		this.modifyUser = modifyUser;
	}

	public void setCreateUserDetails(UserDetails user) {
		if (user != null)
			createUser = user.getUsername();
	}

	public void setModifyUserDetails(UserDetails user) {
		if (user != null)
			modifyUser = user.getUsername();
	}

}
