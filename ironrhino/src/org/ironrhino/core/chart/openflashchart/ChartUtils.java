package org.ironrhino.core.chart.openflashchart;

public class ChartUtils {
	public static int caculateSteps(double max) {
		double d = max / 10;
		if (d < 1)
			return 1;
		int i = (int) d;
		if (d > i)
			i++;
		int digit = String.valueOf(i).length();

		int first = i % ((int) Math.pow(10, digit - 1));
		if (first < 5)
			first = 5;
		else
			first = 10;
		return first * ((int) Math.pow(10, digit - 1));
	}

	private static final String[] colors = new String[] { "#ee4400", "#94ee00",
			"#00eee6", "#ee00c7", "#9800ee", "#524141", "#173652", "#36520d",
			"#d1d900", "#00d96d" };

	public static String caculateColor(int seed) {
		if (seed <= colors.length)
			return colors[seed - 1];
		boolean odd = seed % 2 != 0;
		seed = odd ? seed * 2 : 10 - seed;
		StringBuilder sb = new StringBuilder();
		sb.append('#');
		sb.append(seed);
		sb.append(10 - seed);
		sb.append(seed);
		sb.append(10 - seed);
		sb.append(seed);
		sb.append(10 - seed);
		return sb.toString();
	}

}
