package io.vertigo.struts2.plugins.context.berkeley;

import io.vertigo.core.lang.Assertion;

import java.io.Serializable;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * @author npiedeloup
 */
final class CacheValueBinding extends TupleBinding {
	private static final String PREFIX = "CacheValue:";
	private final TupleBinding serializableBinding;

	/**
	 * @param serializableBinding TupleBinding des values serializable
	 */
	public CacheValueBinding(final TupleBinding serializableBinding) {
		Assertion.checkNotNull(serializableBinding);
		//---------------------------------------------------------------------
		this.serializableBinding = serializableBinding;
	}

	/** {@inheritDoc} */
	@Override
	public Object entryToObject(final TupleInput ti) {
		final String prefix = ti.readString();
		Assertion.checkArgument(PREFIX.equals(prefix), "L'entrée n'est pas du bon type {0}", prefix);
		final long createTime = ti.readLong();
		final Serializable value = (Serializable) serializableBinding.entryToObject(ti);
		return new CacheValue(value, createTime);
	}

	/** {@inheritDoc} */
	@Override
	public void objectToEntry(final Object value, final TupleOutput to) {
		final CacheValue cacheValue = (CacheValue) value;
		to.writeString(PREFIX);
		to.writeLong(cacheValue.getCreateTime());
		serializableBinding.objectToEntry(cacheValue.getValue(), to);
	}
}
