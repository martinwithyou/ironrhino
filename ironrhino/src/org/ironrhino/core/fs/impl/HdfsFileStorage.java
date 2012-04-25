package org.ironrhino.core.fs.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.ironrhino.core.fs.FileStorage;
import org.ironrhino.core.metadata.ClusterProfile;
import org.springframework.util.Assert;

@Singleton
@Named("fileStorage")
@ClusterProfile
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
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public boolean mkdir(String path) {
		try {
			return hdfs.mkdirs(new Path(path));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public boolean exists(String path) {
		try {
			if (cache != null)
				return cache.exists(path);
			return hdfs.exists(new Path(path));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e.getCause());
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
			throw new RuntimeException(e.getMessage(), e.getCause());
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
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public boolean get(String path, File localFile, boolean realtime) {
		try {
			InputStream is = open(path);
			OutputStream os = new FileOutputStream(localFile, realtime);
			return copy(is, os);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public boolean save(File file, String path) {
		try {
			hdfs.copyFromLocalFile(new Path(file.getAbsolutePath()), new Path(
					path));
			return true;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public boolean save(InputStream is, String path) {
		try {
			OutputStream os = hdfs.create(new Path(path), true);
			return copy(is, os);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e.getCause());
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

	public boolean isDirectory(String path) {
		try {
			return hdfs.getFileStatus(new Path(path)).isDirectory();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public List<String> listFiles(String path) {
		try {
			final List<String> list = new ArrayList<String>();
			for (FileStatus fs : hdfs.listStatus(new Path(path))) {
				if (!fs.isDirectory()) {
					list.add(fs.getPath().getName());
				}
			}
			return list;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public Map<String, Boolean> listFilesAndDirectory(String path) {
		try {
			final Map<String, Boolean> map = new LinkedHashMap<String, Boolean>();
			FileStatus[] arr = hdfs.listStatus(new Path(path));
			List<FileStatus> list = new ArrayList<FileStatus>(arr.length);
			for (FileStatus fs : arr)
				list.add(fs);
			Collections.sort(list, new Comparator<FileStatus>() {
				@Override
				public int compare(FileStatus o1, FileStatus o2) {
					int i = Boolean.valueOf(o1.isDirectory()).compareTo(
							Boolean.valueOf(o2.isDirectory()));
					if (i == 0)
						return o1.getPath().getName()
								.compareTo(o2.getPath().getName());
					else
						return i;
				}
			});
			for (FileStatus fs : list) {
				map.put(fs.getPath().getName(), !fs.isDirectory());
			}
			return map;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}
}