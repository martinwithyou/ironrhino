package org.ironrhino.core.lb;

import java.util.List;
import java.util.Map;

public interface PolicyFactory<T> {

	public Policy<T> getPolicy(List<T> targets);

	public Policy<T> getPolicy(List<T> targets, UsableChecker<T> usableChecker);

	public Policy<T> getPolicy(Map<T, Integer> targets);

	public Policy<T> getPolicy(Map<T, Integer> targets,
			UsableChecker<T> usableChecker);

}
