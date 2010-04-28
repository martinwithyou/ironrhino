package com.ironrhino.online.payment;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.ironrhino.core.util.NumberUtils;
import org.ironrhino.core.util.RequestUtils;
import org.springframework.beans.factory.annotation.Value;

import com.ironrhino.online.model.Order;
import com.ironrhino.online.service.OrderManager;

public class Paypal extends AbstractPayment {

	@Value("${paypal.business}")
	private String business;

	private String currency = "CNY";

	private OrderManager orderManager;

	public Paypal() {
		setCode("paypal");
		setName("贝宝");
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getBusiness() {
		return business;
	}

	public void setBusiness(String business) {
		this.business = business;
	}

	public OrderManager getOrderManager() {
		return orderManager;
	}

	public void setOrderManager(OrderManager orderManager) {
		this.orderManager = orderManager;
	}

	public String getPayForm(Order order) {
		HttpServletRequest request = ServletActionContext.getRequest();
		StringBuilder sb = new StringBuilder();
		sb
				.append("<form action=\"https://www.paypal.com/cgi-bin/webscr\" method=\"POST\">");
		sb.append(getHiddenFormField("cmd", "_xclick"));
		sb.append(getHiddenFormField("business", business));
		sb.append(getHiddenFormField("item_name", order.getCode()));
		sb.append(getHiddenFormField("item_number", order.getItems().size()));
		sb.append(getHiddenFormField("amount", NumberUtils.format(order
				.getGrandtotal().doubleValue(), 2)));
		sb.append(getHiddenFormField("currency_code", getCurrency()));
		sb.append(getHiddenFormField("no_note", "1"));
		sb.append(getHiddenFormField("no_shipping", "1"));
		sb.append(getHiddenFormField("charset", "utf-8"));
		sb.append(getHiddenFormField("return", RequestUtils.getBaseUrl(request)
				+ "/account/order/view/" + order.getCode()));
		sb.append(getHiddenFormField("notify_url", RequestUtils.getBaseUrl(
				request, false)
				+ "/payment/paypal"));
		sb
				.append("<input type=\"image\" SRC=\"http://images.paypal.com/assets/images/x-click-but01.gif\" border=\"0\" name=\"submit\" alt='"
						+ name + "' title='" + name + "'/>");
		sb.append("</form>");
		return sb.toString();
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
