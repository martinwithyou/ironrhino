package org.ironrhino.core.fs.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.fs.FileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component("fileStorage")
public class LocalFileStorage implements FileStorage {

	private Log log = LogFactory.getLog(getClass());

	@Autowired
	private ResourceLoader resourceLoader;

	private File directory;

	private String path = "/uploadfiles";

	@PostConstruct
	public void afterPropertiesSet() throws IOException {
		this.directory = resourceLoader.getResource(path).getFile();
		if (this.directory.isFile())
			throw new RuntimeException(directory + " is not directory");
		if (!this.directory.exists())
			if (!this.directory.mkdirs())
				log.error("mkdir error:" + directory.getAbsolutePath());
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Override
	public boolean delete(String filename) {
		return new File(directory, filename).delete();
	}

	@Override
	public InputStream get(String filename) {
		File dest = new File(directory, filename);
		if (!dest.exists())
			return null;
		try {
			return new FileInputStream(dest);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public boolean get(String filename, File localFile) {
		File dest = new File(directory, filename);
		if (!dest.exists())
			return false;
		try {
			FileUtils.copyFile(dest, localFile);
			return true;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}

	}

	@Override
	public String save(File localFile) {
		String filename = newName();
		File dest = new File(directory, filename);
		boolean b = localFile.renameTo(dest);
		if (b)
			return filename;
		else
			throw new RuntimeException("rename to '" + dest.getAbsolutePath()
					+ "' error");
	}

	@Override
	public String save(InputStream is) {
		String name = newName();
		File dest = new File(directory, name);
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(dest);
			IOUtils.copy(is, os);
			return name;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				if (os != null)
					os.close();
				if (is != null)
					is.close();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void save(File file, String filename) {
		if (StringUtils.isBlank(filename))
			throw new RuntimeException("filename is blank");
		File dest = new File(directory, filename);
		if (!file.renameTo(dest))
			throw new RuntimeException("rename to '" + dest.getAbsolutePath()
					+ "' error");
	}

	@Override
	public void save(InputStream is, String filename) {
		if (StringUtils.isBlank(filename))
			throw new RuntimeException("filename is blank");
		File dest = new File(directory, filename);
		FileOutputStream os = null;
		try {
			os = new FileOutputStream(dest);
			IOUtils.copy(is, os);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			try {
				if (os != null)
					os.close();
				if (is != null)
					is.close();
			} catch (Exception e) {
			}
		}
	}

	protected String newName() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
