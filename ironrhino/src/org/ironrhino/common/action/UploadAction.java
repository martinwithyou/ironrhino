package org.ironrhino.common.action;

import java.io.File;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.core.fs.FileStorage;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

@AutoConfig(fileupload = "image/*,text/*,application/x-shockwave-flash,application/pdf")
public class UploadAction extends BaseAction {

	private static final long serialVersionUID = 625509291613761721L;

	private File[] file;

	private String[] fileFileName;

	private String folder;

	private boolean rename;

	@Inject
	private transient FileStorage fileStorage;

	public boolean getRename() {
		return rename;
	}

	public void setRename(boolean rename) {
		this.rename = rename;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public String getFolder() {
		return folder;
	}

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
		String dir;
		if (StringUtils.isBlank(folder))
			dir = "/upload/";
		else
			dir = "/upload/" + folder + "/";
		String path = dir + filename;
		if (rename) {
			boolean exists = fileStorage.exists(path);
			int i = 2;
			while (exists) {
				path = dir + "(" + (i++) + ")" + filename;
				exists = fileStorage.exists(path);
			}
		}
		return path;
	}
}
