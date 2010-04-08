package com.ironrhino.online.action.account;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.ServletActionContext;
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
				order = getCart().getOrder();
		} else {
			order = getCart().getOrder();
		}
		return order;
	}

	public Cart getCart() {
		if (cart == null) {
			cart = (Cart) ServletActionContext.getRequest().getSession()
					.getAttribute(Cart.SESSION_KEY_CART);
			if (cart == null)
				cart = new Cart();
		}
		return cart;
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
		orderManager.calculateOrder(getOrder());
		return VIEW;
	}

	@Override
	public String input() {
		if ("addressee".equals(originalMethod)) {
			Addressee add = getCart().getOrder().getAddressee();
			if (add == null) {
				add = new Addressee();
				getCart().getOrder().setAddressee(add);
				BeanUtils.copyProperties(AuthzUtils.getUserDetails(User.class)
						.getDefaultAddressee(), add);
			}
			return "addressee";
		} else if ("payment".equals(originalMethod)) {
			Addressee add = getCart().getOrder().getAddressee();
			if (add == null) {
				add = new Addressee();
				getCart().getOrder().setAddressee(add);
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
		if (!getOrder().isNew()) {
			orderManager.save(getOrder());
			targetUrl = "/account/order/view/" + getOrder().getCode();
		} else {
			targetUrl = "/account/order/view";
		}
		markSessionDirty();
		return REDIRECT;
	}

	@InputConfig(methodName = "input")
	public String payment() {
		targetUrl = "/account/order/view/" + getUid();
		return REDIRECT;
	}

	public String confirm() {
		targetUrl = "/account/order/view/"
				+ orderManager.create(getCart().getOrder());
		getCart().clear();
		markSessionDirty();
		return REDIRECT;
	}

	public String cancel() {
		Order order = getOrder();
		if (!getOrder().isNew()
				&& getOrder().getStatus() == OrderStatus.INITIAL) {
			getOrder().setStatus(OrderStatus.CANCELLED);
			orderManager.save(order);
		}
		return REFERER;
	}

	@Override
	public String delete() {
		String[] id = getId();
		if (id != null) {
			List<Order> list;
			if (id.length == 1) {
				list = new ArrayList<Order>(1);
				list.add(orderManager.get(id[0]));
			} else {
				DetachedCriteria dc = orderManager.detachedCriteria();
				dc.add(Restrictions.in("id", id));
				list = orderManager.findListByCriteria(dc);
			}
			if (list.size() > 0) {
				boolean deletable = true;
				for (Order temp : list) {
					if (!orderManager.canDelete(temp)) {
						addActionError(temp.getCode() + getText("delete.forbidden"));
						deletable = false;
						break;
					}
				}
				if (deletable) {
					for (Order temp : list)
						orderManager.delete(temp);
					addActionMessage(getText("delete.success"));
				}
			}
		}
		return SUCCESS;
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
				items = getOrder().getItems();
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
		targetUrl = "/account/order/view" + getOrder().getCode() == null ? ""
				: "/" + getOrder().getCode();
		return REDIRECT;
	}

	@Override
	public String update() {
		// adjust quantity,remove item status must be INITIAL
		return NONE;
	}

	private void markSessionDirty() {
		if (cart != null)
			ServletActionContext.getRequest().getSession().setAttribute(
					Cart.SESSION_KEY_CART, cart);
	}

}
