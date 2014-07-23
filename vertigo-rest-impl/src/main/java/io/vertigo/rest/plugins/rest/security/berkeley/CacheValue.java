package io.vertigo.rest.plugins.rest.security.berkeley;

import java.io.Serializable;

/**
 * @author npiedeloup
 * @version $Id: CacheValue.java,v 1.1 2013/09/23 16:25:27 npiedeloup Exp $
 */
final class CacheValue {
	private final long createTime;
	private final Serializable value;

	CacheValue(final Serializable value) {
		this(value, System.currentTimeMillis());
	}

	CacheValue(final Serializable value, final long createTime) {
		this.value = value;
		this.createTime = createTime;
	}

	Serializable getValue() {
		return value;
	}

	long getCreateTime() {
		return createTime;
	}
}
