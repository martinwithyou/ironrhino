package com.ironrhino.online.action.account;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Addressee;
import org.ironrhino.core.model.ResultPage;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.ums.model.User;
import org.springframework.beans.BeanUtils;

import com.ironrhino.online.model.Order;
import com.ironrhino.online.model.OrderItem;
import com.ironrhino.online.model.OrderStatus;
import com.ironrhino.online.payment.PaymentManager;
import com.ironrhino.online.service.OrderManager;
import com.ironrhino.online.support.Cart;
import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

public class OrderAction extends BaseAction {

	private static final long serialVersionUID = 3927567371955750570L;

	private Cart cart;

	private Order order;

	private ResultPage<Order> resultPage;

	@Inject
	private transient OrderManager orderManager;
	@Inject
	private transient PaymentManager paymentManager;

	public ResultPage<Order> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Order> resultPage) {
		this.resultPage = resultPage;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Order getOrder() {
		String code = getUid();
		if (StringUtils.isNotBlank(code)) {
			if (order != null && !order.isNew())
				return order;
			order = orderManager.findByNaturalId(code);
			if (order == null
					|| !order.getUser().equals(
							AuthzUtils.getUserDetails(User.class)))
				order = cart.getOrder();
			return order;
		} else {
			return cart.getOrder();
		}
	}

	public void setCart(Cart cart) {
		this.cart = cart;
	}

	public PaymentManager getPaymentManager() {
		return paymentManager;
	}

	@Override
	public String execute() {
		if (resultPage == null)
			resultPage = new ResultPage<Order>();
		DetachedCriteria dc = orderManager.detachedCriteria();
		resultPage.setDetachedCriteria(dc);
		dc.add(Restrictions.eq("user", AuthzUtils.getUserDetails(User.class)));
		resultPage.addOrder(org.hibernate.criterion.Order.desc("createDate"));
		resultPage = orderManager.findByResultPage(resultPage);
		return SUCCESS;
	}

	@Override
	public String view() {
		order = getOrder();
		orderManager.calculateOrder(order);
		return VIEW;
	}

	@Override
	public String input() {
		if ("addressee".equals(originalMethod)) {
			Addressee add = cart.getOrder().getAddressee();
			if (add == null) {
				add = new Addressee();
				cart.getOrder().setAddressee(add);
				BeanUtils.copyProperties(AuthzUtils.getUserDetails(User.class)
						.getDefaultAddressee(), add);
			}
			return "addressee";
		} else if ("payment".equals(originalMethod)) {
			Addressee add = cart.getOrder().getAddressee();
			if (add == null) {
				add = new Addressee();
				cart.getOrder().setAddressee(add);
				BeanUtils.copyProperties(AuthzUtils.getUserDetails(User.class)
						.getDefaultAddressee(), add);
			}
			return "payment";
		}
		targetUrl = "/account/order/view";
		return REDIRECT;
	}

	@InputConfig(methodName = "input")
	public String addressee() {
		if (!order.isNew()) {
			orderManager.save(order);
			targetUrl = "/account/order/view/" + order.getCode();
		} else {
			targetUrl = "/account/order/view";
		}
		return REDIRECT;
	}

	@InputConfig(methodName = "input")
	public String payment() {
		targetUrl = "/account/order/view/" + getUid();
		return REDIRECT;
	}

	public String confirm() {
		targetUrl = "/account/order/view/"
				+ orderManager.create(cart.getOrder());
		cart.clear();
		return REDIRECT;
	}

	public String cancel() {
		Order order = getOrder();
		if (!order.isNew() && order.getStatus() == OrderStatus.INITIAL) {
			order.setStatus(OrderStatus.CANCELLED);
			orderManager.save(order);
		}
		return REFERER;
	}

	@Override
	public String delete() {
		Order order = getOrder();
		if (!order.isNew()
				&& (order.getStatus() == OrderStatus.INITIAL || order
						.getStatus() == OrderStatus.CANCELLED))
			orderManager.delete(order);
		targetUrl = "/account/order";
		return REDIRECT;
	}

	public String merge() {
		String[] ids = getId();
		if (ids == null || ids.length < 2)
			return SUCCESS;
		List<OrderItem> items = null;
		for (int i = 0; i < ids.length; i++) {
			Order o = orderManager.get(ids[i]);
			if (i == 0) {
				order = o;
				items = order.getItems();
				continue;
			}
			if (o.getStatus() != OrderStatus.INITIAL
					|| !o.getUser().equals(
							AuthzUtils.getUserDetails(User.class)))
				continue;
			for (OrderItem oi : o.getItems()) {
				boolean contains = false;
				for (OrderItem item : items) {
					if (item.getProductCode().equals(oi.getProductCode())) {
						contains = true;
						item.setQuantity(item.getQuantity() + oi.getQuantity());
						break;
					}
				}
				if (!contains)
					items.add(oi);
			}
			orderManager.delete(o);
		}
		orderManager.save(order);
		targetUrl = "/account/order/view" + order.getCode() == null ? "" : "/"
				+ order.getCode();
		return REDIRECT;
	}

	@Override
	public String update() {
		// adjust quantity,remove item status must be INITIAL
		return NONE;
	}

}
