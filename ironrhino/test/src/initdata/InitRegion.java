package initdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ironrhino.common.model.Region;
import org.ironrhino.common.util.RegionParser;
import org.ironrhino.core.service.BaseManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class InitRegion {

	static BaseManager baseManager;

	public static void main(String... strings) throws Exception {
		System.setProperty("app.name", "ironrhino");
		System.setProperty("ironrhino.home", System.getProperty("user.home")
				+ "/" + System.getProperty("app.name"));
		System.setProperty("ironrhino.context", System.getProperty("user.home")
				+ "/" + System.getProperty("app.name"));
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] {
						"initdata/applicationContext-initdata.xml",
						"resources/spring/applicationContext-ds.xml",
						"resources/spring/applicationContext-hibernate.xml" });
		baseManager = (BaseManager) ctx.getBean("baseManager");
		List<Region> regions = RegionParser.parse();
		for (Region region : regions) {
			save(region);
		}
	}

	private static void save(Region region) {
		baseManager.save(region);
		List<Region> list = new ArrayList<Region>();
		for (Region child : region.getChildren())
			list.add(child);
		Collections.sort(list);
		for (Region child : list)
			save(child);
	}
}
