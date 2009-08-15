package org.ironrhino.common.support;

import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.model.Stat;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.KeyValuePair;
import org.ironrhino.core.monitor.Value;
import org.ironrhino.core.monitor.analysis.Analyzer;
import org.ironrhino.core.monitor.analysis.FileAnalyzer;
import org.ironrhino.core.service.BaseManager;

public class StatControl {

	protected Log log = LogFactory.getLog(getClass());

	private BaseManager<Stat> baseManager;

	public void setBaseManager(BaseManager<Stat> baseManager) {
		this.baseManager = baseManager;
	}

	public boolean archive() {
		return archive(false);
	}

	public boolean archive(boolean today) {
		Calendar cal = Calendar.getInstance();
		if (!today)
			cal.add(Calendar.DAY_OF_YEAR, -1);
		final Date statDay = cal.getTime();
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
		dc.addOrder(Order.desc("statDate"));
		Stat stat = baseManager.getByCriteria(dc);
		final Date lastStatDate;
		if (stat != null)
			lastStatDate = stat.getStatDate();
		else
			lastStatDate = null;

		try {
			Analyzer analyzer = new FileAnalyzer(statDay) {
				Calendar calendar = Calendar.getInstance();
				int currentHour = 0;
				Map<Key, Value> map = new HashMap<Key, Value>();

				private void save() {
					List<Map.Entry<Key, Value>> list = new ArrayList<Map.Entry<Key, Value>>();
					list.addAll(map.entrySet());
					Collections.sort(list,
							new Comparator<Map.Entry<Key, Value>>() {
								public int compare(Entry<Key, Value> o1,
										Entry<Key, Value> o2) {
									return o1.getKey().compareTo(o2.getKey());
								}
							});
					for (Map.Entry<Key, Value> entry : list)
						baseManager.save(new Stat(entry.getKey().toString(),
								entry.getValue().getLongValue(), entry.getValue()
										.getDoubleValue(), new Timestamp(entry
										.getKey().getLastWriteTime())));
					map.clear();
				}

				protected void process(KeyValuePair pair) {
					if (!pair.getKey().isCumulative())
						return;
					if (lastStatDate != null) {
						long time = lastStatDate.getTime();
						// hibernate doesn't handle mysql's timestamp
						if (time % 1000 == 0)
							time += 999;
						if (pair.getDate().getTime() <= time)
							return;
					}
					Key lastKey = null;
					Value lastValue = null;
					for (Map.Entry<Key, Value> entry : map.entrySet()) {
						if (pair.getKey().equals(entry.getKey())) {
							lastKey = entry.getKey();
							lastValue = entry.getValue();
							break;
						}
					}
					if (lastValue == null) {
						lastKey = pair.getKey();
						lastValue = pair.getValue();
						lastKey.setLastWriteTime(pair.getDate().getTime());
						map.put(lastKey, lastValue);
					} else {
						if (calendar.get(Calendar.HOUR_OF_DAY) == currentHour) {
							lastKey.setLastWriteTime(pair.getDate().getTime());
							lastValue.cumulate(pair.getValue());
						} else {
							save();
							lastKey = pair.getKey();
							lastValue = pair.getValue();
							lastKey.setLastWriteTime(pair.getDate().getTime());
							map.put(lastKey, lastValue);
						}
					}
					calendar.setTime(pair.getDate());
					currentHour = calendar.get(Calendar.HOUR_OF_DAY);
				}

				protected void postAnalyze() {
					save();
				}
			};
			analyzer.analyze();
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

}
