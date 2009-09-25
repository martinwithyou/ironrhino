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
package org.ironrhino.core.openflashchart.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.ironrhino.core.openflashchart.model.axis.XAxis;
import org.ironrhino.core.openflashchart.model.axis.YAxis;
import org.ironrhino.core.openflashchart.model.elements.Element;
import org.ironrhino.core.openflashchart.model.elements.Legend;
import org.ironrhino.core.openflashchart.model.elements.Tooltip;

/**
 * This is the most important class in the Java OFC library. Start here,
 * configuring the title, axes, legends, labels, and draw-able elements in your
 * chart. Coerce the object to a String with the toString() method to get the
 * chart data back out.
 */
public class Chart implements Serializable {

	private static final long serialVersionUID = -1868082240169089976L;
	private Text title;
	private XAxis x_axis;
	private YAxis y_axis;
	private YAxis y_axis_right;
	private Text y_legend;
	private Text x_legend;
	private String bg_colour;

	private int num_decimals = 2;
	private Collection<Element> elements = new ArrayList<Element>();
	private Legend legend;
	private Tooltip tooltip;

	public XAxis getXAxis() {
		return x_axis;
	}

	public Chart() {
		// nothing...
	}

	public Chart(String titleText) {
		this(titleText, null);
	}

	public Chart(String titleText, String style) {
		this.setTitle(new Text(titleText, style));
	}

	public Tooltip getTooltip() {
		return tooltip;
	}

	public void setTooltip(Tooltip tooltip) {
		this.tooltip = tooltip;
	}

	public Chart setXAxis(XAxis x_axis) {
		this.x_axis = x_axis;
		return this;
	}

	public YAxis getYAxis() {
		return y_axis;
	}

	public Chart setYAxis(YAxis y_axis) {
		this.y_axis = y_axis;
		return this;
	}

	public Chart setYAxisRight(YAxis y_axis_right) {
		this.y_axis_right = y_axis_right;
		return this;
	}

	public YAxis getYAxisRight() {
		return y_axis_right;
	}

	public Text getTitle() {
		return title;
	}

	public Chart setTitle(Text title) {
		this.title = title;
		return this;
	}

	public Text getXLegend() {
		return x_legend;
	}

	public Chart setXLegend(Text x_legend) {
		this.x_legend = x_legend;
		return this;
	}

	public Text getYLegend() {
		return y_legend;
	}

	public Chart setYLegend(Text y_legend) {
		this.y_legend = y_legend;
		return this;
	}

	public String getBackgroundColour() {
		return bg_colour;
	}

	public Chart setBackgroundColour(String bg_colour) {
		this.bg_colour = bg_colour;
		return this;
	}

	public Collection<Element> getElements() {
		return elements;
	}

	public Chart setElements(Collection<Element> elements) {
		this.elements.clear();
		this.elements.addAll(elements);
		return this;
	}

	public Chart addElements(Element... e) {
		elements.addAll(Arrays.asList(e));
		return this;
	}

	public Chart addElements(Collection<Element> coll) {
		elements.addAll(coll);
		return this;
	}

	public boolean removeElement(Element e) {
		return elements.remove(e);
	}

	public Element getElementByText(String text) {
		for (Element e : getElements()) {
			if (text.equals(e.getText()))
				return e;
		}
		return null;
	}

	@Override
	public String toString() {
		// TODO rendre to json
		return "{}";
		// return OFC.getInstance().render(this);
	}

	/**
	 * @return the max number of decimals printed out in OFC. <br />
	 */
	public int getNumDecimals() {
		return num_decimals;
	}

	/**
	 * @param num_decimals
	 *            the max number of decimals printed out in OFC.<br />
	 *            Allowed values 0 - 16. <br/>
	 */
	public void setNumDecimals(int num_decimals) {
		this.num_decimals = num_decimals;
	}

	public void computeYAxisRange(int steps) {
		Double min = null;
		double max = 0;
		double stepWidth = 1;
		if (getElements() != null) {
			if (getYAxis() == null) {
				YAxis ya = new YAxis();
				this.setYAxis(ya);
			}
			for (Element e : getElements()) {
				max = Math.max(max, e.getMaxValue());
				min = nullSafeMin(min, e.getMinValue());
			}
			if (min == null) {
				min = 0.0;
			}
			stepWidth = getStepWidth(Math.abs(max - min), steps);
			min = Math.floor(min / stepWidth) * stepWidth;
			max = Math.ceil(max / stepWidth) * stepWidth;
			getYAxis().setRange(min, max, stepWidth);

		}
	}

	private Double nullSafeMin(Double min, double doubleValue) {
		if (null == min) {
			min = doubleValue;
		}
		min = Math.min(min, doubleValue);
		return min;
	}

	private double getStepWidth(double distance, int steps) {
		double result = distance / steps;
		double exponent = Math.floor(Math.log10(result)) + 1;
		result = result / Math.pow(10, exponent);
		if (result > 0.5) {
			result = 1;
		} else if (result > 0.25) {
			result = 0.5;
		} else {
			result = 0.25;
		}
		return result * Math.pow(10, exponent);
	}

	public Legend getLegend() {
		return legend;
	}

	public void setLegend(Legend legend) {
		this.legend = legend;
	}
}
