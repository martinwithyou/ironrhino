package org.ironrhino.core.lb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RoundRobinPolicyFactory<T> implements PolicyFactory<T> {

	public Policy<T> getPolicy(List<T> targets) {
		return getPolicy(targets, null);
	}

	public Policy<T> getPolicy(List<T> targets, UsableChecker usableChecker) {
		List<TargetWrapper> ts = new ArrayList<TargetWrapper>(targets.size());
		for (T t : targets)
			ts.add(new TargetWrapper(t));
		RoundRobinPolicy rrp = new RoundRobinPolicy();
		rrp.setTargetWrappers(ts);
		rrp.setUsableChecker(usableChecker);

		return rrp;
	}

	public Policy<T> getPolicy(Map<T, Integer> targets) {
		return getPolicy(targets, null);
	}

	public Policy<T> getPolicy(Map<T, Integer> targets,
			UsableChecker usableChecker) {
		List<TargetWrapper<T>> ts = new ArrayList<TargetWrapper<T>>(targets
				.size());
		for (T t : targets.keySet())
			ts.add(new TargetWrapper(t, targets.get(t)));
		RoundRobinPolicy rrp = new RoundRobinPolicy();
		rrp.setTargetWrappers(ts);
		rrp.setUsableChecker(usableChecker);
		return rrp;
	}

}
