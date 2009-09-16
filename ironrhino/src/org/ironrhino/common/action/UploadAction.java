package org.ironrhino.common.action;

import java.io.File;

import org.ironrhino.core.ext.struts.BaseAction;
import org.ironrhino.core.fs.FileSystem;
import org.ironrhino.core.metadata.AutoConfig;

@AutoConfig(namespace = "/", fileupload = "*/*")
public class UploadAction extends BaseAction {

	private static final long serialVersionUID = 625509291613761721L;

	private File[] file;

	private transient FileSystem fileSystem;

	public void setFile(File[] file) {
		this.file = file;
	}

	public void setFileSystem(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	public String execute() {
		if (file != null) {
			for (File f : file)
				fileSystem.save(f);
		}
		return NONE;
	}

}
