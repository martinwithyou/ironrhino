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
package org.ironrhino.core.openflashchart.axis;

import java.util.List;

public class YAxis extends Axis {

	private static final long serialVersionUID = 7471159737831995334L;
	private Integer tick_length;
	private YAxisLabels labels;

	public YAxis setTickLength(Integer tick_length) {
		this.tick_length = tick_length;
		return this;
	}

	public Integer getTickLength() {
		return tick_length;
	}

	public YAxis setLabels(String... labels) {
		this.labels = new YAxisLabels(labels);
		return this;
	}

	public YAxis setLabels(List<String> labels) {
		this.labels = new YAxisLabels(labels);
		return this;
	}

	public YAxis addLabels(String... labels) {
		if (this.labels == null) {
			this.labels = new YAxisLabels();
		}
		this.labels.addLabels(labels);
		return this;
	}

	public YAxis addLabels(List<Label> labels) {
		if (this.labels == null) {
			this.labels = new YAxisLabels();
		}
		this.labels.addLabels(labels);
		return this;
	}

	public YAxisLabels getLabels() {
		if (this.labels == null) {
			this.labels = new YAxisLabels();
		}
		return this.labels;
	}
}
