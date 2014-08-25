package io.vertigo.dynamo.plugins.work.redis;

import io.vertigo.kernel.lang.Assertion;

/**
 * @author pchretien
 * $Id: RedisDispatcherThread.java,v 1.8 2014/02/03 17:28:45 pchretien Exp $
 */
public final class RedisResult<WR> {
	private final String workId;
	private final Throwable error;
	private final WR result;

	RedisResult(final String workId, final WR result, final Throwable error) {
		Assertion.checkArgNotEmpty(workId);
		Assertion.checkArgument(error != null ^ result != null, "error xor result is required not null");
		//---------------------------------------------------------------------
		this.workId = workId;
		this.error = error;
		this.result = result;
	}

	public String getWorkId() {
		return workId;
	}

	public boolean hasError() {
		return error != null;
	}

	public WR getResult() {
		Assertion.checkNotNull(result);
		//---------------------------------------------------------------------
		return result;
	}

	public Throwable getError() {
		Assertion.checkNotNull(error);
		//---------------------------------------------------------------------
		return error;
	}

}
