package org.ironrhino.common.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.struts2.ServletActionContext;
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

	private Long total;

	private int limit = 20;

	private Map<String, Long> urls;

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

	public Long getTotal() {
		return total;
	}

	public Map<String, Long> getUrls() {
		return urls;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	@Override
	public String execute() {
		if (date == null
				&& ServletActionContext.getRequest().getParameter("date") == null)
			date = DateUtils.beginOfDay(new Date());
		if (from == null && to == null) {
			to = DateUtils.beginOfDay(new Date());
			from = DateUtils.addDays(to, -30);
		}
		return SUCCESS;
	}

	public String pv() {
		if (date == null && from != null && to != null
				&& from.getTime() == to.getTime())
			date = from;
		if (date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			data = new ArrayList<Pair<Date, Long>>();
			for (int i = 0; i < 24; i++) {
				cal.set(Calendar.HOUR_OF_DAY, i);
				if (cal.getTime().before(new Date())) {
					Date d = cal.getTime();
					String key = DateUtils.format(d, "yyyyMMddHH");
					Long value = pageViewService.getPageView(key);
					Calendar c = Calendar.getInstance();
					c.setTime(d);
					c.set(Calendar.MINUTE, 30);
					c.set(Calendar.SECOND, 30);
					data.add(new Pair<Date, Long>(c.getTime(), value));
				}
			}
		} else if (from != null && to != null && from.before(to)) {
			data = new ArrayList<Pair<Date, Long>>();
			Date date = from;
			while (!date.after(to)) {
				String key = DateUtils.format(date, "yyyyMMdd");
				Long value = pageViewService.getPageView(key);
				data.add(new Pair<Date, Long>(date, value));
				date = DateUtils.addDays(date, 1);
			}
			Pair<String, Long> p = pageViewService.getMaxPageView();
			if (p != null)
				max = new Pair<Date, Long>(
						DateUtils.parse(p.getA(), "yyyyMMdd"), p.getB());
			long value = pageViewService.getPageView(null);
			if (value > 0)
				total = value;
		}
		return "chart";
	}

	public String uip() {
		if (from != null && to != null && from.before(to)) {
			data = new ArrayList<Pair<Date, Long>>();
			Date date = from;
			while (!date.after(to)) {
				String key = DateUtils.format(date, "yyyyMMdd");
				Long value = pageViewService.getUniqueIp(key);
				data.add(new Pair<Date, Long>(date, value));
				date = DateUtils.addDays(date, 1);
			}
			Pair<String, Long> p = pageViewService.getMaxUniqueIp();
			if (p != null)
				max = new Pair<Date, Long>(
						DateUtils.parse(p.getA(), "yyyyMMdd"), p.getB());
		}
		return "chart";
	}

	public String usid() {
		if (from != null && to != null && from.before(to)) {
			data = new ArrayList<Pair<Date, Long>>();
			Date date = from;
			while (!date.after(to)) {
				String key = DateUtils.format(date, "yyyyMMdd");
				Long value = pageViewService.getUniqueSessionId(key);
				data.add(new Pair<Date, Long>(date, value));
				date = DateUtils.addDays(date, 1);
			}
			Pair<String, Long> p = pageViewService.getMaxUniqueSessionId();
			if (p != null)
				max = new Pair<Date, Long>(
						DateUtils.parse(p.getA(), "yyyyMMdd"), p.getB());
		}
		return "chart";
	}

	public String uu() {
		if (from != null && to != null && from.before(to)) {
			data = new ArrayList<Pair<Date, Long>>();
			Date date = from;
			while (!date.after(to)) {
				String key = DateUtils.format(date, "yyyyMMdd");
				Long value = pageViewService.getUniqueUsername(key);
				data.add(new Pair<Date, Long>(date, value));
				date = DateUtils.addDays(date, 1);
			}
			Pair<String, Long> p = pageViewService.getMaxUniqueUsername();
			if (p != null)
				max = new Pair<Date, Long>(
						DateUtils.parse(p.getA(), "yyyyMMdd"), p.getB());
		}
		return "chart";
	}

	public String url() {
		String day = date != null ? DateUtils.format(date, "yyyyMMdd") : null;
		urls = pageViewService.getTopPageViewUrls(day, limit);
		return "url";
	}

}
