package org.ironrhino.core.fs.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.annotation.PostConstruct;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.ironrhino.core.fs.FileStorage;
import org.springframework.util.Assert;

public class HdfsFileStorage extends AbstractFileStorage {

	private FileSystem hdfs;

	private FileStorage cache;

	private int cacheTimeToLive = 60 * 60 * 1000; // in milliseconds

	public void setCache(FileStorage cache) {
		this.cache = cache;
	}

	public void setCacheTimeToLive(int cacheTimeToLive) {
		this.cacheTimeToLive = cacheTimeToLive;
	}

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

	public boolean delete(String path) {
		try {
			boolean b = hdfs.delete(new Path(path), true);
			if (b && cache != null)
				cache.delete(path);
			return b;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	public boolean rename(String fromPath, String toPath) {
		try {
			boolean b = hdfs.rename(new Path(fromPath), new Path(toPath));
			if (b && cache != null) {
				cache.delete(fromPath);
			}
			return b;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	public InputStream open(String path, boolean realtime) {
		try {
			if (cache == null)
				return hdfs.open(new Path(path), bufferSize);

			InputStream is = null;
			long now = System.currentTimeMillis();
			long cacheLastModified = cache.getLastModified(path);
			long realLastModified = 0;
			if (cacheLastModified > 0) {
				if (realtime) {
					realLastModified = getLastModified(path);
					if (realLastModified == cacheLastModified)
						is = cache.open(path);
				} else {
					if (now - cacheLastModified < cacheTimeToLive) {
						is = cache.open(path);
					} else {
						realLastModified = getLastModified(path);
						if (realLastModified <= cacheLastModified) {
							cache.setLastModified(path, cacheLastModified
									+ cacheTimeToLive);
							is = cache.open(path);
						}
					}
				}
			}
			if (is == null) {
				is = hdfs.open(new Path(path), bufferSize);
				if (is != null) {
					cache.save(is, path);
					cache.setLastModified(path, getLastModified(path));
					is = cache.open(path);
				}
			}
			return is;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public boolean get(String path, File localFile, boolean realtime) {
		try {
			InputStream is = open(path);
			OutputStream os = new FileOutputStream(localFile, realtime);
			return copy(is, os);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

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

	public boolean save(InputStream is, String path) {
		try {
			OutputStream os = hdfs.create(new Path(path), true);
			return copy(is, os);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	public long getLastModified(String path) {
		try {
			FileStatus status = hdfs.getFileStatus(new Path(path));
			return status.getModificationTime();
		} catch (FileNotFoundException e) {
			return 0;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return 0;
		}
	}

	public void setLastModified(String path, long lastModified) {
		try {
			hdfs.setTimes(new Path(path), lastModified, -1);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

}