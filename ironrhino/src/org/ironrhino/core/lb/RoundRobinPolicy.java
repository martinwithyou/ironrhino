package org.ironrhino.core.lb;

import java.util.concurrent.atomic.AtomicLong;

public class RoundRobinPolicy<T> extends AbstractPolicy<T> {

	@Override
	public TargetWrapper<T> pickTargetWrapper() {
		int totalWeight = 0;
		TargetWrapper<T> tw = null;
		for (int i = 0; i < targetWrappers.size(); i++) {
			TargetWrapper<T> tempTarget = targetWrappers.get(i);
			if (!isUsable(tempTarget))
				continue;
			AtomicLong tempStat = tempTarget.getStat();
			long newStat = tempStat.get();
			newStat += tempTarget.getWeight();
			totalWeight += tempTarget.getWeight();
			tempStat.set(newStat);
			if (tw == null || newStat > tw.getStat().get()) 
				tw = tempTarget;
		}
		tw.getCount().incrementAndGet();
		tw.getStat().addAndGet(-totalWeight);
		return tw;
	}

}
