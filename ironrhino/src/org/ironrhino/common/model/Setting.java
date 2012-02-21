package org.ironrhino.common.model;

import java.util.Date;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.common.record.RecordAware;
import org.ironrhino.core.aop.PublishAware;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Recordable;
import org.springframework.security.core.userdetails.UserDetails;

@RecordAware
@PublishAware
@AutoConfig(searchable = true, order = "key asc")
@Searchable(alias = "setting")
public class Setting extends BaseEntity implements Recordable {

	private static final long serialVersionUID = -8352037603261222984L;

	@NaturalId(caseInsensitive = true, mutable = true)
	@UiConfig(displayOrder = 1, size = 50)
	@SearchableProperty(boost = 3)
	private String key = "";

	@UiConfig(displayOrder = 2, type = "textarea")
	@SearchableProperty
	private String value = "";

	@UiConfig(displayOrder = 3, type = "textarea")
	@SearchableProperty
	private String description = "";

	@NotInCopy
	@UiConfig(hide = true)
	private Date createDate = new Date();

	@NotInCopy
	@UiConfig(hide = true)
	private Date modifyDate;

	@NotInCopy
	@UiConfig(hide = true)
	private UserDetails createUser;

	@NotInCopy
	@UiConfig(hide = true)
	private UserDetails modifyUser;

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

	public UserDetails getCreateUser() {
		return createUser;
	}

	public void setCreateUser(UserDetails createUser) {
		this.createUser = createUser;
	}

	public UserDetails getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(UserDetails modifyUser) {
		this.modifyUser = modifyUser;
	}

}
