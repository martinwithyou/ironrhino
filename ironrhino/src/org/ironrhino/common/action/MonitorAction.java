package org.ironrhino.common.action;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ironrhino.common.support.MonitorControl;
import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.monitor.analysis.TreeNode;

@AutoConfig
public class MonitorAction extends BaseAction {

	private Map<String, List<TreeNode>> data;

	private Date date;

	private transient MonitorControl monitorControl;

	public Map<String, List<TreeNode>> getData() {
		return data;
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
		// TODO date range
		Date today = new Date();
		if (date == null || date.after(today))
			date = today;
		data = monitorControl.getData(date);
		return SUCCESS;
	}

}
