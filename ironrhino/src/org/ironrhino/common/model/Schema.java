package org.ironrhino.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.compass.annotations.Index;
import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableComponent;
import org.compass.annotations.SearchableProperty;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.NaturalId;
import org.ironrhino.core.metadata.NotInCopy;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.model.BaseEntity;
import org.ironrhino.core.util.JsonUtils;
import org.ironrhino.security.model.UserRole;

@AutoConfig(searchable = true, order = "name asc")
@Searchable(alias = "schema")
@Authorize(ifAnyGranted = UserRole.ROLE_ADMINISTRATOR)
public class Schema extends BaseEntity {

	private static final long serialVersionUID = -8352037604269012984L;

	@NaturalId(caseInsensitive = true, mutable = true)
	@SearchableProperty(boost = 3)
	@UiConfig(displayOrder = 1)
	private String name;

	@SearchableProperty(boost = 3)
	@UiConfig(displayOrder = 2)
	private String description;

	@UiConfig(displayOrder = 3)
	private boolean strict;

	@SearchableComponent
	// @UiConfig(displayOrder = 4, cellEdit = "none", excludeIfNotEdited = true, template = "${entity.getFieldsAsString()!}")
	@UiConfig(displayOrder = 4, hideInList = true)
	private List<Field> fields = new ArrayList<Field>();

	public Schema() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}

	@NotInCopy
	public String getFieldsAsString() {
		if (fields == null)
			return null;
		Set<String> names = new HashSet<String>();
		Iterator<Field> it = fields.iterator();
		while (it.hasNext()) {
			Field f = it.next();
			if (names.contains(f.getName()) || StringUtils.isBlank(f.getName())) {
				it.remove();
				continue;
			}
			Set<String> values = new HashSet<String>();
			Iterator<String> it2 = f.getValues().iterator();
			while (it2.hasNext()) {
				String value = it2.next();
				if (values.contains(value) || StringUtils.isBlank(value)) {
					it2.remove();
					continue;
				}
				values.add(value);
			}
			if (f.getValues().isEmpty()) {
				it.remove();
				continue;
			}
			names.add(f.getName());
		}
		if (fields.isEmpty())
			return null;
		return JsonUtils.toJson(fields);
	}

	public void setFieldsAsString(String str) {
		if (StringUtils.isNotBlank(str))
			try {
				fields = JsonUtils.fromJson(str,
						new TypeReference<List<Field>>() {
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public Schema merge(Schema other) {
		if (other != null)
			fields.addAll(other.getFields());
		Set<String> names = new HashSet<String>();
		Iterator<Field> it = fields.iterator();
		while (it.hasNext()) {
			Field f = it.next();
			if (names.contains(f.getName()) || StringUtils.isBlank(f.getName())) {
				it.remove();
				continue;
			}
			names.add(f.getName());
		}
		return this;
	}

	@Searchable(root = false)
	public static class Field implements Serializable {

		@SearchableProperty(boost = 2, index = Index.NOT_ANALYZED)
		private String name;

		@SearchableProperty(boost = 2, index = Index.NOT_ANALYZED)
		private List<String> values = new ArrayList<String>();

		private boolean required;

		private boolean strict;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getValues() {
			return values;
		}

		public void setValues(List<String> values) {
			this.values = values;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}

		public boolean isStrict() {
			return strict;
		}

		public void setStrict(boolean strict) {
			this.strict = strict;
		}

		public int hashCode() {
			return name != null ? name.hashCode() : 0;
		}

		public boolean equals(Object o) {
			if (o instanceof Field) {
				Field that = (Field) o;
				return that.getName() != null
						&& that.getName().equals(this.getName());
			}
			return false;
		}
	}

}
