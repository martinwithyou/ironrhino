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
import org.ironrhino.core.monitor.analysis.TreeNode;

@AutoConfig
public class MonitorAction extends BaseAction {

	private Date date;

	private Date from;

	private Date to;

	private Map<String, List<TreeNode>> data;

	private Chart chart;

	private transient MonitorControl monitorControl;

	public Chart getChart() {
		return chart;
	}

	public Map<String, List<TreeNode>> getData() {
		return data;
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
				data = monitorControl.getData(from, to);
			} else {
				Date today = new Date();
				if (date == null || date.after(today))
					date = today;
				data = monitorControl.getData(date);
			}
		} catch (Exception e) {
			data = new HashMap<String, List<TreeNode>>();
		}
		return SUCCESS;
	}

	public String chart() {
//		String key = getUid();
		return "chart";
	}

	@JsonConfig(root = "chart", serializer = JsonSerializerType.GSON)
	public String bar() {
		String key = getUid();
		chart = new Chart();
		chart.setTitle(new Text("I am title"));
		XAxis x = new XAxis();
		x.setLabels("1月","2月","3月","4月");
		YAxis y = new YAxis();
		y.setMax(50);
		y.setLabels("ylabel");
		chart.setXAxis(x);
		chart.setYAxis(y);
		BarChart bc = new BarChart();
		bc.addValues(12, 23, 34, 45);
		chart.addElements(bc);
		return JSON;
	}

}
