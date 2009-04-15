package org.ironrhino.core.lb;

import java.util.LinkedHashMap;
import java.util.Map;

public class Test {

	public static void main(String... strings) {
		PolicyFactory<String> pf = new RoundRobinPolicyFactory<String>();
		Map<String, Integer> targets = new LinkedHashMap<String, Integer>();
		targets.put("a", 1);
		targets.put("b", 2);
		targets.put("c", 3);
		Policy<String> p = pf.getPolicy(targets);
		Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		for (int i = 0; i < 100; i++) {
			String s = p.pick();
			if (!result.containsKey(s))
				result.put(s, 1);
			else
				result.put(s, result.get(s) + 1);
		}
		System.out.println(result);
		if (p instanceof AbstractPolicy) {
			// change weight or usableChecker
			AbstractPolicy<String> ap = (AbstractPolicy<String>) p;
			ap.setUsableChecker(null);
			ap.getTargetWrappers().get(0).setTarget("d");
			ap.getTargetWrappers().get(0).setWeight(4);
			for (int i = 0; i < 100; i++) {
				String s = p.pick();
				if (!result.containsKey(s))
					result.put(s, 1);
				else
					result.put(s, result.get(s) + 1);
			}
			System.out.println(result);
		}
	}

}
