package org.ironrhino.security.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.util.CodecUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@AutoConfig
@Searchable(alias = "user")
public class User extends BaseEntity implements UserDetails {

	private static final long serialVersionUID = -6135434863820342822L;

	@NaturalId
	@SearchableProperty
	private String username;

	@NotInCopy
	private String password;

	@SearchableProperty
	private String name;

	private Collection<GrantedAuthority> authorities;

	private boolean enabled = true;

	@NotInCopy
	private Date createDate = new Date();

	@NotInCopy
	@NotInJson
	@SearchableProperty
	private Set<String> roles = new HashSet<String>(0);

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<String> getRoles() {
		return roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	@NotInCopy
	public String getRolesAsString() {
		if (roles.size() > 0)
			return StringUtils.join(roles.iterator(), ',');
		return null;
	}

	public void setRolesAsString(String rolesAsString) {
		if (StringUtils.isNotBlank(rolesAsString))
			roles.addAll(Arrays.asList(rolesAsString.split(",")));
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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

	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(Collection<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	public boolean isAccountNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
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

	@Override
	public String toString() {
		return this.name;
	}
}
