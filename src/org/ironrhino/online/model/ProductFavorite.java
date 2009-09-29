package org.ironrhino.online.model;

import java.util.Date;

import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.model.BaseEntity;

public class ProductFavorite extends BaseEntity {

	private static final long serialVersionUID = 2682396354291685790L;

	@NaturalId
	private String username;

	@NaturalId
	private String productCode;

	private String productName;

	private Date addDate;

	public ProductFavorite() {
		addDate = new Date();
	}

	public Date getAddDate() {
		return addDate;
	}

	public void setAddDate(Date addDate) {
		this.addDate = addDate;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

}
