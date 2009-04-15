package org.ironrhino.core.lb;

import java.util.concurrent.atomic.AtomicLong;

public class TargetWrapper<T> {

	public static final int DEFAULT_WEIGHT = 1;

	private static final long serialVersionUID = -5014563629634040181L;

	private T target;

	private int weight = DEFAULT_WEIGHT;

	private AtomicLong count = new AtomicLong(0);

	private AtomicLong stat = new AtomicLong(0);

	public TargetWrapper(T target) {
		this.target = target;
	}

	public TargetWrapper(T target, int weight) {
		this.target = target;
		if (weight > 0)
			this.weight = weight;
	}

	public T getTarget() {
		return target;
	}

	public void setTarget(T target) {
		this.target = target;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public AtomicLong getCount() {
		return count;
	}

	public AtomicLong getStat() {
		return stat;
	}
}
