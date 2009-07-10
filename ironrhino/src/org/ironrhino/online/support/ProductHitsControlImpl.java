package org.ironrhino.online.support;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.support.StringStore;
import org.ironrhino.common.util.DateUtils;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.online.model.ProductHits;
import org.ironrhino.online.model.ProductHitsHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;

public class ProductHitsControlImpl extends StringStore implements
		ProductHitsControl {

	@Autowired
	private BaseManager<ProductHits> baseManager;

	public void put(String productCode) {
		store(productCode + "," + DateUtils.getDatetime(new Date()));
	}

	protected void doConsume(List<String> list) {

		Map<String, ProductHits> map = new HashMap<String, ProductHits>();
		Date d = new Date();
		if (DateUtils.isYearStart(d))
			resetMonthlyAndYearly();
		else if (DateUtils.isMonthStart(d))
			resetMonthly();

		String today = DateUtils.getDatetime(d);
		for (String s : list) {
			String code = s.substring(0, s.lastIndexOf(','));
			String date = s.substring(s.lastIndexOf(',') + 1);
			ProductHits hits = map.get(code);
			if (hits == null) {
				hits = new ProductHits();
				hits.setProductCode(code);
				map.put(code, hits);
			}
			if (today.substring(0, 4).equals(date.substring(0, 4)))
				hits.setYearly(hits.getYearly() + 1);
			if (today.substring(0, 7).equals(date.substring(0, 7)))
				hits.setMonthly(hits.getMonthly() + 1);
			if (today.substring(0, 10).equals(date.substring(0, 10)))
				hits.setDaily(hits.getDaily() + 1);
			hits.setTotal(hits.getTotal() + 1);
		}
		for (ProductHits ph : map.values())
			merge(ph, d);

		if (DateUtils.isYearEnd(d))
			archiveMonthAndYear(d);
		else if (DateUtils.isMonthEnd(d))
			archiveMonth(d);
	}

	private void merge(ProductHits ph, Date d) {
		baseManager.setEntityClass(ProductHits.class);
		ProductHits hits = baseManager.getByNaturalId("productCode", ph
				.getProductCode());
		if (hits == null) {
			hits = ph;
		} else {
			hits.setDaily(ph.getDaily());
			hits.setMonthly(hits.getMonthly() + ph.getMonthly());
			hits.setYearly(hits.getYearly() + ph.getYearly());
			hits.setTotal(hits.getTotal() + ph.getTotal());
		}
		baseManager.save(hits);
	}

	private void resetMonthly() {
		String hql = "update ProductHits ph set ph.monthly = 0";
		baseManager.bulkUpdate(hql);
	}

	private void resetMonthlyAndYearly() {
		String hql = "update ProductHits ph set ph.monthly = 0 and ph.yearly = 0";
		baseManager.bulkUpdate(hql);
	}

	private void archiveMonth(Date date) {
		baseManager.setEntityClass(ProductHits.class);
		List<ProductHits> list = baseManager.getAll();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		final int year = cal.get(Calendar.YEAR);
		final int month = cal.get(Calendar.MONTH) + 1;
		for (final ProductHits ph : list) {
			baseManager.execute(new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Criteria c = session
							.createCriteria(ProductHitsHistory.class);
					c.add(Restrictions.naturalId().set("productCode",
							ph.getProductCode()).set("month", month).set(
							"year", year));
					ProductHitsHistory phh = (ProductHitsHistory) c
							.uniqueResult();
					if (phh == null)
						phh = new ProductHitsHistory();
					phh.setProductCode(ph.getProductCode());
					phh.setMonth(month);
					phh.setYear(year);
					phh.setCount(ph.getMonthly());
					session.saveOrUpdate(phh);
					return null;
				}
			});
		}
	}

	private void archiveMonthAndYear(Date date) {
		baseManager.setEntityClass(ProductHits.class);
		List<ProductHits> list = baseManager.getAll();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		final int year = cal.get(Calendar.YEAR);
		final int month = cal.get(Calendar.MONTH) + 1;
		for (final ProductHits ph : list) {
			baseManager.execute(new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Criteria c = session
							.createCriteria(ProductHitsHistory.class);
					c.add(Restrictions.naturalId().set("productCode",
							ph.getProductCode()).set("month", month).set(
							"year", year));
					ProductHitsHistory phhm = (ProductHitsHistory) c
							.uniqueResult();
					if (phhm == null)
						phhm = new ProductHitsHistory();
					phhm.setProductCode(ph.getProductCode());
					phhm.setMonth(month);
					phhm.setYear(year);
					phhm.setCount(ph.getMonthly());
					session.saveOrUpdate(phhm);
					c = session.createCriteria(ProductHitsHistory.class);
					c.add(Restrictions.naturalId().set("productCode",
							ph.getProductCode()).set("month", 0).set("year",
							year));
					ProductHitsHistory phhy = (ProductHitsHistory) c
							.uniqueResult();
					if (phhy == null)
						phhy = new ProductHitsHistory();
					phhy.setProductCode(ph.getProductCode());
					phhy.setMonth(0);
					phhy.setYear(year);
					phhy.setCount(ph.getYearly());
					session.saveOrUpdate(phhy);
					return null;
				}
			});
		}
	}

}
