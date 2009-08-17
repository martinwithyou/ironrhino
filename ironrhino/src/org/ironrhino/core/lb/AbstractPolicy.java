package org.ironrhino.core.lb;

import java.util.List;

public abstract class AbstractPolicy<T> implements Policy<T> {

	protected List<TargetWrapper<T>> targetWrappers;

	protected UsableChecker usableChecker;

	public void setTargetWrappers(List<TargetWrapper<T>> targets) {
		this.targetWrappers = targets;
	}

	public List<TargetWrapper<T>> getTargetWrappers() {
		return targetWrappers;
	}

	public UsableChecker getUsableChecker() {
		return usableChecker;
	}

	public void setUsableChecker(UsableChecker usableChecker) {
		this.usableChecker = usableChecker;
	}

	protected boolean isUsable(TargetWrapper<T> t) {
		return usableChecker == null || usableChecker.isUsable(t);
	}

	public T pick() {
		TargetWrapper tw = pickTargetWrapper();
		if (tw == null)
			return null;
		return pickTargetWrapper().getTarget();
	}

	public abstract TargetWrapper<T> pickTargetWrapper();

}
