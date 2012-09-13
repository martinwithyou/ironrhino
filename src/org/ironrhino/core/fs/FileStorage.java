package org.ironrhino.core.fs;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface FileStorage {

	public boolean save(File file, String path);

	public boolean save(File file, String path, boolean deleteFile);

	public boolean save(InputStream is, String path);

	public InputStream open(String path);

	public InputStream open(String path, boolean realtime);

	public boolean get(String path, File local);

	public boolean get(String path, File local, boolean realtime);

	public void write(String path, OutputStream os);

	public void write(String path, OutputStream os, boolean realtime);
	
	public boolean mkdir(String path);

	public boolean delete(String path);

	public long getLastModified(String path);

	public void setLastModified(String path, long lastModified);

	public boolean copy(String path, FileStorage from, FileStorage to);
	
	public boolean exists(String path);

	public boolean rename(String fromPath, String toPath);
	
	public boolean isDirectory(String path);
	
	public List<String> listFiles(String path);
	
	public Map<String,Boolean> listFilesAndDirectory(String path);

}
