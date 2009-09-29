package org.ironrhino.online.payment;

import javax.servlet.http.HttpServletRequest;

import org.ironrhino.online.model.Order;
import org.ironrhino.online.service.OrderManager;

public class Alipay extends AbstractPayment {

	private String partner;

	private String key;

	private OrderManager orderManager;

	public Alipay() {
		setCode("alipay");
		setName("支付宝");
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String bargainor_id) {
		this.partner = bargainor_id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public OrderManager getOrderManager() {
		return orderManager;
	}

	public void setOrderManager(OrderManager orderManager) {
		this.orderManager = orderManager;
	}

	public String getPayForm(Order order) {
		return null;
	}

	public String getCheckUrl(Order order) {
		return null;
	}

	public String payCallback(HttpServletRequest request) {
		return doCallback(request, "1");
	}

	public String checkCallback(HttpServletRequest request) {
		return doCallback(request, "2");
	}

	public String doCallback(HttpServletRequest request, String cmdno) {
		return null;
	}
}
