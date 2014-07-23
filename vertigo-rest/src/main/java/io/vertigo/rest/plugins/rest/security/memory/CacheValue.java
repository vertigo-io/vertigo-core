package io.vertigo.rest.plugins.rest.security.memory;

import java.io.Serializable;

/**
 * Keep value, with it's createTime.
 * @author npiedeloup
 */
final class CacheValue {
	private final long createTime;
	private final Serializable value;

	/**
	 * Constructor.
	 * @param value Value
	 */
	CacheValue(final Serializable value) {
		this.value = value;
		createTime = System.currentTimeMillis();
	}

	/**
	 * @return Value
	 */
	Serializable getValue() {
		return value;
	}

	/**
	 * @return Creation time
	 */
	long getCreateTime() {
		return createTime;
	}
}
