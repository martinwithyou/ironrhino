package org.ironrhino.common.action;

import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Stat;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.monitor.KeyValuePair;
import org.ironrhino.core.monitor.analysis.CumulativeAnalyzer;
import org.ironrhino.core.monitor.analysis.CumulativeFileAnalyzer;
import org.ironrhino.core.monitor.analysis.TreeNode;
import org.ironrhino.core.service.BaseManager;

@AutoConfig
public class MonitorAction extends BaseAction {

	private Map<String, List<TreeNode>> data;

	private Date date;

	private BaseManager<Stat> baseManager;

	public Map<String, List<TreeNode>> getData() {
		return data;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setBaseManager(BaseManager<Stat> baseManager) {
		this.baseManager = baseManager;
	}

	public String execute() {
		//TODO date range
		Date today = new Date();
		if (date == null || date.after(today))
			date = today;
		try {
			CumulativeFileAnalyzer ana = new CumulativeFileAnalyzer(date);
			ana.analyze();
			data = ana.getData();
		} catch (FileNotFoundException e) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			Date start = cal.getTime();
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			Date end = cal.getTime();
			baseManager.setEntityClass(Stat.class);
			DetachedCriteria dc = baseManager.detachedCriteria();
			dc.add(Restrictions.between("statDate", start, end));
			final Iterator<Stat> it = baseManager.getListByCriteria(dc)
					.iterator();
			Iterator<KeyValuePair> iterator = new Iterator<KeyValuePair>() {

				public boolean hasNext() {
					return it.hasNext();
				}

				public KeyValuePair next() {
					return it.next().toKeyValuePair();
				}

				public void remove() {
					it.remove();
				}

			};
			CumulativeAnalyzer ana = new CumulativeAnalyzer(iterator);
			ana.analyze();
			data = ana.getData();
		}
		return SUCCESS;
	}

}
