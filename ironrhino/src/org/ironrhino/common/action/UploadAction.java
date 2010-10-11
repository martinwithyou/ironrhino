package org.ironrhino.common.action;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.support.SettingControl;
import org.ironrhino.core.fs.FileStorage;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

@AutoConfig(fileupload = "image/*,text/*,application/x-shockwave-flash,application/pdf,application/msword,application/vnd.ms-powerpoint")
public class UploadAction extends BaseAction {

	private static final long serialVersionUID = 625509291613761721L;

	public static final String UPLOAD_DIR = "/upload";

	private File[] file;

	private String[] fileFileName;

	private String folder;

	private boolean autorename;

	private Map<String, Boolean> files;

	@Inject
	private transient FileStorage fileStorage;

	@Inject
	private transient SettingControl settingControl;

	public Map<String, Boolean> getFiles() {
		return files;
	}

	public boolean isAutorename() {
		return autorename;
	}

	public void setAutorename(boolean autorename) {
		this.autorename = autorename;
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

	public String getFileStoragePath() {
		return settingControl.getStringValue("fileStorage.path", "/assets");
	}

	@Override
	public String input() {
		return INPUT;
	}

	@Override
	@InputConfig(methodName = "list")
	public String execute() {
		if (file != null) {
			int i = 0;
			String[] arr = settingControl
					.getStringArray("upload.excludeSuffix");
			if (arr == null || arr.length == 0)
				arr = "jsp,jspx,php,asp,rb,py,sh".split(",");
			List<String> excludes = Arrays.asList(arr);
			for (File f : file) {
				String fn = fileFileName[i];
				String suffix = fn.substring(fn.lastIndexOf('.') + 1);
				if (!excludes.contains(suffix))
					fileStorage.save(f, createPath(fn, autorename));
				i++;
			}
			addActionMessage(getText("operate.success"));
		}
		return list();
	}

	public String list() {
		if (folder == null) {
			folder = getUid();
			if (folder != null) {
				folder = folder.replace("__", "..");
				try {
					folder = new URI(folder).normalize().toString();
					if (folder.contains(".."))
						folder = "";
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			} else {
				folder = "";
			}
			if (folder.length() > 0 && !folder.startsWith("/"))
				folder = "/" + folder;
		}
		files = fileStorage.listFilesAndDirectory(UPLOAD_DIR + folder);
		if (StringUtils.isNotBlank(folder))
			files.put("..", Boolean.FALSE);
		return LIST;
	}

	public String delete() {
		String path = getUid();
		if (path != null) {
			folder = path.substring(0, path.lastIndexOf('/'));
			boolean b = fileStorage.delete(UPLOAD_DIR + "/" + path);
			if (!b)
				addActionError(getText("delete.forbidden",
						new String[] { path }));
		}
		return list();
	}

	public String mkdir() {
		String path = getUid();
		if (path != null) {
			if (!path.startsWith("/"))
				path = "/" + path;
			folder = path;
			fileStorage.mkdir(UPLOAD_DIR + "/" + folder);
		}
		return list();
	}

	private String createPath(String filename, boolean autorename) {
		String dir = UPLOAD_DIR + "/";
		if (StringUtils.isNotBlank(folder))
			dir = dir + folder + "/";
		String path = dir + filename;
		if (autorename) {
			boolean exists = fileStorage.exists(path);
			int i = 2;
			while (exists) {
				path = dir + "(" + (i++) + ")" + filename;
				exists = fileStorage.exists(path);
			}
		}
		path = org.ironrhino.core.util.StringUtils.compressRepeatSlash(path);
		return path;
	}
}
