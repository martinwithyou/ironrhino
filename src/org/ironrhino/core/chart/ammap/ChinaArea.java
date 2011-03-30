package org.ironrhino.core.chart.ammap;

import java.util.Locale;
import java.util.ResourceBundle;

public enum ChinaArea {

	CN_11, CN_12, CN_13, CN_14, CN_15, CN_21, CN_22, CN_23, CN_31, CN_32, CN_33, CN_34, CN_35, CN_36, CN_37, CN_41, CN_42, CN_43, CN_44, CN_45, CN_46, CN_50, CN_51, CN_52, CN_53, CN_54, CN_61, CN_62, CN_63, CN_64, CN_65, CN_91, CN_92, TW;

	static ResourceBundle resources = ResourceBundle.getBundle(ChinaArea.class
			.getName(), Locale.CHINA);

	public String getName() {
		return name();
	}

	public String getDisplayName() {
		return resources.getString(getName());
	}

	public static ChinaArea parse(String name) {
		if (name != null)
			for (ChinaArea area : values())
				if (name.equals(area.name())
						|| name.equals(area.getDisplayName()))
					return area;
		return null;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

}
