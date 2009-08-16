package org.ironrhino.core.ext.fckeditor;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

public class UploadAction extends ActionSupport {

	protected String baseDir = "/uploadfiles/";

	protected String type = "file";

	protected String newFileFileName;

	protected String newFileContentType;

	protected File newFile;

	public File getNewFile() {
		return newFile;
	}

	public void setNewFile(File newFile) {
		this.newFile = newFile;
	}

	public String getNewFileContentType() {
		return newFileContentType;
	}

	public void setNewFileContentType(String newFileContentType) {
		this.newFileContentType = newFileContentType;
	}

	public String getNewFileFileName() {
		return newFileFileName;
	}

	public void setNewFileFileName(String newFileFileName) {
		this.newFileFileName = newFileFileName;
	}

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String execute() throws Exception {
		if (newFile == null || newFileFileName.endsWith(".jsp"))
			return NONE;
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();

		String currentDirPath = ServletActionContext.getServletContext()
				.getRealPath(baseDir + type);
		File dir = new File(currentDirPath);
		if(!dir.exists()&&!dir.mkdirs())
			return NONE;
		String retVal = "0";
		String newName = "";
		String fileUrl = "";
		String errorMessage = "";

		String nameWithoutExt = newFileFileName.substring(0, newFileFileName
				.lastIndexOf('.'));
		String ext = newFileFileName
				.substring(newFileFileName.lastIndexOf('.') + 1);
		File pathToSave = new File(currentDirPath, newFileFileName);
		int counter = 1;
		while (pathToSave.exists()) {
			newName = nameWithoutExt + "(" + counter + ")" + "." + ext;
			retVal = "201";
			pathToSave = new File(currentDirPath, newName);
			counter++;
		}
		boolean success = newFile.renameTo(pathToSave);
		if (success) {
			out.println("<script type=\"text/javascript\">");
			out.println("window.parent.OnUploadCompleted(" + retVal + ",'"
					+ fileUrl + "','" + newName + "','" + errorMessage + "');");
			out.println("</script>");
			out.flush();
			out.close();
		}
		return NONE;

	}

}
