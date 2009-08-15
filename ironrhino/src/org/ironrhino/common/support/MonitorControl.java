package org.ironrhino.common.support;

import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ironrhino.core.monitor.analysis.TreeNode;

public interface MonitorControl {

	public boolean archive();

	public boolean archive(Date date);

	public Map<String, List<TreeNode>> getData(Date date)
			throws FileNotFoundException;

	public Map<String, List<TreeNode>> getData(Date from, Date to)
			throws FileNotFoundException;
}
