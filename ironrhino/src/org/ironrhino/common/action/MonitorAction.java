package org.ironrhino.common.action;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ironrhino.common.support.MonitorControl;
import org.ironrhino.core.ext.openflashchart.model.Chart;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.metadata.JsonSerializerType;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.analysis.TreeNode;

@AutoConfig
public class MonitorAction extends BaseAction {

	private static final long serialVersionUID = -8946871669998582841L;

	private Date date;

	private Date from;

	private Date to;

	private String vtype; // value type l for longValue or d for doubleValue

	private String ctype;// chart type, bar ...

	private Map<String, List<TreeNode>> result;

	private Chart chart;

	private transient MonitorControl monitorControl;

	public String getVtype() {
		return vtype;
	}

	public void setVtype(String vtype) {
		this.vtype = vtype;
	}

	public String getCtype() {
		return ctype;
	}

	public void setCtype(String ctype) {
		this.ctype = ctype;
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

	public Chart getChart() {
		return chart;
	}

	public Map<String, List<TreeNode>> getResult() {
		return result;
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
		return "chart";
	}

	@JsonConfig(root = "chart", serializer = JsonSerializerType.GSON)
	public String data() {
		chart = monitorControl.getChart(Key.fromString(getUid()), date, vtype,
				ctype);
		return JSON;
	}
}
