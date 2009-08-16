package org.ironrhino.online.model;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class OrderItem implements Serializable {

	private int quantity;

	private String productCode;

	private String productName;

	private BigDecimal productPrice;

	public OrderItem() {

	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
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

	public BigDecimal getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(BigDecimal productPrice) {
		this.productPrice = productPrice;
	}

	public BigDecimal getSubtotal() {
		return productPrice.multiply(new BigDecimal(quantity));
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.quantity).append(
				this.productCode).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof OrderItem))
			return false;
		OrderItem that = (OrderItem) obj;
		return new EqualsBuilder().append(this.quantity, that.quantity).append(
				this.productCode, that.productCode).isEquals();
	}

}
