package org.ironrhino.common.model.tuples;

import java.io.Serializable;

public class Pair<A, B> implements Serializable {

	private A a;

	private B b;

	public Pair() {

	}

	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public A getA() {
		return a;
	}

	public void setA(A a) {
		this.a = a;
	}

	public B getB() {
		return b;
	}

	public void setB(B b) {
		this.b = b;
	}

}