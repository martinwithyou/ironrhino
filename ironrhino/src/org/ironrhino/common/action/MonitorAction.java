package org.ironrhino.common.action;

import java.util.List;

import org.ironrhino.core.annotation.AutoConfig;
import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.monitor.analysis.CumulativeStatAnalyzer;
import org.ironrhino.core.monitor.analysis.TreeNode;

@AutoConfig
public class MonitorAction extends BaseAction {

	private List<TreeNode> list;

	public List<TreeNode> getList() {
		return list;
	}

	public String execute() {
		CumulativeStatAnalyzer ana = new CumulativeStatAnalyzer();
		ana.analyze();
		list = ana.getTopTreeNodes();
		return SUCCESS;
	}

}
