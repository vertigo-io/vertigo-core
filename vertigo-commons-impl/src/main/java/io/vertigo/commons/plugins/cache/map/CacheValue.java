package io.vertigo.commons.plugins.cache.map;

import java.io.Serializable;

/**
 * @author npiedeloup
 */
final class CacheValue {
	private final long createTime;
	private final Serializable value;

	CacheValue(final Serializable value) {
		this.value = value;
		createTime = System.currentTimeMillis();
	}

	Serializable getValue() {
		return value;
	}

	long getCreateTime() {
		return createTime;
	}
}
