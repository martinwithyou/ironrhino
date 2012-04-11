package org.ironrhino.security.oauth.server.model;

import java.util.Date;

import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.security.model.User;
import org.ironrhino.security.model.UserRole;

@AutoConfig(namespace = "/oauth", readonly = true, order = "createDate asc")
@Authorize(ifAllGranted = UserRole.ROLE_ADMINISTRATOR)
public class Authorization extends BaseEntity {

	private static final long serialVersionUID = -559379341059695550L;

	@NaturalId
	@UiConfig(displayOrder = 1)
	private String accessToken = CodecUtils.nextId();

	@UiConfig(displayOrder = 2)
	private Client client;

	@UiConfig(displayOrder = 3)
	private User grantor;

	@UiConfig(displayOrder = 4)
	private String scope;

	@UiConfig(displayOrder = 5)
	private String code;

	@UiConfig(displayOrder = 6)
	private int expiresIn = 3600;

	@UiConfig(displayOrder = 7)
	private String refreshToken;

	@UiConfig(displayOrder = 8)
	private String responseType = "code";

	@NotInCopy
	@UiConfig(hidden = true)
	private Date modifyDate = new Date();

	@NotInCopy
	@UiConfig(hidden = true)
	private Date createDate = new Date();

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public User getGrantor() {
		return grantor;
	}

	public void setGrantor(User grantor) {
		this.grantor = grantor;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
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

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

	public int getLifetime() {
		return expiresIn > 0 ? expiresIn
				- (int) (System.currentTimeMillis() - modifyDate.getTime() / 1000)
				: Integer.MAX_VALUE;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getResponseType() {
		return responseType;
	}

	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}

	public boolean isClientSide() {
		return "token".equals(responseType);
	}

}
