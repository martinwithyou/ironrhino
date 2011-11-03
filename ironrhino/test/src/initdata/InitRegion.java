package initdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.ironrhino.common.model.Region;
import org.ironrhino.common.util.RegionParser;
import org.ironrhino.core.service.BaseManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InitRegion {

	static BaseManager baseManager;

	static Map<String, String> regionAreacodeMap = regionAreacodeMap();

	public static void main(String... strings) throws Exception {
		System.setProperty("app.name", "ironrhino");
		System.setProperty("ironrhino.home", System.getProperty("user.home")
				+ "/" + System.getProperty("app.name"));
		System.setProperty("ironrhino.context", System.getProperty("user.home")
				+ "/" + System.getProperty("app.name"));
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] { "initdata/applicationContext-initdata.xml",
						"resources/spring/applicationContext-ds.xml",
						"resources/spring/applicationContext-hibernate.xml" });
		baseManager = (BaseManager) ctx.getBean("baseManager");
		List<Region> regions = RegionParser.parse();
		for (Region region : regions) {
			save(region);
		}
		ctx.close();
	}

	private static void save(Region region) {
		region.setAreacode(regionAreacodeMap.get(region.getName()));
		baseManager.save(region);
		List<Region> list = new ArrayList<Region>();
		for (Region child : region.getChildren())
			list.add(child);
		Collections.sort(list);
		for (Region child : list)
			save(child);
	}

	private static Map<String, String> regionAreacodeMap() {
		List<String> lines = null;
		try {
			lines = IOUtils.readLines(
					Thread.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									"resources/data/region_code.txt"), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map<String, String> map = new HashMap<String, String>(lines.size());
		for (String line : lines) {
			if (StringUtils.isBlank(line))
				continue;
			String arr[] = line.split("\\s+", 2);
			map.put(arr[1], arr[0]);
		}
		return map;
	}
}
