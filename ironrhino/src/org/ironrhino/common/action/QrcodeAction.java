package org.ironrhino.common.action;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.BarcodeUtils;
import org.ironrhino.core.util.ErrorMessage;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

@AutoConfig(fileupload = "image/*")
public class QrcodeAction extends BaseAction {

	private static final long serialVersionUID = 8180265410790553918L;

	private boolean decode = false;

	private String content;

	private String encoding = "UTF-8";

	private String url;

	private File file;

	private int width = 400;

	private int height = 400;

	public boolean isDecode() {
		return decode;
	}

	public void setDecode(boolean decode) {
		this.decode = decode;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	@Override
	@InputConfig(resultName = "success")
	public String execute() {
		try {
			if (decode) {
				if (file == null && StringUtils.isBlank(url)) {
					if (file == null) {
						addFieldError("file", getText("validation.required"));
						return SUCCESS;
					}
					if (url == null) {
						addFieldError("url", getText("validation.required"));
						return SUCCESS;
					}
				}
				if (file != null)
					content = BarcodeUtils.decode(new FileInputStream(file));
				else if (url != null)
					content = BarcodeUtils.decode(url);
				return SUCCESS;
			} else {
				if (content == null) {
					addFieldError("content", getText("validation.required"));
					return SUCCESS;
				}
				BarcodeUtils.encodeQRCode(content, null, null, width, height,
						ServletActionContext.getResponse().getOutputStream(),
						file != null ? new FileInputStream(file) : null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ErrorMessage(e.getMessage());
		}
		return NONE;
	}

}
