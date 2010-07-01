package org.ironrhino.common.action;

import java.io.File;

import javax.inject.Inject;

import org.ironrhino.core.fs.FileStorage;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

@AutoConfig(fileupload = "*/*")
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
	public String input() {
		return INPUT;
	}

	@Override
	@InputConfig
	public String execute() {
		if (file != null) {
			int i = 0;
			for (File f : file) {
				fileStorage.save(f, createPath(fileFileName[i]));
				i++;
			}
			addActionMessage(getText("operate.success"));
		}
		return INPUT;
	}

	private String createPath(String filename) {
		return "/upload/" + filename;
	}
}
