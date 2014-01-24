package io.vertigo.plugins.commons.cache.map;

import java.io.Serializable;

/**
 * @author npiedeloup
 * @version $Id: CacheValue.java,v 1.1 2013/07/10 15:45:32 npiedeloup Exp $
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
