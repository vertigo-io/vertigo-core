package io.vertigo.arch.test.beta;

import io.vertigo.arch.test.delta.D;

public class B {
	public String compute() {
		return String.valueOf(new D().value() + 1);
	}
}
