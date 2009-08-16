package org.ironrhino.core.monitor.analysis;

import java.io.File;
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

public class CumulativeAnalyzer extends FileAnalyzer {

	private Map<String, List<TreeNode>> data = new HashMap<String, List<TreeNode>>();

	public CumulativeAnalyzer() throws FileNotFoundException {
		super();
	}

	public CumulativeAnalyzer(Date start, Date end, boolean excludeEnd)
			throws FileNotFoundException {
		super(start, end, excludeEnd);
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

	public CumulativeAnalyzer(File... files) {
		super(files);
	}

	public CumulativeAnalyzer(File file) {
		super(file);
	}

	public CumulativeAnalyzer(Iterator<KeyValuePair>... iterators) {
		super(iterators);
	}

	public Map<String, List<TreeNode>> getData() {
		return data;
	}

	public Iterator<KeyValuePair> iterate() {
		return iterator;
	}

	protected void process(KeyValuePair pair) {
		if (!pair.getKey().isCumulative())
			return;
		List<TreeNode> list = data.get(pair.getKey().getNamespace());
		if (list == null)
			list = new ArrayList<TreeNode>();
		data.put(pair.getKey().getNamespace(), list);
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

	protected void postAnalyze() {

		for (List<TreeNode> topTreeNodes : data.values()) {
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
			for (TreeNode topNode : topTreeNodes) {
				TreeWalker.walk(topNode, new TreeWalker.Visitor() {
					int id;

					public void visit(TreeNode node) {
						node.setId(++id);
						if (node.isLeaf())
							return;
						for (TreeNode n : node.getChildren()) {
							n.setParent(node);
							if (n.getValue().getLongValue() > 0)
								n.setLongPercent(NumberUtils.formatPercent(
										((double) n.getValue().getLongValue())
												/ node.getValue()
														.getLongValue(), 2));
							if (n.getValue().getDoubleValue() > 0)
								n.setDoublePercent(NumberUtils.formatPercent(n
										.getValue().getDoubleValue()
										/ node.getValue().getDoubleValue(), 2));
						}
					}
				});
			}
		}
		// sort map by namespace
		Map<String, List<TreeNode>> linked = new LinkedHashMap<String, List<TreeNode>>();
		List<String> namespaces = new ArrayList();
		namespaces.addAll(data.keySet());
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
			linked.put(namespace, data.get(namespace));
		data = linked;
	}

}