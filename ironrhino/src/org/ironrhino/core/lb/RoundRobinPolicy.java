package org.ironrhino.core.lb;

import java.util.concurrent.atomic.AtomicLong;

public class RoundRobinPolicy<T> extends AbstractPolicy<T> {

	public TargetWrapper<T> pickTargetWrapper() {
		int totalWeight = 0;
		TargetWrapper<T> tw = null;
		for (int i = 0; i < targetWrappers.size(); i++) {
			TargetWrapper<T> tempTarget = targetWrappers.get(i);
			if (!isUsable(tempTarget))
				continue;
			AtomicLong tempStat = tempTarget.getStat();
			long oldStat = tempStat.get();
			oldStat += tempTarget.getWeight();
			totalWeight += tempTarget.getWeight();
			tempStat.set(oldStat);
			if (tw == null || oldStat > tw.getStat().get()) {
				tw = tempTarget;
			}
		}
		tw.getCount().incrementAndGet();
		tw.getStat().addAndGet(-totalWeight);
		return tw;
	}

}
