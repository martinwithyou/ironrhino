package org.ironrhino.core.model;

import java.util.Date;

public interface Recordable {

	public Date getModifyDate();

	public Date getCreateDate();

	public void setModifyDate(Date date);

	public void setCreateDate(Date date);

}
