package org.ironrhino.common.action;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ironrhino.common.support.MonitorControl;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.annotation.JsonConfig;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.monitor.analysis.TreeNode;

@AutoConfig
public class MonitorAction extends BaseAction {

	private Map<String, List<TreeNode>> data;

	private Date date;

	private Date from;

	private Date to;

	private transient MonitorControl monitorControl;

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
		return "chart";
	}

	@JsonConfig(root = "data")
	public String bar() {
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
		return JSON;
	}

}
