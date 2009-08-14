package org.ironrhino.core.monitor.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.Value;

public class CumulativeStatAnalyzer extends StatAnalyzer {

	private List<TreeNode> topTreeNodes = new ArrayList<TreeNode>();

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

	public List<TreeNode> getTopTreeNodes() {
		return topTreeNodes;
	}

	protected void process(Key key, Value value, Date date) {
		if (!key.isCumulative())
			return;
		List<TreeNode> list = topTreeNodes;
		int level = key.getLevel();
		for (int i = 1; i <= level; i++) {
			Key cur = key.parent(i);
			TreeNode node = null;
			boolean contains = false;
			for (int j = 0; j < list.size(); j++) {
				node = list.get(j);
				if (cur.equals(node.getKey())) {
					if (i == level)
						node.getValue().cumulate(value);
					contains = true;
					break;
				}
			}
			if (!contains) {
				node = new TreeNode();
				node.setKey(cur);
				node.setValue(i == level ? value : new Value());
				list.add(node);
			}
			list = node.getChildren();
		}

	}

	protected void postAnalyze() {
		for (TreeNode topNode : topTreeNodes) {
			TreeWalker.walk(topNode, new TreeWalker.Visitor() {
				public void visit(TreeNode node) {
					if (!node.isLeaf()) {
						Value v = node.getValue();
						if (v == null) {
							v = new Value();
							node.setValue(v);
						}
						List<TreeNode> children = node.getChildren();
						Collections.sort(children, new Comparator<TreeNode>() {
							public int compare(TreeNode o1, TreeNode o2) {
								return o1.getKey().compareTo(o2.getKey());
							}
						});
						for (TreeNode n : children){
							n.setParent(node);
							v.cumulate(n.getValue());
						}
					}
				}
			}, true);
		}

		for (TreeNode topNode : topTreeNodes) {
			TreeWalker.walk(topNode, new TreeWalker.Visitor() {
				int id;

				public void visit(TreeNode node) {
					node.setId(++id);
				}
			});
		}
	}

}
