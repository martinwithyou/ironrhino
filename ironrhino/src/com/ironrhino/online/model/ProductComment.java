package com.ironrhino.online.model;

import java.util.Date;

import org.ironrhino.core.model.BaseEntity;

public class ProductComment extends BaseEntity {

	private static final long serialVersionUID = -7729245741527669247L;

	private String username;

	private String displayName;

	private boolean needNotify;

	private String email;

	private String productCode;

	private String content;

	private Date commentDate;

	public ProductComment() {
		commentDate = new Date();
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public Date getCommentDate() {
		return commentDate;
	}

	public void setCommentDate(Date commentDate) {
		this.commentDate = commentDate;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isNeedNotify() {
		return needNotify;
	}

	public void setNeedNotify(boolean needNotify) {
		this.needNotify = needNotify;
	}

}
