package io.vertigo.rest.plugins.rest.security.memory;

import io.vertigo.kernel.lang.Assertion;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Delayed key of SecurityToken.
 * @author npiedeloup (15 juil. 2014 17:56:15)
 */
final class DelayedKey implements Delayed {
	private final long timeoutTime;
	private final String key;

	/**
	 * Constructor.
	 * @param key Security Token key
	 * @param timeoutTime When key expired 
	 */
	public DelayedKey(final String key, final long timeoutTime) {
		this.key = key;
		this.timeoutTime = timeoutTime;
	}

	/** {@inheritDoc} */
	public int compareTo(final Delayed o) {
		Assertion.checkArgument(o instanceof DelayedKey, "Only DelayedKey is supported ({0})", o.getClass());
		//-----------------------------------------------------------------
		return (int) (timeoutTime - ((DelayedKey) o).timeoutTime);
	}

	/** {@inheritDoc} */
	public long getDelay(final TimeUnit unit) {
		return unit.convert(timeoutTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @return Security Token key
	 */
	public final String getKey() {
		return key;
	}
}
