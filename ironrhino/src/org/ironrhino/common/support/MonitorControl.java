package org.ironrhino.common.support;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.Value;
import org.ironrhino.core.monitor.analysis.TreeNode;

public interface MonitorControl {

	public void archive();

	public void archive(Date date);

	public Map<String, List<TreeNode>> getResult(Date date);

	public Map<String, List<TreeNode>> getResult(Date from, Date to);

	public List<Value> getResult(Key key, Date date);

	public Map<String, Value> getResultPerHost(Key key, Date date);
}
