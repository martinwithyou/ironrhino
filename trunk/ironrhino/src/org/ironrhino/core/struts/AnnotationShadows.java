package org.ironrhino.core.struts;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.metadata.Hidden;
import org.ironrhino.core.metadata.Readonly;
import org.ironrhino.core.metadata.Richtable;
import org.ironrhino.core.metadata.UiConfig;
import org.ironrhino.core.util.JsonUtils;

import com.fasterxml.jackson.core.type.TypeReference;

public class AnnotationShadows {

	public static class UiConfigImpl implements Serializable {

		private static final long serialVersionUID = -5963246979386241924L;
		private Class<?> propertyType;
		private String id;
		private String type = UiConfig.DEFAULT_TYPE;
		private String inputType = UiConfig.DEFAULT_INPUT_TYPE;
		private boolean required;
		private boolean unique;
		private int maxlength;
		private String regex;
		private Set<String> cssClasses = new LinkedHashSet<>(0);
		private ReadonlyImpl readonly;
		private int displayOrder = Integer.MAX_VALUE;
		private String alias;
		private HiddenImpl hiddenInList;
		private HiddenImpl hiddenInInput;
		private HiddenImpl hiddenInView;
		private String template;
		private String listTemplate;
		private String viewTemplate;
		private String width;
		private Map<String, String> dynamicAttributes = new HashMap<String, String>(
				0);
		private String cellDynamicAttributes = "";
		private boolean excludeIfNotEdited;
		private String listKey = UiConfig.DEFAULT_LIST_KEY;
		private String listValue = UiConfig.DEFAULT_LIST_VALUE;
		private String cellEdit = "";
		private boolean searchable;
		private String pickUrl = "";
		private String templateName = "";
		private boolean excludedFromLike = false;
		private boolean excludedFromCriteria = false;

		public UiConfigImpl() {
		}

		public UiConfigImpl(Class<?> propertyType, UiConfig config) {
			this.propertyType = propertyType;
			if (config == null)
				return;
			if (StringUtils.isNotBlank(config.id()))
				this.id = config.id();
			this.type = config.type();
			this.inputType = config.inputType();
			this.listKey = config.listKey();
			this.listValue = config.listValue();
			this.required = config.required();
			this.unique = config.unique();
			this.maxlength = config.maxlength();
			this.regex = config.regex();
			this.readonly = new ReadonlyImpl(config.readonly());
			this.displayOrder = config.displayOrder();
			if (StringUtils.isNotBlank(config.alias()))
				this.alias = config.alias();
			this.hiddenInList = new HiddenImpl(config.hiddenInList());
			this.hiddenInInput = new HiddenImpl(config.hiddenInInput());
			this.hiddenInView = new HiddenImpl(config.hiddenInView());
			this.template = config.template();
			this.listTemplate = config.listTemplate();
			this.viewTemplate = config.viewTemplate();
			this.width = config.width();
			if (StringUtils.isNotBlank(config.dynamicAttributes()))
				try {
					this.dynamicAttributes = JsonUtils.fromJson(
							config.dynamicAttributes(),
							new TypeReference<Map<String, String>>() {
							});
				} catch (Exception e) {
					e.printStackTrace();
				}
			this.cellDynamicAttributes = config.cellDynamicAttributes();
			this.cellEdit = config.cellEdit();
			this.excludeIfNotEdited = config.excludeIfNotEdited();
			if (StringUtils.isNotBlank(config.cssClass()))
				this.cssClasses.addAll(Arrays.asList(config.cssClass().split(
						"\\s")));
			this.searchable = config.searchable();
			this.pickUrl = config.pickUrl();
			this.templateName = config.templateName();
			if (StringUtils.isNotBlank(this.regex)) {
				cssClasses.add("regex");
				dynamicAttributes.put("data-regex", this.regex);
			}
			this.excludedFromLike = config.excludedFromLike();
			this.excludedFromCriteria = config.excludedFromCriteria();
		}

		public boolean isExcludedFromLike() {
			return excludedFromLike;
		}

		public void setExcludedFromLike(boolean excludedFromLike) {
			this.excludedFromLike = excludedFromLike;
		}

		public boolean isExcludedFromCriteria() {
			return excludedFromCriteria;
		}

		public void setExcludedFromCriteria(boolean excludedFromCriteria) {
			this.excludedFromCriteria = excludedFromCriteria;
		}

		public Class<?> getPropertyType() {
			return propertyType;
		}

		public void setPropertyType(Class<?> propertyType) {
			this.propertyType = propertyType;
		}

		public HiddenImpl getHiddenInList() {
			return hiddenInList;
		}

		public void setHiddenInList(HiddenImpl hiddenInList) {
			this.hiddenInList = hiddenInList;
		}

		public HiddenImpl getHiddenInInput() {
			return hiddenInInput;
		}

		public void setHiddenInInput(HiddenImpl hiddenInInput) {
			this.hiddenInInput = hiddenInInput;
		}

		public HiddenImpl getHiddenInView() {
			return hiddenInView;
		}

		public void setHiddenInView(HiddenImpl hiddenInView) {
			this.hiddenInView = hiddenInView;
		}

		public String getPickUrl() {
			return pickUrl;
		}

		public void setPickUrl(String pickUrl) {
			this.pickUrl = pickUrl;
		}

		public String getTemplateName() {
			return templateName;
		}

		public void setTemplateName(String templateName) {
			this.templateName = templateName;
		}

		public Map<String, String> getDynamicAttributes() {
			return dynamicAttributes;
		}

		public void setDynamicAttributes(Map<String, String> dynamicAttributes) {
			this.dynamicAttributes = dynamicAttributes;
		}

		public String getCellDynamicAttributes() {
			return cellDynamicAttributes;
		}

		public void setCellDynamicAttributes(String cellDynamicAttributes) {
			this.cellDynamicAttributes = cellDynamicAttributes;
		}

		public boolean isRequired() {
			return required;
		}

		public void setRequired(boolean required) {
			this.required = required;
		}

		public boolean isUnique() {
			return unique;
		}

		public void setUnique(boolean unique) {
			this.unique = unique;
		}

		public String getAlias() {
			return alias;
		}

		public void setAlias(String alias) {
			this.alias = alias;
		}

		public int getDisplayOrder() {
			return displayOrder;
		}

		public void setDisplayOrder(int displayOrder) {
			this.displayOrder = displayOrder;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getInputType() {
			return inputType;
		}

		public void setInputType(String inputType) {
			this.inputType = inputType;
		}

		public int getMaxlength() {
			return maxlength;
		}

		public void setMaxlength(int maxlength) {
			this.maxlength = maxlength;
		}

		public String getRegex() {
			return regex;
		}

		public void setRegex(String regex) {
			this.regex = regex;
		}

		public String getListKey() {
			return listKey;
		}

		public void setListKey(String listKey) {
			this.listKey = listKey;
		}

		public String getListValue() {
			return listValue;
		}

		public void setListValue(String listValue) {
			this.listValue = listValue;
		}

		public String getCssClass() {
			if (required)
				addCssClass("required");
			if (unique)
				addCssClass("checkavailable");
			if (excludeIfNotEdited)
				addCssClass("excludeIfNotEdited");
			return StringUtils.join(cssClasses, " ");
		}

		public void addCssClass(String cssClass) {
			this.cssClasses.add(cssClass);
		}

		public Set<String> getCssClasses() {
			return cssClasses;
		}

		public ReadonlyImpl getReadonly() {
			return readonly;
		}

		public void setReadonly(ReadonlyImpl readonly) {
			this.readonly = readonly;
		}

		public String getTemplate() {
			return template;
		}

		public void setTemplate(String template) {
			this.template = template;
		}

		public String getListTemplate() {
			return listTemplate;
		}

		public void setListTemplate(String listTemplate) {
			this.listTemplate = listTemplate;
		}

		public String getViewTemplate() {
			return viewTemplate;
		}

		public void setViewTemplate(String viewTemplate) {
			this.viewTemplate = viewTemplate;
		}

		public String getWidth() {
			return width;
		}

		public void setWidth(String width) {
			this.width = width;
		}

		public boolean isExcludeIfNotEdited() {
			return excludeIfNotEdited;
		}

		public void setExcludeIfNotEdited(boolean excludeIfNotEdited) {
			this.excludeIfNotEdited = excludeIfNotEdited;
		}

		public String getCellEdit() {
			return cellEdit;
		}

		public void setCellEdit(String cellEdit) {
			this.cellEdit = cellEdit;
		}

		public boolean isSearchable() {
			return searchable;
		}

		public void setSearchable(boolean searchable) {
			this.searchable = searchable;
		}

	}

	public static class ReadonlyImpl implements Serializable {

		private static final long serialVersionUID = 6566440254646584026L;
		private boolean value = false;
		private String expression = "";
		private boolean deletable = false;

		public ReadonlyImpl() {
		}

		public ReadonlyImpl(Readonly config) {
			if (config == null)
				return;
			this.value = config.value();
			this.expression = config.expression();
			this.deletable = config.deletable();
		}

		public boolean isValue() {
			return value;
		}

		public void setValue(boolean value) {
			this.value = value;
		}

		public String getExpression() {
			return expression;
		}

		public void setExpression(String expression) {
			this.expression = expression;
		}

		public boolean isDeletable() {
			return deletable;
		}

		public void setDeletable(boolean deletable) {
			this.deletable = deletable;
		}

	}

	public static class HiddenImpl implements Serializable {

		private static final long serialVersionUID = 6566440254646584026L;
		private boolean value = false;
		private String expression = "";

		public HiddenImpl() {
		}

		public HiddenImpl(Hidden config) {
			if (config == null)
				return;
			this.value = config.value();
			this.expression = config.expression();
			if (!this.value && StringUtils.isBlank(this.expression)
					&& config.hideWhenBlank())
				this.expression = "!(value?? && value?string?has_content)";
		}

		public boolean isValue() {
			return value;
		}

		public void setValue(boolean value) {
			this.value = value;
		}

		public String getExpression() {
			return expression;
		}

		public void setExpression(String expression) {
			this.expression = expression;
		}

	}

	public static class RichtableImpl implements Serializable {

		private static final long serialVersionUID = 7346213812241502993L;
		private String formid = "";
		private boolean filterable = true;
		private boolean showPageSize = true;
		private boolean searchable;
		private String actionColumnButtons = "";
		private String bottomButtons = "";
		private String listHeader = "";
		private String listFooter = "";
		private String formHeader = "";
		private String formFooter = "";
		private String rowDynamicAttributes = "";

		public RichtableImpl() {
		}

		public RichtableImpl(Richtable config) {
			if (config == null)
				return;
			this.formid = config.formid();
			this.filterable = config.filterable();
			this.showPageSize = config.showPageSize();
			this.searchable = config.searchable();
			this.actionColumnButtons = config.actionColumnButtons();
			this.bottomButtons = config.bottomButtons();
			this.listHeader = config.listHeader();
			this.listFooter = config.listFooter();
			this.formHeader = config.formHeader();
			this.formFooter = config.formFooter();
			this.rowDynamicAttributes = config.rowDynamicAttributes();
		}

		public String getFormid() {
			return formid;
		}

		public void setFormid(String formid) {
			this.formid = formid;
		}

		public boolean isFilterable() {
			return filterable;
		}

		public void setFilterable(boolean filterable) {
			this.filterable = filterable;
		}

		public boolean isShowPageSize() {
			return showPageSize;
		}

		public void setShowPageSize(boolean showPageSize) {
			this.showPageSize = showPageSize;
		}

		public boolean isSearchable() {
			return searchable;
		}

		public void setSearchable(boolean searchable) {
			this.searchable = searchable;
		}

		public String getActionColumnButtons() {
			return actionColumnButtons;
		}

		public void setActionColumnButtons(String actionColumnButtons) {
			this.actionColumnButtons = actionColumnButtons;
		}

		public String getBottomButtons() {
			return bottomButtons;
		}

		public void setBottomButtons(String bottomButtons) {
			this.bottomButtons = bottomButtons;
		}

		public String getListHeader() {
			return listHeader;
		}

		public void setListHeader(String listHeader) {
			this.listHeader = listHeader;
		}

		public String getListFooter() {
			return listFooter;
		}

		public void setListFooter(String listFooter) {
			this.listFooter = listFooter;
		}

		public String getFormHeader() {
			return formHeader;
		}

		public void setFormHeader(String formHeader) {
			this.formHeader = formHeader;
		}

		public String getFormFooter() {
			return formFooter;
		}

		public void setFormFooter(String formFooter) {
			this.formFooter = formFooter;
		}

		public String getRowDynamicAttributes() {
			return rowDynamicAttributes;
		}

		public void setRowDynamicAttributes(String rowDynamicAttributes) {
			this.rowDynamicAttributes = rowDynamicAttributes;
		}

	}

}
