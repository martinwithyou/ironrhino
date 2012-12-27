package org.ironrhino.core.fs.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;

public class LocalFileStorage extends AbstractFileStorage {

	private File directory;

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(uri);
		uri = uri.replace('\\', '/');
		uri = uri.replace(" ", "%20");
		this.directory = new File(new URI(uri));
		if (this.directory.isFile())
			throw new RuntimeException(directory + " is not directory");
		if (!this.directory.exists())
			if (!this.directory.mkdirs())
				log.error("mkdir error:" + directory.getAbsolutePath());
	}

	public boolean mkdir(String path) {
		return new File(directory, path).mkdirs();
	}

	public boolean delete(String path) {
		return new File(directory, path).delete();
	}

	public InputStream open(String path, boolean realtime) {
		File target = new File(directory, path);
		if (!target.exists())
			return null;
		try {
			return new FileInputStream(target);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public boolean get(String path, File localFile, boolean realtime) {
		File target = new File(directory, path);
		if (!target.exists())
			return false;
		try {
			FileUtils.copyFile(target, localFile);
			return true;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}

	}

	public boolean save(File file, String path) {
		File dest = new File(directory, path);
		dest.getParentFile().mkdirs();
		return file.renameTo(dest);
	}

	public boolean save(InputStream is, String path) {
		File dest = new File(directory, path);
		if (!dest.getParentFile().mkdirs())
			return false;
		try {
			FileOutputStream os = new FileOutputStream(dest);
			return copy(is, os);
		} catch (IOException e) {
			return false;
		}
	}

	public long getLastModified(String path) {
		return new File(directory, path).lastModified();
	}

	public void setLastModified(String path, long lastModified) {
		try {
			new File(directory, path).setLastModified(lastModified);
		} catch (Exception e) {

		}
	}

	public boolean exists(String path) {
		return new File(directory, path).exists();
	}

	public boolean rename(String fromPath, String toPath) {
		return new File(directory, fromPath).renameTo(new File(directory,
				toPath));
	}

	public boolean isDirectory(String path) {
		return new File(directory, path).isDirectory();
	}

	public List<String> listFiles(String path) {
		final List<String> list = new ArrayList<String>();
		new File(directory, path).listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isFile()) {
					list.add(f.getName());
				}
				return false;
			}
		});
		return list;
	}

	public Map<String, Boolean> listFilesAndDirectory(String path) {
		final Map<String, Boolean> map = new HashMap<String, Boolean>();
		new File(directory, path).listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				map.put(f.getName(), f.isFile());
				return false;
			}
		});
		List<Map.Entry<String, Boolean>> list = new ArrayList<Map.Entry<String, Boolean>>();
		list.addAll(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Boolean>>() {
			@Override
			public int compare(Map.Entry<String, Boolean> o1,
					Map.Entry<String, Boolean> o2) {
				int i = o2.getValue().compareTo(o1.getValue());
				if (i == 0)
					return o1.getKey().compareTo(o2.getKey());
				else
					return i;
			}
		});
		Map<String, Boolean> result = new LinkedHashMap<String, Boolean>();
		for (Map.Entry<String, Boolean> entry : list)
			result.put(entry.getKey(), entry.getValue());
		return result;
	}
}