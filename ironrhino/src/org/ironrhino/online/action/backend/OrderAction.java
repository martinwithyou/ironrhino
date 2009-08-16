package org.ironrhino.online.action.backend;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.online.model.Order;
import org.ironrhino.online.model.OrderStatus;
import org.ironrhino.online.service.OrderManager;

@AutoConfig
public class OrderAction extends BaseAction {

	private OrderManager orderManager;

	private Order order;

	private ResultPage<Order> resultPage;

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public OrderManager getOrderManager() {
		return orderManager;
	}

	public void setOrderManager(OrderManager orderManager) {
		this.orderManager = orderManager;
	}

	public ResultPage<Order> getResultPage() {
		return resultPage;
	}

	public void setResultPage(ResultPage<Order> resultPage) {
		this.resultPage = resultPage;
	}

	@Override
	public String execute() {
		if (resultPage == null)
			resultPage = new ResultPage<Order>();
		DetachedCriteria dc = orderManager.detachedCriteria();
		resultPage.setDetachedCriteria(dc);
		if (order != null) {
			if (StringUtils.isNotBlank(order.getCode()))
				dc.add(Restrictions.ilike("code", order.getCode(),
						MatchMode.ANYWHERE));
			if (order.getAccount() != null) {
				dc.createAlias("account", "a");
				if (StringUtils.isNotBlank(order.getAccount().getUsername()))
					dc.add(Restrictions.ilike("a.username", order.getAccount()
							.getUsername(), MatchMode.ANYWHERE));
				if (StringUtils.isNotBlank(order.getAccount().getName()))
					dc.add(Restrictions.ilike("a.name", order.getAccount()
							.getName(), MatchMode.ANYWHERE));
			}
		}
		resultPage.addOrder(org.hibernate.criterion.Order.desc("orderDate"));
		resultPage = orderManager.getResultPage(resultPage);
		return LIST;
	}

	public String reject() {
		Order o = orderManager.get(order.getId());
		if (o.getStatus() == OrderStatus.INITIAL) {
			o.setStatus(OrderStatus.REJECTED);
			o.setComment(order.getComment());
			orderManager.save(o);
		}
		return SUCCESS;
	}

	public String ship() {
		order = orderManager.get(order.getId());
		if (order.getStatus() == OrderStatus.PAID) {
			order.setStatus(OrderStatus.SHIPPED);
			orderManager.save(order);
		}
		return SUCCESS;
	}

	public String complete() {
		order = orderManager.get(order.getId());
		if (order.getStatus() == OrderStatus.SHIPPED) {
			order.setStatus(OrderStatus.COMPLETED);
			orderManager.save(order);
		}
		return SUCCESS;
	}

	@Override
	public String view() {
		if (order != null)
			if (order.getId() != null)
				order = orderManager.get(order.getId());
			else if (order.getCode() != null)
				order = orderManager.getByNaturalId("code", order.getCode());
		return VIEW;
	}

	@Override
	public String delete() {
		if (order != null)
			if (order.getId() != null)
				order = orderManager.get(order.getId());
			else if (order.getCode() != null)
				order = orderManager.getByNaturalId("code", order.getCode());
		if (order != null && order.getStatus() == OrderStatus.CANCELLED)
			orderManager.delete(order);
		return SUCCESS;
	}

}
