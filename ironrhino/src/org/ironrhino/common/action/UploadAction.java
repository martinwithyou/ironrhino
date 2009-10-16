package org.ironrhino.common.action;

import java.io.File;
import java.util.UUID;

import org.ironrhino.core.fs.FileStorage;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.springframework.beans.factory.annotation.Autowired;

@AutoConfig(namespace = "/", fileupload = "*/*")
public class UploadAction extends BaseAction {

	private static final long serialVersionUID = 625509291613761721L;

	private File[] file;

	private String[] fileFileName;

	@Autowired
	private transient FileStorage fileStorage;

	public void setFile(File[] file) {
		this.file = file;
	}

	public void setFileFileName(String[] fileFileName) {
		this.fileFileName = fileFileName;
	}

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
		return "/upload/" + UUID.randomUUID().toString().replaceAll("-", "")
				+ "-" + filename;
	}
}
