package org.ironrhino.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TextFileIterator<T> implements Iterator<T> {

	int index;

	List<File> files = new ArrayList<File>();

	String encoding;

	FileInputStream fis;

	BufferedReader br;

	String nextline;

	public TextFileIterator(File... files) {
		for (File f : files)
			if (f != null && f.exists() && f.length() > 0)
				this.files.add(f);
		if (this.files.size() == 0)
			throw new RuntimeException("no file available");
		try {
			openFile();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public TextFileIterator(String encoding, File... files) {
		this(files);
		this.encoding = encoding;
	}

	private void openFile() {
		try {
			fis = new FileInputStream(files.get(index));
			if (encoding == null)
				br = new BufferedReader(new InputStreamReader(fis));
			else
				br = new BufferedReader(new InputStreamReader(fis, encoding));
			nextline = br.readLine();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasNext() {
		return nextline != null || index < files.size() - 1;
	}

	public T next() {
		try {
			if (nextline == null && index < files.size() - 1) {
				index++;
				openFile();
			}
			String result = nextline;
			if (nextline != null) {
				nextline = br.readLine();
				if (nextline == null) {
					br.close();
					fis.close();
				}
			}
			return transform(result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	protected T transform(String s) {
		// hook to be override
		return (T) s;
	}

}