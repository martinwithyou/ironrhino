package org.ironrhino.ums.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.Addressee;
import org.ironrhino.common.model.Sex;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.NotInJson;
import org.ironrhino.core.metadata.RecordAware;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Recordable;
import org.ironrhino.core.model.SimpleElement;
import org.ironrhino.core.util.CodecUtils;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;

@RecordAware
public class User extends BaseEntity implements UserDetails, Recordable {

	private static final long serialVersionUID = -6696796299386961371L;

	@NaturalId
	private String username;

	@NotInCopy
	@NotInJson
	private String password;

	private String name;

	private Sex sex;

	private Date birthday;

	private String email;

	private String address;

	private String postcode;

	private String phone;

	private boolean locked;

	private boolean enabled;

	@NotInCopy
	private int loginTimes;

	@NotInCopy
	private Date lastLoginDate;

	@NotInCopy
	private String lastLoginAddress;

	@NotInCopy
	private Date createDate;

	@NotInCopy
	private Date modifyDate;

	@NotInCopy
	@NotInJson
	private Set<SimpleElement> roles = new HashSet<SimpleElement>(0);

	@NotInJson
	private GrantedAuthority[] authorities = new GrantedAuthority[0];

	public User() {
		createDate = new Date();
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLastLoginAddress() {
		return lastLoginAddress;
	}

	public void setLastLoginAddress(String lastLoginAddress) {
		this.lastLoginAddress = lastLoginAddress;
	}

	public Date getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public int getLoginTimes() {
		return loginTimes;
	}

	public void setLoginTimes(int loginTimes) {
		this.loginTimes = loginTimes;
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

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setPassword(String password) {
		this.password = password;
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
		return true;
	}

	public boolean isAccountNonLocked() {
		return !locked;
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

	public String getFriendlyName() {
		if (StringUtils.isNotBlank(this.name))
			return this.name;
		else
			return this.username;
	}

	public Addressee getDefaultAddressee() {
		Addressee defaultAddressee = new Addressee();
		defaultAddressee.setName(name);
		defaultAddressee.setAddress(address);
		defaultAddressee.setPostcode(postcode);
		defaultAddressee.setPhone(phone);
		return defaultAddressee;
	}
}
