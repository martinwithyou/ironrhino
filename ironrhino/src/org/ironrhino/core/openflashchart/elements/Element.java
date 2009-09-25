/*
This file is part of JOFC2.

JOFC2 is free software: you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

JOFC2 is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

See <http://www.gnu.org/licenses/lgpl-3.0.txt>.
 */
package org.ironrhino.core.openflashchart.elements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.ironrhino.core.openflashchart.elements.BarChart.Bar;
import org.ironrhino.core.openflashchart.elements.LineChart.Dot;
import org.ironrhino.core.openflashchart.elements.PieChart.Slice;

import com.google.gson.annotations.SerializedName;

public abstract class Element implements Serializable {

	/**
	 * 
	 */

	public static final String ON_CLICK_TOGGLE_VISIBILITY = "toggle-visibility";

	private static final long serialVersionUID = 3975314200083173622L;
	private final String type;
	private Float alpha;
	private String text;
	@SerializedName("font-size")
	private Integer fontSize;
	@SerializedName("tip")
	private String tooltip;
	@SerializedName("gradient-fill")
	private Boolean gradientFill;
	@SerializedName("key-on-click")
	private String key_on_click;
	private List<Object> values = new ArrayList<Object>();

	protected Element(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public Float getAlpha() {
		return alpha;
	}

	public Element setAlpha(Float alpha) {
		this.alpha = alpha;
		return this;
	}

	public String getText() {
		return text;
	}

	/**
	 * The Text is used to represent the Element in the legend. If text is null
	 * the element will not appear in the legend
	 */
	public Element setText(String text) {
		this.text = text;
		return this;
	}

	public Integer getFontSize() {
		return fontSize;
	}

	public Element setFontSize(Integer fontSize) {
		this.fontSize = fontSize;
		return this;
	}

	public List<Object> getValues() {
		return values;
	}

	public Element setTooltip(String tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	public String getTooltip() {
		return tooltip;
	}

	public Boolean getGradientFill() {
		return gradientFill;
	}

	public void setGradientFill(Boolean gradientFill) {
		this.gradientFill = gradientFill;
	}

	/**
	 * Returns the maximum value (double) of the given Element Supports only the
	 * Elements Dot, Bar, Slice and Horizontal Bar
	 */
	public double getMaxValue() {
		double max = 0.0;
		for (Object obj : getValues()) {
			if (obj != null) {
				if (obj instanceof Number) {
					max = Math.max(max, ((Number) obj).doubleValue());
				} else if (obj instanceof Dot) {
					max = Math.max(max,
							((Dot) obj).getValue() != null ? ((Dot) obj)
									.getValue().doubleValue() : 0);
				} else if (obj instanceof Bar) {
					max = Math.max(max,
							((Bar) obj).getTop() != null ? ((Bar) obj).getTop()
									.doubleValue() : 0);
					max = Math.max(max,
							((Bar) obj).getBottom() != null ? ((Bar) obj)
									.getBottom().doubleValue() : 0);
				} else if (obj instanceof Slice) {
					max = Math.max(max,
							((Slice) obj).getValue() != null ? ((Slice) obj)
									.getValue().doubleValue() : 0);
				} else if (obj instanceof org.ironrhino.core.openflashchart.elements.HorizontalBarChart.Bar) {
					max = Math
							.max(
									max,
									((org.ironrhino.core.openflashchart.elements.HorizontalBarChart.Bar) obj)
											.getLeft() != null ? ((org.ironrhino.core.openflashchart.elements.HorizontalBarChart.Bar) obj)
											.getLeft().doubleValue()
											: 0);
					max = Math
							.max(
									max,
									((org.ironrhino.core.openflashchart.elements.HorizontalBarChart.Bar) obj)
											.getRight() != null ? ((org.ironrhino.core.openflashchart.elements.HorizontalBarChart.Bar) obj)
											.getRight().doubleValue()
											: 0);
				} else if (obj instanceof NullElement) {
					/* No action */
				} else {
					throw new IllegalArgumentException(
							"Cannot process Objects of Class: "
									+ String.valueOf(obj.getClass()));
				}
			}
		}
		return max;
	}

	/**
	 * Returns the minimum value (double) of the given Element Supports only the
	 * Elements Dot, Bar, Slice and Horizontal Bar
	 */
	public double getMinValue() {
		Double min = null;
		for (Object obj : getValues()) {
			if (obj != null) {
				if (obj instanceof Number) {
					min = nullSafeMin(min, ((Number) obj).doubleValue());
				} else if (obj instanceof Dot) {
					min = nullSafeMin(min,
							((Dot) obj).getValue() != null ? ((Dot) obj)
									.getValue().doubleValue() : 0);
				} else if (obj instanceof Bar) {
					min = nullSafeMin(min,
							((Bar) obj).getTop() != null ? ((Bar) obj).getTop()
									.doubleValue() : 0);
					min = nullSafeMin(min,
							((Bar) obj).getBottom() != null ? ((Bar) obj)
									.getBottom().doubleValue() : 0);
				} else if (obj instanceof Slice) {
					min = nullSafeMin(min,
							((Slice) obj).getValue() != null ? ((Slice) obj)
									.getValue().doubleValue() : 0);
				} else if (obj instanceof org.ironrhino.core.openflashchart.elements.HorizontalBarChart.Bar) {
					min = nullSafeMin(
							min,
							((org.ironrhino.core.openflashchart.elements.HorizontalBarChart.Bar) obj)
									.getLeft() != null ? ((org.ironrhino.core.openflashchart.elements.HorizontalBarChart.Bar) obj)
									.getLeft().doubleValue()
									: 0);
					min = nullSafeMin(
							min,
							((org.ironrhino.core.openflashchart.elements.HorizontalBarChart.Bar) obj)
									.getRight() != null ? ((org.ironrhino.core.openflashchart.elements.HorizontalBarChart.Bar) obj)
									.getRight().doubleValue()
									: 0);
				} else if (obj instanceof NullElement) {
					/* No action */
				} else {
					throw new IllegalArgumentException(
							"Cannot process Objects of Class: "
									+ String.valueOf(obj.getClass()));
				}
			}
		}
		if (null == min) {
			min = 0.0;
		}
		return min;
	}

	private Double nullSafeMin(Double min, double doubleValue) {
		if (null == min) {
			min = doubleValue;
		}
		min = Math.min(min, doubleValue);
		return min;
	}

	public String getKey_on_click() {
		return key_on_click;
	}

	/**
	 * Set the Key on Click Funktion. e.g. "toggle-visibility". For a Link just
	 * pass the URL toggle-visibility will enable you to click on your legend an
	 * switch on and off individual elements
	 */
	public void setKey_on_click(String key_on_click) {
		this.key_on_click = key_on_click;
	}

	/**
	 * Shortcut for setKey_on_click(ON_CLICK_TOGGLE_VISIBILITY)
	 */
	public void setToggleVisibility() {
		this.key_on_click = ON_CLICK_TOGGLE_VISIBILITY;
	}
}
