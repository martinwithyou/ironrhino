package org.ironrhino.core.aspect;

import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.ironrhino.common.model.Record;
import org.ironrhino.common.util.AuthzUtils;
import org.ironrhino.core.event.EntityOperationType;
import org.ironrhino.core.model.Entity;
import org.ironrhino.core.model.Recordable;
import org.springframework.core.Ordered;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.security.userdetails.UserDetails;

import com.google.gson.Gson;

/**
 * Use for record model's CRUD operations
 * 
 * @author zhouyanming
 * @see org.ironrhino.core.annotation.RecordAware
 */
@Aspect
public class Recording extends HibernateDaoSupport implements Ordered {

	private int order;

	@Around("execution(* org.ironrhino..service.*Manager.save*(*)) and args(entity) and @args(org.ironrhino.core.annotation.RecordAware)")
	public Object save(ProceedingJoinPoint call, Entity entity)
			throws Throwable {
		if (AopContext.isBypass(this.getClass()))
			return call.proceed();
		boolean isNew = entity.isNew();
		if (entity instanceof Recordable) {
			Recordable r = (Recordable) entity;
			Date date = new Date();
			r.setModifyDate(date);
			if (isNew)
				r.setCreateDate(date);
		}
		Object result = call.proceed();
		record(entity, isNew ? EntityOperationType.CREATE
				: EntityOperationType.UPDATE);
		return result;
	}

	@AfterReturning("execution(* org.ironrhino..service.*Manager.delete*(*)) and args(entity) and @args(org.ironrhino.core.annotation.RecordAware)")
	public void delete(Entity entity) {
		if (AopContext.isBypass(this.getClass()))
			return;
		record(entity, EntityOperationType.DELETE);
	}

	// record to database,may change to use logger system
	private void record(Entity entity, EntityOperationType action) {
		final Record record = new Record();
		UserDetails ud = AuthzUtils.getUserDetails(UserDetails.class);
		if (ud != null) {
			record.setOperatorId(ud.getUsername());
			record.setOperatorClass(ud.getClass().getName());
		}
		try {
			record.setEntityId(String.valueOf(BeanUtils.getProperty(entity,
					"id")));
		} catch (Exception e) {
		}
		record.setEntityClass(entity.getClass().getName());
		record.setEntityToString(new Gson().toJson(entity));
		record.setAction(action.name());
		record.setRecordDate(new Date());
		// important! no transaction,inserted before actual save entity and
		// ignore transaction rollback
		getHibernateTemplate().save(record);
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

}
