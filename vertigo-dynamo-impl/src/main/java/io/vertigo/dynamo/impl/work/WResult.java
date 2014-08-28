package io.vertigo.dynamo.impl.work;

import io.vertigo.kernel.lang.Assertion;

/**
 * @author pchretien
 */
public final class WResult<WR> {
	private final String workId;
	private final Throwable error;
	private final WR result;

	public WResult(final String workId, final WR result, final Throwable error) {
		Assertion.checkArgNotEmpty(workId);
		Assertion.checkArgument(result == null ^ error == null, "result xor error is null");
		//---------------------------------------------------------------------
		this.workId = workId;
		this.error = error;
		this.result = result;
	}

	public String getWorkId() {
		return workId;
	}

	public boolean hasSucceeded() {
		return error == null;
	}

	public WR getResult() {
		return result;
	}

	public Throwable getError() {
		return error;
	}

}
