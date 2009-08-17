package org.ironrhino.core.lb;

public interface UsableChecker<T> {

	boolean isUsable(TargetWrapper<T> targetWrapper);

}
