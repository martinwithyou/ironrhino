package org.ironrhino.core.monitor.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.ironrhino.core.monitor.KeyValuePair;

public class CumulativeFileAnalyzer extends FileAnalyzer {

	private Map<String, List<TreeNode>> data;

	public CumulativeFileAnalyzer() throws FileNotFoundException {
		super();
	}

	public CumulativeFileAnalyzer(Date start, Date end, boolean excludeEnd)
			throws FileNotFoundException {
		super(start, end, excludeEnd);
	}

	public CumulativeFileAnalyzer(Date start, Date end)
			throws FileNotFoundException {
		super(start, end);
	}

	public CumulativeFileAnalyzer(Date date) throws FileNotFoundException {
		super(date);
	}

	public CumulativeFileAnalyzer(Date[] dates) throws FileNotFoundException {
		super(dates);
	}

	public CumulativeFileAnalyzer(File... files) throws FileNotFoundException {
		super(files);
	}

	public CumulativeFileAnalyzer(File file) throws FileNotFoundException {
		super(file);
	}

	public Map<String, List<TreeNode>> getData() {
		return data;
	}

	public void analyze() {
		CumulativeAnalyzer cla = new CumulativeAnalyzer(iterate());
		cla.analyze();
		data = cla.getData();
	}

	@Override
	protected void process(KeyValuePair pair) {
		// donothing

	}

}
