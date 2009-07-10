package org.ironrhino.online.service;

import java.util.Date;
import java.util.List;

import org.drools.StatefulSession;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.support.RegionTreeControl;
import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.common.util.CodecUtils;
import org.ironrhino.common.util.DateUtils;
import org.ironrhino.common.util.NumberUtils;
import org.ironrhino.core.rule.RuleProvider;
import org.ironrhino.core.service.BaseManagerImpl;
import org.ironrhino.online.model.Account;
import org.ironrhino.online.model.Order;
import org.ironrhino.online.model.OrderStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class OrderManagerImpl extends BaseManagerImpl<Order> implements
		OrderManager {

	@Autowired(required = false)
	private RegionTreeControl regionTreeControl;

	@Autowired
	private RuleProvider ruleProvider;

	@Transactional
	public void save(Order order) {
		if (regionTreeControl != null && order.getRegion() == null)
			order.setRegion(regionTreeControl.parseByAddress(order
					.getAddressee().getAddress()));
		super.save(order);
	}

	private synchronized String generateCode(Order order) {
		String prefix = DateUtils.getDate8(new Date());
		DetachedCriteria dc = detachedCriteria();
		dc.add(Restrictions.like("code", prefix + "%"));
		dc.addOrder(org.hibernate.criterion.Order.desc("code"));
		List<Order> results = getListByCriteria(dc, 1, 1);
		int sequence = 0;
		if (results.size() > 0) {
			String lastId = results.get(0).getCode();
			String lastSeq = lastId.substring(8);
			lastSeq = lastSeq.substring(0, lastSeq.length() - 5);
			sequence = Integer.parseInt(lastSeq);
		}
		String suffix = CodecUtils.randomString(5);
		return prefix + NumberUtils.format(sequence + 1, 5) + suffix;
	}

	public void doCalculateOrder(Order order) {
		// calculate and set discount,shipcost
		order.setAccount(AuthzUtils.getUserDetails(Account.class));
		order.setCreateDate(new Date());
		if (order.getClass() == Order.class) {
			// not proxy
			StatefulSession session = ruleProvider.getStatefulSession();
			ruleProvider.insert(session, order);
			session.fireAllRules();
			ruleProvider.retract(session, order);
		} else {
			Order temp = new Order();
			BeanUtils.copyProperties(order, temp);
			StatefulSession session = ruleProvider.getStatefulSession();
			ruleProvider.insert(session, temp);
			session.fireAllRules();
			ruleProvider.retract(session, temp);
			BeanUtils.copyProperties(temp, order);
		}

	}

	@Transactional
	public void calculateOrder(Order order) {
		if (order.getStatus() != OrderStatus.INITIAL)
			return;
		doCalculateOrder(order);
		if (!order.isNew())
			super.save(order);
	}

	@Transactional
	public String create(Order order) {
		Order o = new Order();
		BeanUtils.copyProperties(order, o);
		calculateOrder(o);
		o.setCode(generateCode(order));
		save(o);
		return o.getCode();
	}

}
