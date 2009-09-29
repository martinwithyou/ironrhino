package org.ironrhino.online.service;

import org.ironrhino.core.service.BaseManager;
import org.ironrhino.online.model.Order;

public interface OrderManager extends BaseManager<Order> {

	public void save(Order order);

	public void calculateOrder(Order order);

	public String create(Order order);

}
