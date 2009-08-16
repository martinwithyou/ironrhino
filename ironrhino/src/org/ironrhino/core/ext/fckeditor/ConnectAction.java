package org.ironrhino.core.ext.fckeditor;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.struts2.ServletActionContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ConnectAction extends UploadAction {

	private String command;

	private String newFolderName;

	private String currentFolder;

	public String getNewFolderName() {
		return newFolderName;
	}

	public void setNewFolderName(String newFolderName) {
		this.newFolderName = newFolderName;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getCurrentFolder() {
		return currentFolder;
	}

	public void setCurrentFolder(String currentFolder) {
		this.currentFolder = currentFolder;
	}

	@Override
	public String execute() throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpServletResponse response = ServletActionContext.getResponse();

		if (request.getMethod().equalsIgnoreCase("post"))
			return super.execute();

		response.setContentType("text/xml; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		PrintWriter out = response.getWriter();
		String currentPath = baseDir + type + currentFolder;
		String currentDirPath = ServletActionContext.getServletContext()
				.getRealPath(currentPath);
		File currentDir = new File(currentDirPath);
		if (!currentDir.exists()) {
			if (!currentDir.mkdir())
				throw new Exception("mkdir error for:"
						+ currentDir.getAbsolutePath());
		}
		Document document = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.newDocument();
		Node root = CreateCommonXml(document, command, type, currentFolder,
				request.getContextPath() + currentPath);
		if (command.equals("GetFolders")) {
			getFolders(currentDir, root, document);
		} else if (command.equals("GetFoldersAndFiles")) {
			getFolders(currentDir, root, document);
			getFiles(currentDir, root, document);
		} else if (command.equals("CreateFolder")) {
			File newFolder = new File(currentDir, newFolderName);
			String retValue = "110";
			if (newFolder.exists()) {
				retValue = "101";
			} else {
				try {
					boolean dirCreated = newFolder.mkdir();
					if (dirCreated)
						retValue = "0";
					else
						retValue = "102";
				} catch (SecurityException sex) {
					retValue = "103";
				}
			}
			setCreateFolderResponse(retValue, root, document);
		}
		document.getDocumentElement().normalize();
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();

		DOMSource source = new DOMSource(document);

		StreamResult result = new StreamResult(out);
		transformer.transform(source, result);
		out.flush();
		out.close();
		return NONE;
	}

	private void setCreateFolderResponse(String retValue, Node root,
			Document doc) {
		Element myEl = doc.createElement("Error");
		myEl.setAttribute("number", retValue);
		root.appendChild(myEl);
	}

	private void getFolders(File dir, Node root, Document doc) {
		Element folders = doc.createElement("Folders");
		root.appendChild(folders);
		File[] fileList = dir.listFiles();
		for (int i = 0; i < fileList.length; ++i) {
			if (fileList[i].isDirectory()) {
				Element myEl = doc.createElement("Folder");
				myEl.setAttribute("name", fileList[i].getName());
				folders.appendChild(myEl);
			}
		}
	}

	private void getFiles(File dir, Node root, Document doc) {
		Element files = doc.createElement("Files");
		root.appendChild(files);
		File[] fileList = dir.listFiles();
		for (int i = 0; i < fileList.length; ++i) {
			if (fileList[i].isFile()) {
				Element myEl = doc.createElement("File");
				myEl.setAttribute("name", fileList[i].getName());
				myEl.setAttribute("size", "" + fileList[i].length() / 1024);
				files.appendChild(myEl);
			}
		}
	}

	private Node CreateCommonXml(Document doc, String commandStr,
			String typeStr, String currentPath, String currentUrl) {

		Element root = doc.createElement("Connector");
		doc.appendChild(root);
		root.setAttribute("command", commandStr);
		root.setAttribute("resourceType", typeStr);

		Element myEl = doc.createElement("CurrentFolder");
		myEl.setAttribute("path", currentPath);
		myEl.setAttribute("url", currentUrl);
		root.appendChild(myEl);

		return root;

	}

}
