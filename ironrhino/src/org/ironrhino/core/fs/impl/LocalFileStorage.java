package org.ironrhino.core.fs.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.springframework.util.Assert;

//@Component("fileStorage")
public class LocalFileStorage extends AbstractFileStorage {

	private File directory;

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(uri);
		this.directory = new File(new URI(uri));
		if (this.directory.isFile())
			throw new RuntimeException(directory + " is not directory");
		if (!this.directory.exists())
			if (!this.directory.mkdirs())
				log.error("mkdir error:" + directory.getAbsolutePath());
	}

	@Override
	public boolean delete(String filename) {
		return new File(directory, filename).delete();
	}

	@Override
	public InputStream open(String filename) {
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
	public boolean save(File file, String filename) {
		File dest = new File(directory, filename);
		return file.renameTo(dest);
	}

	@Override
	public boolean save(InputStream is, String filename) {
		File dest = new File(directory, filename);
		try {
			FileOutputStream os = new FileOutputStream(dest);
			return copy(is, os);
		} catch (IOException e) {
			return false;
		}
	}

}
