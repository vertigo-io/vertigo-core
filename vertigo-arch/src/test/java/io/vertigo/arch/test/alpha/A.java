package io.vertigo.arch.test.alpha;

import io.vertigo.arch.test.beta.B;
import io.vertigo.arch.test.gamma.G;

public class A {
	public String hello() {
		return new B().compute() + new G().greet();
	}
}
