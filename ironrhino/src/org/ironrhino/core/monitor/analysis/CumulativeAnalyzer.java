package org.ironrhino.core.monitor.analysis;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ironrhino.common.util.NumberUtils;
import org.ironrhino.core.monitor.Key;
import org.ironrhino.core.monitor.KeyValuePair;
import org.ironrhino.core.monitor.Value;

public class CumulativeAnalyzer extends
		AbstractAnalyzer<Map<String, List<TreeNode>>> {

	private Map<String, List<TreeNode>> result = new HashMap<String, List<TreeNode>>();

	public CumulativeAnalyzer() throws FileNotFoundException {
		super();
	}

	public CumulativeAnalyzer(Date start, Date end)
			throws FileNotFoundException {
		super(start, end);
	}

	public CumulativeAnalyzer(Date date) throws FileNotFoundException {
		super(date);
	}

	public CumulativeAnalyzer(Date[] dates) throws FileNotFoundException {
		super(dates);
	}

	public CumulativeAnalyzer(Iterator<? extends KeyValuePair> iterator) {
		super(iterator);
	}

	@Override
	public Map<String, List<TreeNode>> getResult() {
		return result;
	}

	@Override
	protected void process(KeyValuePair pair) {
		if (!pair.getKey().isCumulative())
			return;
		List<TreeNode> list = result.get(pair.getKey().getNamespace());
		if (list == null)
			list = new ArrayList<TreeNode>();
		result.put(pair.getKey().getNamespace(), list);
		int level = pair.getKey().getLevel();
		for (int i = 1; i <= level; i++) {
			Key cur = pair.getKey().parent(i);
			TreeNode node = null;
			boolean contains = false;
			for (int j = 0; j < list.size(); j++) {
				node = list.get(j);
				if (cur.equals(node.getKey())) {
					if (i == level)
						node.getValue().cumulate(pair.getValue());
					contains = true;
					break;
				}
			}
			if (!contains) {
				node = new TreeNode();
				node.setKey(cur);
				node.setValue(i == level ? pair.getValue() : new Value());
				list.add(node);
			}
			list = node.getChildren();
		}

	}

	@Override
	protected void postAnalyze() {

		for (List<TreeNode> topTreeNodes : result.values()) {
			// sort children,set and cumulate to parent
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
							Collections.sort(children,
									new Comparator<TreeNode>() {
										public int compare(TreeNode o1,
												TreeNode o2) {
											return o1.getKey().compareTo(
													o2.getKey());
										}
									});
							for (TreeNode n : children) {
								n.setParent(node);
								v.cumulate(n.getValue());
							}
						}
					}
				}, true);

			}
			// set Id and caculate percent
			final CumulativeAnalyzer ca = this;
			TreeWalker.Visitor vistor = new TreeWalker.Visitor() {
				public void visit(TreeNode node) {
					node.setId(ca.generateId());
					if (node.isLeaf())
						return;
					for (TreeNode n : node.getChildren()) {
						n.setParent(node);
						if (n.getValue().getLongValue() > 0)
							n
									.setLongPercent(NumberUtils.formatPercent(
											((double) n.getValue()
													.getLongValue())
													/ node.getValue()
															.getLongValue(), 2));
						if (n.getValue().getDoubleValue() > 0)
							n.setDoublePercent(NumberUtils.formatPercent(n
									.getValue().getDoubleValue()
									/ node.getValue().getDoubleValue(), 2));
					}
				}
			};
			for (TreeNode topNode : topTreeNodes) {
				TreeWalker.walk(topNode, vistor);
			}
		}
		// sort map by namespace
		Map<String, List<TreeNode>> linked = new LinkedHashMap<String, List<TreeNode>>();
		List<String> namespaces = new ArrayList();
		namespaces.addAll(result.keySet());
		Collections.sort(namespaces, new Comparator<String>() {
			public int compare(String o1, String o2) {
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;
				return o1.compareTo(o2);
			}

		});
		for (String namespace : namespaces)
			linked.put(namespace, result.get(namespace));
		result = linked;
	}

	int id;

	public int generateId() {
		return ++id;
	}

}