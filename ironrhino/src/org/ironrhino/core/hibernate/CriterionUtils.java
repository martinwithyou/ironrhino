package org.ironrhino.core.hibernate;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

public class CriterionUtils {

	public static Criterion like(String value, MatchMode mode, String... names) {
		Criterion c = null;
		for (int i = 0; i < names.length; i++) {
			if (i == 0) {
				c = Restrictions.like(names[i], value, MatchMode.ANYWHERE);
			} else {
				c = Restrictions.or(c,
						Restrictions.like(names[i], value, MatchMode.ANYWHERE));
			}
		}
		return c;
	}

}
