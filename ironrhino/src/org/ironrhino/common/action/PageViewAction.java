package org.ironrhino.common.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.ironrhino.common.model.tuples.Pair;
import org.ironrhino.common.service.PageViewService;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.DateUtils;

@AutoConfig
public class PageViewAction extends BaseAction {

	private static final long serialVersionUID = -6901193289995112304L;

	private Date date;

	private Date from;

	private Date to;

	private List<Pair<Date, Long>> data;

	private Pair<Date, Long> max;

	@Inject
	private transient PageViewService pageViewService;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public List<Pair<Date, Long>> getData() {
		return data;
	}

	public Pair<Date, Long> getMax() {
		return max;
	}

	@Override
	public String execute() {
		return SUCCESS;
	}

	public String chart() {
		if (date == null && from != null && to != null
				&& from.getTime() == to.getTime())
			date = from;
		if (date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			data = new ArrayList<Pair<Date, Long>>();
			for (int i = 0; i < 24; i++) {
				cal.set(Calendar.HOUR_OF_DAY, i);
				if (cal.before(new Date())) {
					Date d = cal.getTime();
					String key = DateUtils.format(d, "yyyyMMddHH");
					Long value = pageViewService.getPageView(key);
					Calendar c = Calendar.getInstance();
					c.setTime(d);
					c.set(Calendar.MINUTE, 59);
					c.set(Calendar.SECOND, 59);
					data.add(new Pair<Date, Long>(c.getTime(), value));
				}
			}
		} else if (from != null && to != null && from.before(to)) {
			data = new ArrayList<Pair<Date, Long>>();
			date = from;
			while (to.after(date)) {
				String key = DateUtils.format(date, "yyyyMMdd");
				Long value = pageViewService.getPageView(key);
				data.add(new Pair<Date, Long>(date, value));
				date = DateUtils.addDays(date, 1);
			}
			Pair<String, Long> p = pageViewService.getMaxPageView();
			max = new Pair<Date, Long>(DateUtils.parse(p.getA(), "yyyyMMdd"),
					p.getB());
		}
		return SUCCESS;
	}

}
