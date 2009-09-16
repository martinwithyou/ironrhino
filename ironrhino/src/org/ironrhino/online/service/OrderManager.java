package org.ironrhino.online.service;

import org.ironrhino.core.metadata.ConcurrencyControl;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.online.model.Order;

public interface OrderManager extends BaseManager<Order> {

	public void save(Order order);

	public void calculateOrder(Order order);

	@ConcurrencyControl(permits = "10")
	public String create(Order order);

}
