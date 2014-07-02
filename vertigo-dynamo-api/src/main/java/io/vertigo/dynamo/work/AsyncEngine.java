package io.vertigo.dynamo.work;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.util.concurrent.Callable;

/**
 * Gestion des taches asynchrones définies par un Callable 
 * @author pchretien, npiedeloup
 */
final class AsyncEngine<WR, W> implements WorkEngine<WR, W> {
	private final Callable<WR> callable;

	AsyncEngine(final Callable<WR> callable) {
		Assertion.checkNotNull(callable);
		//-----------------------------------------------------------------
		this.callable = callable;
	}

	public WR process(final W dummy) {
		try {
			return callable.call();
		} catch (final Exception e) {
			throw new VRuntimeException(e);
		}
	}
}
