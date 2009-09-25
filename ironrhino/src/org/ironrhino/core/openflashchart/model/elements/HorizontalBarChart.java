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

package org.ironrhino.core.openflashchart.model.elements;

import java.util.Arrays;
import java.util.List;

public class HorizontalBarChart extends Element {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3320580794787784639L;
	private String colour;

	public HorizontalBarChart() {
		super("hbar");
	}

	public String getColour() {
		return colour;
	}

	public HorizontalBarChart setColour(String colour) {
		this.colour = colour;
		return this;
	}

	public HorizontalBarChart addBars(Bar... bars) {
		getValues().addAll(Arrays.asList(bars));
		return this;
	}

	public HorizontalBarChart addBars(List<Bar> bars) {
		getValues().addAll(bars);
		return this;
	}

	public HorizontalBarChart addValues(Number... rightValues) {
		Bar[] values = new Bar[rightValues.length];
		for (int i = 0; i < rightValues.length; ++i) {
			values[i] = new Bar(rightValues[i]);
		}
		return addBars(values);
	}

	public HorizontalBarChart addValues(List<Number> rightValues) {
		for (Number number : rightValues) {
			if (number != null) {
				this.addBars(new Bar(number));
			}
		}

		return this;
	}

	public HorizontalBarChart addBar(Number left, Number right) {
		return addBars(new Bar(left, right));
	}

	public static class Bar {
		private final Number right;
		private Number left;

		public Bar(Number right) {
			this(null, right);
		}

		public Bar(Number left, Number right) {
			if (right == null)
				throw new NullPointerException("Field is mandatory.");
			this.right = right;
			setLeft(left);
		}

		public Number getRight() {
			return right;
		}

		public Number getLeft() {
			return left;
		}

		public Bar setLeft(Number left) {
			this.left = left;
			return this;
		}
	}
}
