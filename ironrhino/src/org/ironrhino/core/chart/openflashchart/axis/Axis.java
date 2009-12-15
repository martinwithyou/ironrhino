package org.ironrhino.core.chart.openflashchart.axis;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;

public abstract class Axis implements Serializable {

	private static final long serialVersionUID = 4823643361437691998L;
	private Integer stroke;
	private String colour;
	@JsonProperty("grid-colour")
	private String gridColour;
	private Double steps;
	private Integer offset;
	private Integer _3d;
	private Double min;
	private Double max;

	public Integer getStroke() {
		return stroke;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public void setStroke(Integer stroke) {
		this.stroke = stroke;
	}

	public String getColour() {
		return colour;
	}

	public void setColour(String colour) {
		this.colour = colour;

	}

	public String getGridColour() {
		return gridColour;
	}

	public void setGridColour(String gridColour) {
		this.gridColour = gridColour;

	}

	public Double getSteps() {
		return steps;
	}

	public void setSteps(Double steps) {
		this.steps = steps;

	}

	public void setSteps(Integer steps) {
		this.steps = steps.doubleValue();

	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Boolean offset) {
		if (offset == null)
			this.offset = 0;
		else
			this.offset = offset ? 1 : 0;

	}

	public Integer get3D() {
		return _3d;
	}

	public void set3D(Integer _3d) {
		this._3d = _3d;

	}

	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		this.min = min;

	}

	public void setMin(Integer min) {
		this.min = min.doubleValue();

	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		this.max = max;

	}

	public void setMax(Integer max) {
		this.max = max.doubleValue();

	}

	public void setRange(Number min, Number max, Number step) {
		setMin(min.doubleValue());
		setMax(max.doubleValue());
		setSteps(step.doubleValue());

	}

	public void setRange(Number min, Number max) {
		setRange(min, max, getSteps());
	}

}
