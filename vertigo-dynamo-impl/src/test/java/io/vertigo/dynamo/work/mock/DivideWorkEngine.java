package io.vertigo.dynamo.work.mock;

import io.vertigo.dynamo.work.WorkEngine;

public final class DivideWorkEngine implements WorkEngine<Long, DivideWork> {
	private static long i = 0;

	/** {@inheritDoc} */
	public Long process(final DivideWork work) {
		if (i++ % 5000 == 0) {
			System.out.print("\nDivideWork : i=" + i);
		}
		return work.getValue1() / work.getValue2();
	}
}
