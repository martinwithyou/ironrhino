package org.ironrhino.common.model;

import java.util.Date;

import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.Readonly;
import org.ironrhino.core.model.BaseEntity;

@Readonly
@AutoConfig
public class Record extends BaseEntity {

	private static final long serialVersionUID = -8287907984213799302L;

	private String operatorId;

	private String operatorClass;

	private String entityId;

	private String entityClass;

	private String entityToString;

	private String action;

	private Date recordDate;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getEntityClass() {
		return entityClass;
	}

	public void setEntityClass(String entityClass) {
		this.entityClass = entityClass;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getEntityToString() {
		return entityToString;
	}

	public void setEntityToString(String entityToString) {
		this.entityToString = entityToString;
	}

	public Date getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(Date recordDate) {
		this.recordDate = recordDate;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getOperatorClass() {
		return operatorClass;
	}

	public void setOperatorClass(String operatorClass) {
		this.operatorClass = operatorClass;
	}

}
