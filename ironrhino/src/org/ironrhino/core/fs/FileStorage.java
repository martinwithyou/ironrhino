package org.ironrhino.core.fs;

import java.io.File;
import java.io.InputStream;

public interface FileStorage {

	public boolean save(File file, String path);

	public boolean save(File file, String path, boolean deleteFile);

	public boolean save(InputStream is, String path);

	public InputStream open(String path);

	public InputStream open(String path, boolean realtime);

	public boolean get(String path, File local);

	public boolean get(String path, File local, boolean realtime);

	public boolean delete(String path);

	public long getLastModified(String path);

	public void setLastModified(String path, long lastModified);

	public boolean copy(String path, FileStorage from, FileStorage to);

}
