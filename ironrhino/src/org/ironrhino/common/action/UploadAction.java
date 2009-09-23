package org.ironrhino.common.action;

import java.io.File;

import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.fs.FileStorage;
import org.ironrhino.core.metadata.AutoConfig;
import org.springframework.beans.factory.annotation.Autowired;

@AutoConfig(namespace = "/", fileupload = "*/*")
public class UploadAction extends BaseAction {

	private static final long serialVersionUID = 625509291613761721L;

	private File[] file;

	private String[] filenames;

	@Autowired
	private transient FileStorage fileStorage;

	public String[] getFilenames() {
		return filenames;
	}

	public void setFile(File[] file) {
		this.file = file;
	}

	public String execute() {
		if (file != null) {
			filenames = new String[file.length];
			int i = 0;
			for (File f : file)
				filenames[i++] = fileStorage.save(f);
		}
		return NONE;
	}
}
