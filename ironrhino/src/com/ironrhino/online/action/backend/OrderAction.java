package com.ironrhino.online.action.backend;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.ResultPage;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;

import com.ironrhino.online.model.Order;
import com.ironrhino.online.model.OrderStatus;
import com.ironrhino.online.service.OrderManager;

@AutoConfig
public class OrderAction extends BaseAction {

	private static final long serialVersionUID = 2943946788569857928L;

	private Order order;

	private ResultPage<Order> resultPage;

	@Autowired
	private transient OrderManager orderManager;

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
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
			if (order.getUser() != null) {
				dc.createAlias("user", "u");
				if (StringUtils.isNotBlank(order.getUser().getUsername()))
					dc.add(Restrictions.ilike("u.username", order.getUser()
							.getUsername(), MatchMode.ANYWHERE));
				if (StringUtils.isNotBlank(order.getUser().getName()))
					dc.add(Restrictions.ilike("a.name", order.getUser()
							.getName(), MatchMode.ANYWHERE));
			}
		}
		resultPage.addOrder(org.hibernate.criterion.Order.desc("createDate"));
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
				order = orderManager.getByNaturalId(order.getCode());
		return VIEW;
	}

	@Override
	public String delete() {
		if (order != null)
			if (order.getId() != null)
				order = orderManager.get(order.getId());
			else if (order.getCode() != null)
				order = orderManager.getByNaturalId(order.getCode());
		if (order != null && order.getStatus() == OrderStatus.CANCELLED)
			orderManager.delete(order);
		return SUCCESS;
	}

}
