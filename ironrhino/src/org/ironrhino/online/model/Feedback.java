package org.ironrhino.online.model;

import java.util.Date;

import org.ironrhino.core.model.BaseEntity;

public class Feedback extends BaseEntity {

	private static final long serialVersionUID = -6411648866122045952L;

	private String username;

	private String name;

	private String email;

	private String phone;

	private String subject;

	private String content;

	private Date postDate;

	private FeedbackStatus status;

	public Feedback() {
		status = FeedbackStatus.UNDECIDED;
		postDate = new Date();
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

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
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

	public Date getPostDate() {
		return postDate;
	}

	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}

}
