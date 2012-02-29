package org.ironrhino.common.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.type.TypeReference;
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
	// @UiConfig(displayOrder = 4, cellEdit = "none", excludeIfNotEdited = true,
	// template = "${entity.getFieldsAsString()!}")
	@UiConfig(displayOrder = 4, hideInList = true)
	private List<SchemaField> fields = new ArrayList<SchemaField>();

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

	public List<SchemaField> getFields() {
		return fields;
	}

	public void setFields(List<SchemaField> fields) {
		this.fields = fields;
	}

	@NotInCopy
	public String getFieldsAsString() {
		if (fields == null)
			return null;
		Set<String> names = new HashSet<String>();
		Iterator<SchemaField> it = fields.iterator();
		while (it.hasNext()) {
			SchemaField f = it.next();
			if (names.contains(f.getName()) || StringUtils.isBlank(f.getName())) {
				it.remove();
				continue;
			}
			if (!f.getType().equals(SchemaFieldType.INPUT)) {
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
						new TypeReference<List<SchemaField>>() {
						});
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public Schema merge(Schema other) {
		if (other != null)
			fields.addAll(other.getFields());
		Set<String> names = new HashSet<String>();
		Iterator<SchemaField> it = fields.iterator();
		while (it.hasNext()) {
			SchemaField f = it.next();
			if (names.contains(f.getName()) || StringUtils.isBlank(f.getName())) {
				it.remove();
				continue;
			}
			names.add(f.getName());
		}
		return this;
	}

}
