package org.ironrhino.core.hibernate;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.ironrhino.core.model.Displayable;
import org.ironrhino.core.util.DateUtils;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.util.LocalizedTextUtil;

public enum CriterionOperator implements Displayable {

	EQ(1) {
		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			if (value1 == null)
				return null;
			if (value1 instanceof Date && DateUtils.isBeginOfDay((Date) value1))
				return Restrictions.between(name, value1,
						DateUtils.endOfDay((Date) value1));
			else
				return Restrictions.eq(name, value1);
		}
	},
	NEQ(1) {
		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			if (value1 == null)
				return null;
			if (value1 instanceof Date && DateUtils.isBeginOfDay((Date) value1))
				return Restrictions
						.or(Restrictions.lt(name, value1),
								Restrictions.gt(name,
										DateUtils.endOfDay((Date) value1)));
			else
				return Restrictions.not(Restrictions.eq(name, value1));
		}
	},
	LT(1) {
		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			if (value1 == null)
				return null;
			return Restrictions.lt(name, value1);
		}
	},
	LE(1) {
		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			if (value1 == null)
				return null;
			if (value1 instanceof Date && DateUtils.isBeginOfDay((Date) value1))
				return Restrictions.le(name, DateUtils.endOfDay((Date) value1));
			else
				return Restrictions.le(name, value1);
		}
	},
	GT(1) {
		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			if (value1 == null)
				return null;
			if (value1 instanceof Date && DateUtils.isBeginOfDay((Date) value1))
				return Restrictions.gt(name, DateUtils.endOfDay((Date) value1));
			else
				return Restrictions.gt(name, value1);
		}
	},
	GE(1) {
		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			if (value1 == null)
				return null;
			return Restrictions.ge(name, value1);
		}
	},
	BETWEEN(2) {
		@Override
		public boolean isEffective(Class<?> clazz, String value1, String value2) {
			return StringUtils.isNotBlank(value1)
					|| StringUtils.isNotBlank(value2);
		}

		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			if (value2 instanceof Date && DateUtils.isBeginOfDay((Date) value2))
				value2 = DateUtils.endOfDay((Date) value2);
			if (value1 != null && value2 != null)
				return Restrictions.between(name, value1, value2);
			else if (value1 != null)
				return Restrictions.ge(name, value1);
			else if (value2 != null)
				return Restrictions.le(name, value2);
			else
				return null;
		}
	},
	NOTBETWEEN(2) {
		@Override
		public boolean isEffective(Class<?> clazz, String value1, String value2) {
			return StringUtils.isNotBlank(value1)
					|| StringUtils.isNotBlank(value2);
		}

		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			if (value2 instanceof Date && DateUtils.isBeginOfDay((Date) value2))
				value2 = DateUtils.endOfDay((Date) value2);
			if (value1 != null && value2 != null)
				return Restrictions.or(Restrictions.lt(name, value1),
						Restrictions.gt(name, value2));
			else if (value1 != null)
				return Restrictions.lt(name, value1);
			else if (value2 != null)
				return Restrictions.gt(name, value2);
			else
				return null;
		}
	},
	ISNULL(0) {
		@Override
		public boolean isEffective(Class<?> clazz, String value1, String value2) {
			return supports(clazz);
		}

		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			return Restrictions.isNull(name);
		}
	},
	ISNOTNULL(0) {
		@Override
		public boolean isEffective(Class<?> clazz, String value1, String value2) {
			return supports(clazz);
		}

		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			return Restrictions.isNotNull(name);
		}
	},
	ISEMPTY(0) {
		@Override
		public boolean isEffective(Class<?> clazz, String value1, String value2) {
			return supports(clazz);
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(String.class);
		}

		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			return Restrictions.eq(name, "");
		}
	},
	ISNOTEMPTY(0) {
		@Override
		public boolean isEffective(Class<?> clazz, String value1, String value2) {
			return supports(clazz);
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(String.class);
		}

		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			return Restrictions.not(Restrictions.eq(name, ""));
		}
	},
	ISBLANK(0) {
		@Override
		public boolean isEffective(Class<?> clazz, String value1, String value2) {
			return supports(clazz);
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(String.class);
		}

		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			return Restrictions.or(Restrictions.isNull(name),
					Restrictions.eq(name, ""));
		}
	},
	ISNOTBLANK(0) {
		@Override
		public boolean isEffective(Class<?> clazz, String value1, String value2) {
			return supports(clazz);
		}

		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(String.class);
		}

		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			return Restrictions.and(Restrictions.isNotNull(name),
					Restrictions.not(Restrictions.eq(name, "")));
		}
	},
	INCLUDE(0) {

		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(String.class);
		}

		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			return Restrictions.like(name, (String) value1, MatchMode.ANYWHERE);
		}
	},
	NOTINCLUDE(0) {

		@Override
		public boolean supports(Class<?> clazz) {
			return clazz.equals(String.class);
		}

		@Override
		public Criterion operator(String name, Object value1, Object value2) {
			return Restrictions.not(Restrictions.like(name, (String) value1,
					MatchMode.ANYWHERE));
		}
	};

	private int parametersSize;

	private CriterionOperator(int parametersSize) {
		this.parametersSize = parametersSize;
	}

	public String getName() {
		return this.name();
	}

	public String getDisplayName() {
		try {
			return LocalizedTextUtil.findText(getClass(), name(), ActionContext
					.getContext().getLocale(), name(), null);
		} catch (Exception e) {
			return name();
		}
	}

	public static CriterionOperator parse(String name) {
		if (name != null)
			for (CriterionOperator en : values())
				if (name.equals(en.name()) || name.equals(en.getDisplayName()))
					return en;
		return null;
	}

	public abstract Criterion operator(String name, Object value1, Object value2);

	public int getParametersSize() {
		return parametersSize;
	}

	public boolean isEffective(Class<?> clazz, String value1, String value2) {
		if (!supports(clazz))
			return false;
		if (StringUtils.isBlank(value1))
			return false;
		return true;
	}

	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}
}
