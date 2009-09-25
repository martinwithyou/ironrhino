package org.ironrhino.common.support;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.Value;
import org.ironrhino.core.monitor.analysis.TreeNode;
import org.ironrhino.core.openflashchart.model.Chart;

public interface MonitorControl {

	public void archive();

	public void archive(Date date);

	public Map<String, List<TreeNode>> getResult(Date date);

	public Map<String, List<TreeNode>> getResult(Date from, Date to);

	public List<Value> getPeriodResult(Key key, Date date, boolean cumulative);

	public Map<String, List<Value>> getPerHostPeriodResult(Key key, Date date,
			boolean cumulative);
	
	public Chart getChart(Key key,Date date,String vtype,String ctype);
}
