package org.ironrhino.online.model;

import java.util.Date;

import org.ironrhino.core.model.BaseEntity;

public class LoginRecord extends BaseEntity {

	private static final long serialVersionUID = 7951996396426076387L;

	private String username;

	private Date loginDate;

	private String loginAddress;

	private boolean failed;

	private String cause;

	public LoginRecord() {
		loginDate = new Date();
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}

	public String getLoginAddress() {
		return loginAddress;
	}

	public void setLoginAddress(String loginAddress) {
		this.loginAddress = loginAddress;
	}

	public Date getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(Date loginDate) {
		this.loginDate = loginDate;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isFailed() {
		return failed;
	}

	public void setFailed(boolean failed) {
		this.failed = failed;
	}

}
