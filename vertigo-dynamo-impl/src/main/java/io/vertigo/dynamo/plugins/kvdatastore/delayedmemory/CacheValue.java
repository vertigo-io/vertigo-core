package io.vertigo.dynamo.plugins.kvdatastore.delayedmemory;


/**
 * Keep value, with it's createTime.
 * @author npiedeloup
 */
final class CacheValue {
	private final long createTime;
	private final Object value;

	/**
	 * Constructor.
	 * @param value Value
	 */
	CacheValue(final Object value) {
		this.value = value;
		createTime = System.currentTimeMillis();
	}

	/**
	 * @return Value
	 */
	Object getValue() {
		return value;
	}

	/**
	 * @return Creation time
	 */
	long getCreateTime() {
		return createTime;
	}
}
