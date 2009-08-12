package org.ironrhino.common.support;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.common.model.Stat;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.Value;
import org.ironrhino.core.monitor.analysis.Analyzer;
import org.ironrhino.core.service.BaseManager;

public class StatControl {

	protected Log log = LogFactory.getLog(getClass());

	private BaseManager<Stat> baseManager;

	public void setBaseManager(BaseManager<Stat> baseManager) {
		this.baseManager = baseManager;
	}

	public void archive() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, -1);
		final Date yesterday = cal.getTime();
		try {
			new Analyzer(yesterday) {
				protected void process(Key key, Value value, Date date) {
					if (key.isCumulative())
						baseManager.save(new Stat(key.toString(), value
								.getLong(), value.getDouble(), date));
				}
			};
		} catch (RuntimeException e) {
			log.error("错误发生");
			log.error(e.getMessage(), e);
		}
	}

}
