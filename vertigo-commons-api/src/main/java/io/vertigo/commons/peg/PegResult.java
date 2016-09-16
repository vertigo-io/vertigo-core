package io.vertigo.commons.peg;

import io.vertigo.lang.Assertion;

/**
 * 	Contains the result of a parsing operation.
 *   - the new index position
 *   - the object created
 * @author pchretien
 *
 * @param <R> the type of the Result Object
 */
public final class PegResult<R> {
	private final int index;
	private final R result;

	/**
	 * Constructor.
	 * @param index
	 * @param result
	 */
	PegResult(final int index, final R result) {
		Assertion.checkNotNull(result);
		//---
		this.index = index;
		this.result = result;
	}

	/**
	 * @return Index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return result
	 */
	public R getResult() {
		return result;
	}

}
