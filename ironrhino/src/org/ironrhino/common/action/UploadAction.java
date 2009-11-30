package org.ironrhino.common.action;

import java.io.File;

import javax.inject.Inject;

import org.ironrhino.core.fs.FileStorage;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;

@AutoConfig(namespace = "/", fileupload = "*/*")
public class UploadAction extends BaseAction {

	private static final long serialVersionUID = 625509291613761721L;

	private File[] file;

	private String[] fileFileName;

	@Inject
	private transient FileStorage fileStorage;

	public void setFile(File[] file) {
		this.file = file;
	}

	public void setFileFileName(String[] fileFileName) {
		this.fileFileName = fileFileName;
	}

	@Override
	public String execute() {
		if (file != null) {
			int i = 0;
			for (File f : file) {
				fileStorage.save(f, createPath(fileFileName[i]));
				i++;
			}
		}
		return NONE;
	}

	private String createPath(String filename) {
		return "/upload/" + System.nanoTime() + "-" + filename;
	}
}
