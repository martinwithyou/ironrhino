package com.ironrhino.online.model;

import java.util.Date;

import org.ironrhino.core.metadata.RecordAware;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.model.Recordable;

@RecordAware
public class Feedback extends BaseEntity implements Recordable {

	private static final long serialVersionUID = -6411648866122045952L;

	private String username;

	private String name;

	private String email;

	private String phone;

	private String title;

	private String content;

	private Date createDate;

	private Date modifyDate;

	private FeedbackStatus status;

	public Feedback() {
		status = FeedbackStatus.UNDECIDED;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public FeedbackStatus getStatus() {
		return status;
	}

	public void setStatus(FeedbackStatus status) {
		this.status = status;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

}
