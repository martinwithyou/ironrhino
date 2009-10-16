package org.ironrhino.core.fs.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.annotation.PostConstruct;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.util.Assert;

public class HdfsFileStorage extends AbstractFileStorage {

	private FileSystem hdfs;

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(uri);
		Configuration conf = new Configuration();
		hdfs = FileSystem.get(new URI(uri), conf);
	}

	public void destroy() {
		if (hdfs != null)
			try {
				hdfs.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
	}

	@Override
	public boolean delete(String path) {
		try {
			return hdfs.delete(new Path(path), true);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public InputStream open(String path) {
		try {
			return hdfs.open(new Path(path), bufferSize);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	@Override
	public boolean get(String path, File localFile) {
		try {
			InputStream is = hdfs.open(new Path(path), bufferSize);
			OutputStream os = new FileOutputStream(localFile);
			return copy(is, os);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean save(File file, String path) {
		try {
			hdfs.copyFromLocalFile(new Path(file.getAbsolutePath()), new Path(
					path));
			return true;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean save(InputStream is, String path) {
		try {
			OutputStream os = hdfs.create(new Path(path), true);
			return copy(is, os);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}
}