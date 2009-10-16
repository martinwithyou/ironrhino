package org.ironrhino.core.fs;

import java.io.File;
import java.io.InputStream;

public interface FileStorage {

	public boolean save(File file, String path);

	public boolean save(InputStream os, String path);

	public InputStream open(String path);

	public boolean get(String path, File local);

	public boolean delete(String path);

}
