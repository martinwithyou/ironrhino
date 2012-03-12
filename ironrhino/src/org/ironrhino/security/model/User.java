package org.ironrhino.security.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.compass.annotations.Index;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Recordable;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.JsonUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@AutoConfig
@Searchable(alias = "user")
public class User extends BaseEntity implements UserDetails, Recordable<User> {

	private static final long serialVersionUID = -6135434863820342822L;

	public static final String USERNAME_REGEX = "^[\\w\\(\\)]{3,20}$";

	public static final String USERNAME_REGEX_FOR_SIGNUP = "^[\\w\\]{3,20}$";

	@NaturalId(caseInsensitive = true)
	@SearchableProperty(boost = 3, index = Index.NOT_ANALYZED)
	@NotInCopy
	private String username;

	@NotInCopy
	@NotInJson
	private String password;

	@SearchableProperty(boost = 2)
	private String name;

	@SearchableProperty(boost = 3)
	private String email;

	@SearchableProperty
	@NotInJson
	private String phone;

	@NotInJson
	private boolean enabled = true;

	@NotInCopy
	@NotInJson
	private Date createDate = new Date();

	@NotInCopy
	@NotInJson
	private Collection<GrantedAuthority> authorities;

	@NotInCopy
	@NotInJson
	@SearchableProperty
	private Set<String> roles = new HashSet<String>(0);

	@NotInCopy
	@NotInJson
	private Map<String, String> attributes;

	@NotInCopy
	@NotInJson
	private Date modifyDate;

	@NotInCopy
	@NotInJson
	private transient User createUser;

	@NotInCopy
	@NotInJson
	private transient User modifyUser;

	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public User getCreateUser() {
		return createUser;
	}

	public String getEmail() {
		return email;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public User getModifyUser() {
		return modifyUser;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public String getPhone() {
		return phone;
	}

	public Set<String> getRoles() {
		return roles;
	}

	@NotInCopy
	@NotInJson
	public String getRolesAsString() {
		if (roles.size() > 0)
			return StringUtils.join(roles.iterator(), ',');
		return null;
	}

	public String getUsername() {
		return username;
	}

	@NotInJson
	public boolean isAccountNonExpired() {
		return true;
	}

	@NotInJson
	public boolean isAccountNonLocked() {
		return true;
	}

	@NotInJson
	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isPasswordValid(String legiblePassword) {
		return AuthzUtils.isPasswordValid(this, legiblePassword);
	}

	public void setAuthorities(Collection<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public void setCreateUser(User createUser) {
		this.createUser = createUser;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setLegiblePassword(String legiblePassword) {
		this.password = AuthzUtils.encodePassword(this, legiblePassword);
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public void setModifyUser(User modifyUser) {
		this.modifyUser = modifyUser;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	public void setRolesAsString(String rolesAsString) {
		roles.clear();
		if (StringUtils.isNotBlank(rolesAsString))
			roles.addAll(Arrays.asList(org.ironrhino.core.util.StringUtils
					.trimTail(rolesAsString, ",").split(",")));
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAttribute(String key) {
		if (attributes == null)
			return null;
		return attributes.get(key);
	}

	public void setAttribute(String key, String value) {
		if (attributes == null)
			attributes = new HashMap<String, String>(4);
		if (value == null)
			attributes.remove(key);
		else
			attributes.put(key, value);
	}

	public String getAttributes() {
		if (attributes == null || attributes.isEmpty())
			return null;
		return JsonUtils.toJson(attributes);
	}

	public void setAttributes(String str) {
		if (StringUtils.isNotBlank(str))
			try {
				attributes = JsonUtils.fromJson(str,
						new TypeReference<Map<String, String>>() {
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	@Override
	public String toString() {
		return StringUtils.isNotBlank(this.name) ? this.name : this.username;
	}
}
