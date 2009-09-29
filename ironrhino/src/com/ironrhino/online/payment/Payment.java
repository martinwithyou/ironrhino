package com.ironrhino.online.payment;

import javax.servlet.http.HttpServletRequest;

import com.ironrhino.online.model.Order;

public interface Payment {

	public String getCode();

	public String getName();

	public boolean isDisabled();

	public String getPayForm(Order order);

	public String getCheckUrl(Order order);

	public String payCallback(HttpServletRequest request);

	public String checkCallback(HttpServletRequest request);

}
