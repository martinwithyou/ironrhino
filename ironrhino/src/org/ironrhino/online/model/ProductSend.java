package org.ironrhino.online.model;

import java.util.Date;

import org.ironrhino.core.model.BaseEntity;

public class ProductSend extends BaseEntity {

	private static final long serialVersionUID = 3514967763784546633L;

	private String username;

	private String name;

	private String email;

	private String productCode;

	private String destination;

	private String message;

	private Date sendDate;

	public ProductSend() {
		sendDate = new Date();
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
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

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
