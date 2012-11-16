package com.ironrhino.online.service;

import java.math.BigDecimal;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ironrhino.common.support.RegionTreeControl;
import org.ironrhino.core.metadata.ConcurrencyControl;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.core.util.AuthzUtils;
import org.ironrhino.core.util.CodecUtils;
import org.ironrhino.core.util.DateUtils;
import org.ironrhino.core.util.NumberUtils;
import org.ironrhino.ums.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.transaction.annotation.Transactional;

import com.ironrhino.online.model.Order;
import com.ironrhino.online.model.OrderStatus;

@Singleton
@Named("orderManager")
public class OrderManagerImpl extends BaseManagerImpl<Order> implements
		OrderManager {

	public static final String CREATE_CONCURRENT_PERMITS_KEY = "orderManager.create.concurrent.permits";

	@Autowired(required = false)
	private RegionTreeControl regionTreeControl;

	@Inject
	@Named("orderCodeSequence")
	private DataFieldMaxValueIncrementer orderCodeSequence;

	@Override
	@Transactional
	public void save(Order order) {
		if (regionTreeControl != null && order.getRegion() == null
				&& order.getAddressee() != null)
			order.setRegion(regionTreeControl.parseByAddress(order
					.getAddressee().getAddress()));
		super.save(order);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean canDelete(Order order) {
		return order.getStatus() == OrderStatus.INITIAL
				|| order.getStatus() == OrderStatus.CANCELLED;
	}

	private String nextCode() {
		StringBuilder sb = new StringBuilder();
		sb.append(DateUtils.formatDate8(new Date()));
		sb.append(NumberUtils.format(orderCodeSequence.nextIntValue(), 5));
		sb.append(CodecUtils.randomString(5));
		return sb.toString();
	}

	public void doCalculateOrder(Order order) {
		// calculate and set discount,shipcost
		order.setUser(AuthzUtils.getUserDetails(User.class));
		order.setCreateDate(new Date());
		if (order.getTotal().compareTo(new BigDecimal(100)) < 0)
			order.setShipcost(new BigDecimal(0.01));

	}

	public void calculateOrder(Order order) {
		if (order.getStatus() != OrderStatus.INITIAL)
			return;
		doCalculateOrder(order);
		if (!order.isNew())
			super.save(order);
	}

	@Transactional
	@ConcurrencyControl(permits = "${aspect.config('"
			+ CREATE_CONCURRENT_PERMITS_KEY + "',10)}")
	public String create(Order order) {
		Order o = new Order();
		BeanUtils.copyProperties(order, o);
		calculateOrder(o);
		o.setCode(nextCode());
		save(o);
		return o.getCode();
	}

}