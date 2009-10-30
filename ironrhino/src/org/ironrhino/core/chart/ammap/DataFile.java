package org.ironrhino.core.chart.ammap;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class DataFile implements Serializable {

	private static final long serialVersionUID = -3641120919697379119L;

	private String label;

	private Map<String, Area> areas = new HashMap<String, Area>();

	private Map<String, String> movies = new HashMap<String, String>();

	private final static Template template;
	static {
		try {
			template = new Template("DataFile",
					new InputStreamReader(DataFile.class
							.getResourceAsStream("DataFile.ftl"), "UTF-8"),
					null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public DataFile() {
		ChinaArea[] array = new ChinaArea[] { ChinaArea.CN_11, ChinaArea.CN_91,
				ChinaArea.CN_92 };
		for (ChinaArea ca : array)
			movies.put(ca.getName(), ca.getDisplayName());
	}

	public DataFile(String label) {
		this();
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Map<String, Area> getAreas() {
		return areas;
	}

	public Map<String, String> getMovies() {
		return movies;
	}

	public void put(String name, String value, String color) {
		ChinaArea area = ChinaArea.parse(name);
		if (area != null) {
			String mcName = area.name();
			areas.put(mcName, new Area(mcName, name, value, color));
		}
	}

	public void render(Writer writer) throws TemplateException, IOException {
		template.process(this, writer);
	}

	public static class Area implements Serializable {

		private static final long serialVersionUID = 4232279343467521921L;
		private String mcName;
		private String title;
		private String value;
		private String color;

		public Area() {

		}

		public Area(String mcName, String title, String value, String color) {
			this.mcName = mcName;
			this.title = title;
			this.value = value;
			this.color = color;
		}

		public String getMcName() {
			return mcName;
		}

		public void setMcName(String mcName) {
			this.mcName = mcName;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}

	}
}
