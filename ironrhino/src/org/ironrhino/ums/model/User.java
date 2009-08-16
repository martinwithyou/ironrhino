package org.ironrhino.ums.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.SimpleElement;
import org.ironrhino.common.util.CodecUtils;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.RecordAware;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Recordable;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;

@RecordAware
@AutoConfig
public class User extends BaseEntity implements UserDetails, Recordable {
	@NaturalId
	private String username;

	@NotInCopy
	private String password;

	private String name;

	private String email;

	private String description;

	private Date accountExpireDate;

	private Date passwordExpireDate;

	private GrantedAuthority[] authorities;

	private boolean locked;

	private boolean enabled;

	@NotInCopy
	private Date createDate;

	@NotInCopy
	private Date modifyDate;

	@NotInCopy
	private Set<SimpleElement> roles = new HashSet<SimpleElement>(0);

	@NotInCopy
	private Set<SimpleElement> groups = new HashSet<SimpleElement>(0);

	public User() {
		createDate = new Date();
		enabled = true;
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

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<SimpleElement> getGroups() {
		return groups;
	}

	public void setGroups(Set<SimpleElement> groups) {
		this.groups = groups;
	}

	@NotInCopy
	public String getGroupsAsString() {
		return StringUtils.join(groups.iterator(), ',');
	}

	public void setGroupsAsString(String groupsAsString) {
		SimpleElement.fillCollectionWithString(groups, groupsAsString);
	}

	public Set<SimpleElement> getRoles() {
		return roles;
	}

	public void setRoles(Set<SimpleElement> roles) {
		this.roles = roles;
	}

	@NotInCopy
	public String getRolesAsString() {
		return StringUtils.join(roles.iterator(), ',');
	}

	public void setRolesAsString(String rolesAsString) {
		SimpleElement.fillCollectionWithString(roles, rolesAsString);
	}

	public void setAuthorities(GrantedAuthority[] authorities) {
		this.authorities = authorities;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Date getAccountExpireDate() {
		return accountExpireDate;
	}

	public void setAccountExpireDate(Date accountExpireDate) {
		this.accountExpireDate = accountExpireDate;
	}

	public Date getPasswordExpireDate() {
		return passwordExpireDate;
	}

	public void setPasswordExpireDate(Date passwordExpireDate) {
		this.passwordExpireDate = passwordExpireDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public GrantedAuthority[] getAuthorities() {
		return this.authorities;
	}

	public boolean isAccountNonExpired() {
		return (accountExpireDate == null)
				|| (accountExpireDate.after(new Date()));
	}

	public boolean isAccountNonLocked() {
		return !locked;
	}

	public boolean isCredentialsNonExpired() {
		return (passwordExpireDate == null)
				|| (passwordExpireDate.after(new Date()));
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setLegiblePassword(String legiblePassword) {
		this.password = CodecUtils.digest(legiblePassword);
	}

	public boolean isPasswordValid(String legiblePassword) {
		return this.password.equals(CodecUtils.digest(legiblePassword));
	}
}
