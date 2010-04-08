package org.ironrhino.core.fs.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ironrhino.core.fs.FileStorage;
import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractFileStorage implements FileStorage {

	protected Log log = LogFactory.getLog(getClass());

	protected int bufferSize = 512 * 1024;

	@Value("${fileStorage.uri}")
	protected String uri;

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	@Override
	public boolean save(File file, String path, boolean deleteFile) {
		boolean b = save(file, path);
		if (b && file.delete())
			return true;
		return b;
	}

	@Override
	public boolean copy(String path, FileStorage from, FileStorage to) {
		InputStream is = from.open(path);
		if (is == null)
			return false;
		return to.save(is, path);
	}

	@Override
	public InputStream open(String path) {
		return open(path, false);
	}

	@Override
	public boolean get(String path, File local) {
		return get(path, local, false);
	}

	@Override
	public void write(String path, OutputStream os) {
		write(path, os, false);
	}

	@Override
	public void write(String path, OutputStream os, boolean realtime) {
		InputStream is = open(path, realtime);
		copy(is, os);
	}

	protected boolean copy(InputStream is, OutputStream os) {
		try {
			BufferedInputStream bis = new BufferedInputStream(is, bufferSize);
			BufferedOutputStream bos = new BufferedOutputStream(os, bufferSize);
			byte[] buffer = new byte[bufferSize];
			int read;
			while ((read = bis.read(buffer, 0, buffer.length)) != -1)
				bos.write(buffer, 0, read);
			bos.close();
			bis.close();
			return true;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return false;
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			if (os != null)
				try {
					os.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
}