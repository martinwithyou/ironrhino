package org.ironrhino.online.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.Addressee;
import org.ironrhino.common.model.Region;
import org.ironrhino.common.util.NumberUtils;
import org.ironrhino.core.annotation.NaturalId;
import org.ironrhino.core.annotation.NotInCopy;
import org.ironrhino.core.annotation.NotInJson;
import org.ironrhino.core.annotation.Recordable;
import org.ironrhino.core.model.BaseEntity;

import com.opensymphony.xwork2.util.CreateIfNull;

@Recordable
public class Order extends BaseEntity {

	@NaturalId
	private String code;

	private BigDecimal discount;

	private BigDecimal shipcost;

	private Date orderDate;

	private String payment;

	private Date payDate;

	private String shipment;

	private Date shipDate;

	private OrderStatus status;

	private String comment;

	private String description;

	@CreateIfNull
	private List<OrderItem> items = new ArrayList<OrderItem>(0);

	private Account account;

	private Addressee addressee;

	@NotInCopy
	@NotInJson
	private Region region;

	public Order() {
		status = OrderStatus.INITIAL;
	}

	public String getPayment() {
		return payment;
	}

	public void setPayment(String payment) {
		this.payment = payment;
	}

	public Date getPayDate() {
		return payDate;
	}

	public void setPayDate(Date payDate) {
		this.payDate = payDate;
	}

	public Addressee getAddressee() {
		return addressee;
	}

	public void setAddressee(Addressee addressee) {
		if (this.addressee != null && addressee != null) {
			if (StringUtils.isNotBlank(this.addressee.getAddress())
					&& StringUtils.isNotBlank(addressee.getAddress())
					&& !this.addressee.getAddress().equals(
							addressee.getAddress()))
				this.region = null;
		}
		this.addressee = addressee;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String orderId) {
		this.code = orderId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}

	public String getShipment() {
		return shipment;
	}

	public void setShipment(String shipment) {
		this.shipment = shipment;
	}

	public Date getShipDate() {
		return shipDate;
	}

	public void setShipDate(Date shipDate) {
		this.shipDate = shipDate;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Region getRegion() {
		return region;
	}

	public void setRegion(Region region) {
		this.region = region;
	}

	public BigDecimal getDiscount() {
		return discount;
	}

	public void setDiscount(BigDecimal discount) {
		this.discount = NumberUtils.round(discount, 2);
	}

	public BigDecimal getGrandtotal() {
		BigDecimal grandtotal = new BigDecimal(getTotal().doubleValue());
		if (getShipcost() != null)
			grandtotal = grandtotal.add(getShipcost());
		if (getDiscount() != null)
			grandtotal = grandtotal.subtract(getDiscount());
		return grandtotal;
	}

	public void setGrandtotal(BigDecimal grandtotal) {
	}

	public BigDecimal getShipcost() {
		return shipcost;
	}

	public void setShipcost(BigDecimal shipcost) {
		this.shipcost = NumberUtils.round(shipcost, 2);
	}

	public BigDecimal getTotal() {
		BigDecimal total = new BigDecimal(0.00);
		for (OrderItem oi : items)
			total = total.add(oi.getSubtotal());
		return NumberUtils.round(total, 2);
	}

	public void setTotal(BigDecimal subtotal) {
	}

	public String getTitle() {
		StringBuilder sb = new StringBuilder();
		for (OrderItem oi : items)
			sb.append(oi.getProductName() + ":" + oi.getQuantity() + ",");
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public int getGrandtotalCents() {
		return getGrandtotal().multiply(new BigDecimal(100)).intValue();
	}

}
