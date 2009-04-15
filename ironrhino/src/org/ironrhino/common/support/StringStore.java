package org.ironrhino.common.support;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

public class StringStore implements BeanNameAware {

	private List<String> buffer = new ArrayList<String>(100);

	private int bufferSize = 100;

	private String beanName;

	private String directory = "/WEB-INF/data/";

	@Autowired
	private ResourceLoader resourceLoader;

	private File file;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock r = rwl.readLock();

	private final Lock w = rwl.writeLock();

	public String getDirectory() {
		return directory.endsWith("/") ? directory : directory + "/";
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		Assert.hasLength(directory);
		file = resourceLoader.getResource(getDirectory() + beanName + ".dat")
				.getFile();
		if (!file.exists())
			file.createNewFile();
	}

	@PreDestroy
	public void destroy() throws Exception {
		flush();
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void store(String string) {
		buffer.add(string);
		if (buffer.size() >= bufferSize)
			flush();
	}

	public void store(String[] array) {
		for (String s : array)
			buffer.add(s);
		if (buffer.size() >= bufferSize)
			flush();
	}

	public void store(Collection<String> collection) {
		buffer.addAll(collection);
		if (buffer.size() >= bufferSize)
			flush();
	}

	public void flush() {
		if (buffer.size() == 0)
			return;
		w.lock();
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(raf.length());
			for (String s : buffer)
				raf.write((s + "\n").getBytes("UTF-8"));
			buffer.clear();
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			w.unlock();
		}
	}

	private void clear() {
		w.lock();
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.setLength(0);
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			w.unlock();
		}
	}

	public  List<String> read() {
		List<String> list = new ArrayList<String>();
		if (!file.canRead() || file.length() == 0)
			return list;
		r.lock();
		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(file, "UTF-8");
			while (it.hasNext())
				list.add(it.nextLine());
		} catch (IOException e) {
		} finally {
			LineIterator.closeQuietly(it);
			r.unlock();
		}
		return list;
	}

	public void consume() {
		flush();
		List<String> list = read();
		if (list.size() > 0) {
			doConsume(list);
			clear();
		}
	}

	protected void doConsume(List<String> list) {

	}

}
