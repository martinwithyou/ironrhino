package org.ironrhino.core.chart.ammap;

import java.util.Locale;
import java.util.ResourceBundle;

public enum ChinaArea {

	CN_BJ, CN_TJ, CN_HB, CN_SX, CN_NM, CN_LN, CN_JL, CN_HL, CN_SH, CN_JS, CN_ZJ, CN_AH, CN_FJ, CN_JX, CN_SD, CN_HE, CN_HU, CN_HN, CN_GD, CN_GX, CN_HA, CN_CQ, CN_SC, CN_GZ, CN_YN, CN_XZ, CN_SA, CN_GS, CN_QH, CN_NX, CN_XJ, HK, MO, TW;

	static ResourceBundle resources = ResourceBundle.getBundle(
			ChinaArea.class.getName(), Locale.CHINA);

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
