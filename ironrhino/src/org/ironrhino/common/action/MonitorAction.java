package org.ironrhino.common.action;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ironrhino.common.support.MonitorControl;
import org.ironrhino.core.ext.openflashchart.model.Chart;
import org.ironrhino.core.ext.openflashchart.model.Text;
import org.ironrhino.core.ext.openflashchart.model.axis.XAxis;
import org.ironrhino.core.ext.openflashchart.model.axis.YAxis;
import org.ironrhino.core.ext.openflashchart.model.elements.BarChart;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.metadata.JsonSerializerType;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.Value;
import org.ironrhino.core.monitor.analysis.TreeNode;

@AutoConfig
public class MonitorAction extends BaseAction {

	private static final long serialVersionUID = -8946871669998582841L;

	private Date date;

	private Date from;

	private Date to;

	private Map<String, List<TreeNode>> result;

	private Chart chart;

	private transient MonitorControl monitorControl;

	public Chart getChart() {
		return chart;
	}

	public Map<String, List<TreeNode>> getResult() {
		return result;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setMonitorControl(MonitorControl monitorControl) {
		this.monitorControl = monitorControl;
	}

	@Override
	public String execute() {
		try {
			if (from != null && to != null) {
				result = monitorControl.getResult(from, to);
			} else {
				Date today = new Date();
				if (date == null || date.after(today))
					date = today;
				result = monitorControl.getResult(date);
			}
		} catch (Exception e) {
			result = new HashMap<String, List<TreeNode>>();
		}
		return SUCCESS;
	}

	public String chart() {
		// String key = getUid();
		return "chart";
	}

	@JsonConfig(root = "chart", serializer = JsonSerializerType.GSON)
	public String data() {
		chart = new Chart();
		Key key = Key.fromString(getUid());
		if (date == null)
			date = new Date();
		List<Value> list = monitorControl.getPeriodResult(key, date, false);
		if (list != null && list.size() > 0) {
			String[] labels = new String[list.size()];
			Long[] longValues = new Long[list.size()];
			Double[] doubleValues = new Double[list.size()];
			Long minLongValue = null, maxLongValue = null;
			Double minDoubleValue = null, maxDoubleValue = null;
			for (int i = 0; i < list.size(); i++) {
				labels[i] = String.valueOf(i);
				Long longValue = list.get(i).getLongValue();
				Double doubleValue = list.get(i).getDoubleValue();
				if (minLongValue == null || minLongValue > longValue)
					minLongValue = longValue;
				if (maxLongValue == null || maxLongValue < longValue)
					maxLongValue = longValue;
				if (minDoubleValue == null || minDoubleValue > doubleValue)
					minDoubleValue = doubleValue;
				if (maxDoubleValue == null || maxDoubleValue < doubleValue)
					maxDoubleValue = doubleValue;
				longValues[i] = longValue;
				doubleValues[i] = doubleValue;
			}
			chart.setTitle(new Text(key.toString()));
			XAxis x = new XAxis();
			YAxis y = new YAxis();
			y.setMax(maxLongValue.doubleValue());
			y.setLabels("ylabel");
			chart.setXAxis(x);
			chart.setYAxis(y);
			x.setLabels(labels);
			BarChart bc = new BarChart();
			bc.addValues(longValues);
			chart.addElements(bc);
		}
		return JSON;
	}
}
