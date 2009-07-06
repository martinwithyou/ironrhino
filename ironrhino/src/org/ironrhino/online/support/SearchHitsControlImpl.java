package org.ironrhino.online.support;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.AggregateResult;
import org.ironrhino.common.support.StringStore;
import org.ironrhino.common.util.DateUtils;
import org.ironrhino.core.cache.CheckCache;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.online.model.SearchHits;
import org.ironrhino.online.model.SearchHitsHistory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.hibernate3.HibernateCallback;

public class SearchHitsControlImpl extends StringStore implements
		SearchHitsControl {

	private BaseManager<SearchHits> baseManager;

	@Required
	public void setBaseManager(BaseManager<SearchHits> baseManager) {
		this.baseManager = baseManager;
	}

	public void put(String keywords, int totalHits) {
		if (!StringUtils.isBlank(keywords))
			store(keywords + "," + totalHits + ","
					+ DateUtils.getDatetime(new Date()));
	}

	protected void doConsume(List<String> list) {
		Map<String, SearchHits> map = new HashMap<String, SearchHits>();
		Date d = new Date();
		if (DateUtils.isYearStart(d))
			resetMonthlyAndYearly();
		else if (DateUtils.isMonthStart(d))
			resetMonthly();

		String today = DateUtils.getDatetime(d);
		for (String s : list) {
			String date = s.substring(s.lastIndexOf(',') + 1);
			s = s.substring(0, s.lastIndexOf(','));
			String totalHits = s.substring(s.lastIndexOf(',') + 1);
			String keywords = s.substring(0, s.lastIndexOf(','));
			SearchHits hits = map.get(keywords);
			if (hits == null) {
				hits = new SearchHits();
				hits.setKeyword(keywords);
				map.put(keywords, hits);
			}
			if (today.substring(0, 4).equals(date.substring(0, 4)))
				hits.setYearly(hits.getYearly() + 1);
			if (today.substring(0, 7).equals(date.substring(0, 7)))
				hits.setMonthly(hits.getMonthly() + 1);
			if (today.substring(0, 10).equals(date.substring(0, 10)))
				hits.setDaily(hits.getDaily() + 1);
			hits.setTotal(hits.getTotal() + 1);
			hits.setTotalHits(Integer.parseInt(totalHits));
		}
		for (SearchHits ph : map.values())
			merge(ph, d);
		if (DateUtils.isYearEnd(d))
			archiveMonthAndYear(d);
		else if (DateUtils.isMonthEnd(d))
			archiveMonth(d);
	}

	private void merge(SearchHits ph, Date d) {
		baseManager.setEntityClass(SearchHits.class);
		SearchHits hits = baseManager
				.getByNaturalId("keyword", ph.getKeyword());
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
		String hql = "update SearchHits sh set sh.monthly = 0";
		baseManager.bulkUpdate(hql);
	}

	private void resetMonthlyAndYearly() {
		String hql = "update SearchHits sh set sh.monthly = 0 and sh.yearly = 0";
		baseManager.bulkUpdate(hql);
	}

	private void archiveMonth(Date date) {
		baseManager.setEntityClass(SearchHits.class);
		List<SearchHits> list = baseManager.getAll();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		final int year = cal.get(Calendar.YEAR);
		final int month = cal.get(Calendar.MONTH) + 1;
		for (final SearchHits ph : list) {
			baseManager.execute(new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Criteria c = session
							.createCriteria(SearchHitsHistory.class);
					c.add(Restrictions.naturalId().set("keyword",
							ph.getKeyword()).set("month", month).set("year",
							year));
					SearchHitsHistory phh = (SearchHitsHistory) c
							.uniqueResult();
					if (phh == null)
						phh = new SearchHitsHistory();
					phh.setKeyword(ph.getKeyword());
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
		baseManager.setEntityClass(SearchHits.class);
		List<SearchHits> list = baseManager.getAll();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		final int year = cal.get(Calendar.YEAR);
		final int month = cal.get(Calendar.MONTH) + 1;
		for (final SearchHits ph : list) {
			baseManager.execute(new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Criteria c = session
							.createCriteria(SearchHitsHistory.class);
					c.add(Restrictions.naturalId().set("keyword",
							ph.getKeyword()).set("month", month).set("year",
							year));
					SearchHitsHistory phhm = (SearchHitsHistory) c
							.uniqueResult();
					if (phhm == null)
						phhm = new SearchHitsHistory();
					phhm.setKeyword(ph.getKeyword());
					phhm.setMonth(month);
					phhm.setYear(year);
					phhm.setCount(ph.getMonthly());
					session.saveOrUpdate(phhm);
					c = session.createCriteria(SearchHitsHistory.class);
					c.add(Restrictions.naturalId().set("keyword",
							ph.getKeyword()).set("month", 0).set("year", year));
					SearchHitsHistory phhy = (SearchHitsHistory) c
							.uniqueResult();
					if (phhy == null)
						phhy = new SearchHitsHistory();
					phhy.setKeyword(ph.getKeyword());
					phhy.setMonth(0);
					phhy.setYear(year);
					phhy.setCount(ph.getYearly());
					session.saveOrUpdate(phhy);
					return null;
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ironrhino.online.support.SearchSuggestion#suggest(java.lang.String)
	 */
	@CheckCache(value = "search_suggest_${args[0]}")
	public List<AggregateResult> suggest(String keyword) {
		baseManager.setEntityClass(SearchHits.class);
		DetachedCriteria dc = baseManager.detachedCriteria();
		dc.add(Restrictions.ilike("keyword", keyword, MatchMode.START));
		dc.add(Restrictions.gt("totalHits", 0));
		dc.addOrder(Order.desc("totalHits"));
		List<SearchHits> list = baseManager.getListByCriteria(dc, 1, 10);
		List<AggregateResult> result = new ArrayList<AggregateResult>(list
				.size());
		for (SearchHits sh : list) {
			AggregateResult ar = new AggregateResult();
			ar.setPrincipal(sh.getKeyword());
			ar.setCount(sh.getTotalHits());
			result.add(ar);
		}
		return result;
	}
}
