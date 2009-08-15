package org.ironrhino.common.action;

import java.util.List;
import java.util.Map;

import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.monitor.analysis.CumulativeStatAnalyzer;
import org.ironrhino.core.monitor.analysis.TreeNode;

@AutoConfig
public class MonitorAction extends BaseAction {

	private Map<String, List<TreeNode>> data;

	public Map<String, List<TreeNode>> getData() {
		return data;
	}

	public String execute() {
		CumulativeStatAnalyzer ana = new CumulativeStatAnalyzer();
		ana.analyze();
		data = ana.getData();
		return SUCCESS;
	}

}
