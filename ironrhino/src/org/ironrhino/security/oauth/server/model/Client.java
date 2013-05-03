package org.ironrhino.security.oauth.server.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.ForeignKey;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.CaseInsensitive;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Enableable;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.model.UserRole;

@AutoConfig(order = "name asc")
@Authorize(ifAllGranted = UserRole.ROLE_ADMINISTRATOR)
@Entity
@Table(name = "oauth_client")
public class Client extends BaseEntity implements Enableable {

	private static final long serialVersionUID = -7297737795748467475L;

	public static final String OAUTH_OOB = "urn:ietf:wg:oauth:2.0:oob";

	@UiConfig(displayOrder = 1, size = 50)
	@CaseInsensitive
	@org.hibernate.annotations.NaturalId(mutable = true)
	@Column(nullable = false)
	private String name;

	@UiConfig(displayOrder = 2, size = 50)
	@Column(nullable = false)
	private String secret = CodecUtils.nextId();

	@UiConfig(displayOrder = 3, size = 50)
	private String redirectUri;

	@UiConfig(displayOrder = 4, type = "textarea")
	private String description;

	@NotInCopy
	@UiConfig(hidden = true)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner", nullable = false)
	@ForeignKey(name = "none")
	private User owner;

	@UiConfig(displayOrder = 5)
	private boolean enabled = true;

	@NotInCopy
	@UiConfig(hidden = true)
	private Date modifyDate;

	@NotInCopy
	@UiConfig(hidden = true)
	private Date createDate = new Date();

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

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Date getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String toString() {
		return this.name;
	}

	public boolean supportsRedirectUri(String redirectUri) {
		return StringUtils.isBlank(this.redirectUri)
				|| this.redirectUri.equals(redirectUri);
	}

	@UiConfig(hidden = true)
	public boolean isNative() {
		return OAUTH_OOB.equals(redirectUri);
	}

	public boolean equals(Object other) {
		if (other instanceof Client) {
			Client that = (Client) other;
			if (this.getId().equals(that.getId())
					&& this.getSecret().equals(that.getSecret())
					&& supportsRedirectUri(that.getRedirectUri()))
				return true;
		}
		return false;
	}

	public int hashCode() {
		return id != null ? id.hashCode() : -1;
	}
}
