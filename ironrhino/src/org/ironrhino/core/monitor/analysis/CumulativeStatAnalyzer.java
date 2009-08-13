package org.ironrhino.core.monitor.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.Value;
import org.ironrhino.core.monitor.analysis.model.TreeNode;

public class CumulativeStatAnalyzer extends StatAnalyzer {

	private List<TreeNode> topLevel = new ArrayList<TreeNode>();

	private int index = 0;

	public CumulativeStatAnalyzer() {
		super();
	}

	public CumulativeStatAnalyzer(Date start, Date end, boolean excludeEnd) {
		super(start, end, excludeEnd);
	}

	public CumulativeStatAnalyzer(Date start, Date end) {
		super(start, end);
	}

	public CumulativeStatAnalyzer(Date date) {
		super(date);
	}

	public CumulativeStatAnalyzer(Date[] dates) {
		super(dates);
	}

	public CumulativeStatAnalyzer(File... files) {
		super(files);
	}

	public CumulativeStatAnalyzer(File file) {
		super(file);
	}

	public List<TreeNode> getList() {
		return topLevel;
	}

	protected void process(Key key, Value value, Date date) {
		if (!key.isCumulative())
			return;
		// TreeNode parent = null;
		List<TreeNode> list = topLevel;
		for (int i = 1; i <= key.getLevel(); i++) {
			Key cur = key.parent(i);
			TreeNode node = null;
			boolean contains = false;
			for (int j = 0; j < list.size(); j++) {
				node = list.get(j);
				if (cur.equals(node.getKey())) {
					node.getValue().cumulate(value);
					contains = true;
					break;
				}
			}
			if (!contains) {
				node = new TreeNode();
				node.setId(++index);
				node.setKey(cur);
				node.setValue(value);
				list.add(node);
			}
			// parent = node;
			list = node.getChildren();
		}

	}

	protected void postAnalyze() {
		// TODO sort;
	}

	public static void main(String... strings) {

		CumulativeStatAnalyzer ana = new CumulativeStatAnalyzer();
		ana.analyze();
		walk(ana.getList().get(0));

	}

	public static void walk(TreeNode node) {
		System.out.println(node.getKey() + "=" + node.getValue());
		for (TreeNode tn : node.getChildren())
			walk(tn);
	}
}
