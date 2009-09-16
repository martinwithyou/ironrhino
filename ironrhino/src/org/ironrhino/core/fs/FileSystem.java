package org.ironrhino.core.fs;

import java.io.File;
import java.io.InputStream;

public interface FileSystem {

	public String save(File localFile);

	public String save(InputStream os);

	public void save(File file, String filename);

	public void save(InputStream os, String filename);

	public InputStream get(String filename);

	public boolean get(String filename, File localFile);

	public boolean delete(String filename);

}
