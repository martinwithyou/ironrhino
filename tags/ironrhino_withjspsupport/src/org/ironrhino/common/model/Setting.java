package org.ironrhino.common.model;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.PublishAware;
import org.ironrhino.core.metadata.RecordAware;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Recordable;
import org.ironrhino.security.model.User;

@RecordAware
@PublishAware
@AutoConfig(searchable = true, order = "key asc")
@Searchable(alias = "setting")
public class Setting extends BaseEntity implements Recordable<User> {

	private static final long serialVersionUID = -8352037603261222984L;

	@NaturalId(caseInsensitive = true, mutable = true)
	@UiConfig(displayOrder = 1, size = 50)
	@SearchableProperty(boost = 3)
	private String key = "";

	@UiConfig(displayOrder = 2, type = "textarea")
	@SearchableProperty
	private String value = "";

	@NotInCopy
	@UiConfig(hide = true)
	private Date createDate = new Date();

	@NotInCopy
	@UiConfig(hide = true)
	private Date modifyDate;

	@NotInCopy
	@UiConfig(hide = true)
	private User createUser;

	@NotInCopy
	@UiConfig(hide = true)
	private User modifyUser;

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

	public User getCreateUser() {
		return createUser;
	}

	public void setCreateUser(User createUser) {
		this.createUser = createUser;
	}

	public User getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(User modifyUser) {
		this.modifyUser = modifyUser;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}