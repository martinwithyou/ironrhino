package org.ironrhino.common.action;

import java.io.File;
import java.io.FileInputStream;

import org.apache.struts2.ServletActionContext;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.BarcodeUtils;
import org.ironrhino.core.util.ErrorMessage;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.Validations;
import com.opensymphony.xwork2.validator.annotations.ValidatorType;

@AutoConfig(fileupload = "image/*")
public class QrcodeAction extends BaseAction {

	private static final long serialVersionUID = 8180265410790553918L;

	private String content;

	private File watermark;

	private int width = 400;

	private int height = 400;

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

	public File getWatermark() {
		return watermark;
	}

	public void setWatermark(File watermark) {
		this.watermark = watermark;
	}

	@Override
	@InputConfig(resultName = "success")
	@Validations(requiredStrings = { @RequiredStringValidator(type = ValidatorType.FIELD, fieldName = "content", trim = true, key = "validation.required") })
	public String execute() {
		try {
			BarcodeUtils.encodeQRCode(content, null, null, width, height,
					ServletActionContext.getResponse().getOutputStream(),
					watermark != null ? new FileInputStream(watermark) : null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ErrorMessage(e.getMessage());
		}
		return NONE;
	}

}
