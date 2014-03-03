package io.vertigo.dynamo.work.mock;

import java.io.Serializable;

public final class DivideWork implements Serializable {
	private static final long serialVersionUID = -3662831488252806536L;
	private final long value1, value2;

	public DivideWork(final long value1, final long value2) {
		//---------------------------------------------------------------------
		this.value1 = value1;
		this.value2 = value2;
	}

	long getValue1() {
		return value1;
	}

	long getValue2() {
		return value2;
	}
}
