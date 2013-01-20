package org.ironrhino.daq.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.common.support.DictionaryControl;
import org.ironrhino.core.chart.ChartUtils;
import org.ironrhino.core.chart.openflashchart.Chart;
import org.ironrhino.core.chart.openflashchart.Text;
import org.ironrhino.core.chart.openflashchart.axis.Label.Rotate;
import org.ironrhino.core.chart.openflashchart.axis.XAxis;
import org.ironrhino.core.chart.openflashchart.axis.XAxisLabels;
import org.ironrhino.core.chart.openflashchart.axis.YAxis;
import org.ironrhino.core.chart.openflashchart.elements.LineChart;
import org.ironrhino.core.chart.openflashchart.elements.Point;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.service.BaseManager;
import org.ironrhino.core.struts.EntityAction;
import org.ironrhino.core.util.DateUtils;
import org.ironrhino.daq.model.Acquisition;

public class AcquisitionAction extends EntityAction {

	private static final long serialVersionUID = -1506823281606610436L;

	public static final String datePattern = "yyyy-MM-dd";

	public static final String DICTIONARY_NAME_ACQUISITION_TYPE = "ACQUISITION_TYPE";

	public static final String DICTIONARY_NAME_ACQUISITION_PLACE = "ACQUISITION_PLACE";

	@Inject
	private DictionaryControl dictionaryControl;

	private Date date;

	private Date from;

	private Date to;

	private String title;

	private Chart chart;

	private String type;

	private String[] place;

	private String recent;

	public String getRecent() {
		return recent;
	}

	public void setRecent(String recent) {
		this.recent = recent;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String[] getPlace() {
		return place;
	}

	public void setPlace(String[] place) {
		this.place = place;
	}

	public String getDictionaryNameAcquisitionType() {
		return DICTIONARY_NAME_ACQUISITION_TYPE;
	}

	public String getDictionaryNameAcquisitionPlace() {
		return DICTIONARY_NAME_ACQUISITION_PLACE;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		if (date == null)
			date = new Date();
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getFrom() {
		if (recent != null) {
			if (recent.endsWith("h")) {
				int hours = Integer.valueOf(recent.substring(0,
						recent.length() - 1));
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.HOUR_OF_DAY, -hours);
				from = cal.getTime();
			} else if (recent.endsWith("m")) {
				int minutes = Integer.valueOf(recent.substring(0,
						recent.length() - 1));
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MINUTE, -minutes);
				from = cal.getTime();
			}
		} else if (from == null) {
			if (date != null)
				from = date;
			else
				from = DateUtils.addDays(new Date(), 0);
		}
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		if (recent != null) {
			to = new Date();
		} else if (to == null) {
			if (date != null)
				to = date;
			else
				to = new Date();
		}
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public Chart getChart() {
		return chart;
	}

	public String getDateRange() {
		if (date != null)
			return DateUtils.format(date, datePattern);
		else if (getFrom() != null && getTo() != null)
			if (!DateUtils.isSameDay(from, to))
				return DateUtils.format(from, datePattern) + "-"
						+ DateUtils.format(to, datePattern);
			else
				return DateUtils.format(from, datePattern);
		return "";
	}

	public String chart() {
		return "chart";
	}

	@JsonConfig(root = "chart")
	public String data() {
		if (type == null || place == null)
			return NONE;

		title = dictionaryControl.getDictionaryLabel(
				getDictionaryNameAcquisitionType(), type);
		chart = new Chart(title + "(" + getDateRange() + ")",
				"font-size: 15px;");
		chart.setY_legend(new Text("value", "{font-size: 12px; color: #778877}"));
		chart.setX_legend(new Text(getText("time"),
				"{font-size: 12px; color: #778877}"));
		XAxis x = new XAxis();
		XAxisLabels xAxisLabels = new XAxisLabels();
		xAxisLabels.setText("#date:H:i:s#");
		xAxisLabels.setSteps(ChartUtils.caculateXAxisLabelsSteps(getFrom(),
				getTo()));
		xAxisLabels.setVisible_steps(1);
		xAxisLabels.setRotate(Rotate.VERTICAL);
		xAxisLabels.setSize(12);
		x.setXAxisLabels(xAxisLabels);
		x.setSteps(ChartUtils.caculateXAxisSteps(getFrom(), getTo()));
		if (recent != null)
			x.setRange((long) (getFrom().getTime() / 1000), (long) (getTo()
					.getTime() / 1000));
		else
			x.setRange(
					(long) (DateUtils.beginOfDay(getFrom()).getTime() / 1000),
					(long) (DateUtils.endOfDay(getTo()).getTime() / 1000));
		chart.setX_axis(x);
		Double max = 0.00;
		for (int i = 0; i < place.length; i++) {

			BaseManager<Acquisition> entityManager = getEntityManager(Acquisition.class);
			DetachedCriteria dc = entityManager.detachedCriteria();
			dc.add(Restrictions.eq("type", type));
			dc.add(Restrictions.eq("place", place[i]));
			dc.add(Restrictions.between("time",
					DateUtils.beginOfDay(getFrom()),
					DateUtils.endOfDay(getTo())));
			dc.addOrder(org.hibernate.criterion.Order.asc("time"));
			List<Acquisition> list = entityManager.findListByCriteria(dc);
			List<Object> points = new ArrayList<Object>(list.size());
			for (Acquisition ac : list) {
				double value = ac.getValue();
				if (max < value)
					max = value;
				points.add(new Point((long) (ac.getTime().getTime() / 1000),
						value));
			}
			LineChart element = new LineChart();
			element.setText(dictionaryControl.getDictionaryLabel(
					getDictionaryNameAcquisitionPlace(), place[i]));
			element.setFontSize(12);
			element.setColour(ChartUtils.caculateColor(i));
			element.setValues(points);
			chart.addElements(element);
		}
		YAxis y = new YAxis();
		y.setSteps(ChartUtils.caculateSteps(max));
		y.setMax(max);
		chart.setY_axis(y);

		return JSON;
	}

	private Acquisition acq;

	public Acquisition getAcq() {
		return acq;
	}

	@JsonConfig(root = "acq")
	public String latest() {
		if (type == null || place == null)
			return NONE;
		BaseManager<Acquisition> entityManager = getEntityManager(Acquisition.class);
		DetachedCriteria dc = entityManager.detachedCriteria();
		dc.add(Restrictions.eq("type", type));
		dc.add(Restrictions.eq("place", place[0]));
		dc.addOrder(Order.desc("time"));
		acq = entityManager.findByCriteria(dc);
		return JSON;
	}

}
