package org.ironrhino.core.hibernate;

import java.util.Date;

import org.apache.struts2.ServletActionContext;
import org.compass.core.util.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.model.Persistable;
import org.ironrhino.core.util.DateUtils;
import org.springframework.beans.BeanWrapperImpl;

public class CriterionUtils {

	public static Criterion like(String value, MatchMode mode, String... names) {
		Criterion c = null;
		for (String name : names) {
			if (c == null) {
				c = Restrictions.like(name, value, MatchMode.ANYWHERE);
			} else {
				c = Restrictions.or(c,
						Restrictions.like(name, value, MatchMode.ANYWHERE));
			}
		}
		return c;
	}

	public static Criterion filter(Persistable entity, String... names) {
		if (entity == null || names.length == 0)
			return null;
		BeanWrapperImpl bw = new BeanWrapperImpl(entity);
		String prefix = StringUtils.uncapitalize(entity.getClass()
				.getSimpleName()) + '.';
		Criterion c = null;
		for (String name : names) {
			if (name.startsWith(prefix))
				name = name.substring(prefix.length());
			if (name.indexOf('.') > -1)
				continue;
			Object value = null;
			try {
				if (ServletActionContext.getRequest().getParameter(
						prefix + name) == null)
					continue;
				value = bw.getPropertyValue(name);
			} catch (Exception e) {
				continue;
			}
			if (value == null)
				continue;
			Criterion temp = null;
			if (value instanceof Date)
				temp = Restrictions.between(name,
						DateUtils.beginOfDay((Date) value),
						DateUtils.endOfDay((Date) value));
			else
				temp = Restrictions.eq(name, value);
			if (c == null)
				c = temp;
			else
				c = Restrictions.and(c, temp);
		}
		return c;
	}

}
