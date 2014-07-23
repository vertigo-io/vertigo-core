package io.vertigo.rest.plugins.rest.security.berkeley;

import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * @author npiedeloup
 * @version $Id: CacheValueBinding.java,v 1.2 2013/11/15 17:14:36 npiedeloup Exp $
 */
final class CacheValueBinding extends TupleBinding<CacheValue> {
	private static final String PREFIX = "CacheValue:";
	private final TupleBinding<Serializable> serializableBinding;

	/**
	 * @param serializableBinding TupleBinding des values serializable
	 */
	public CacheValueBinding(final TupleBinding<Serializable> serializableBinding) {
		Assertion.checkNotNull(serializableBinding);
		//---------------------------------------------------------------------
		this.serializableBinding = serializableBinding;
	}

	/** {@inheritDoc} */
	@Override
	public CacheValue entryToObject(final TupleInput ti) {
		final String prefix = ti.readString();
		Assertion.checkArgument(PREFIX.equals(prefix), "L'entr�e n'est pas du bon type {0}", prefix);
		final long createTime = ti.readLong();
		final Serializable value = serializableBinding.entryToObject(ti);
		return new CacheValue(value, createTime);
	}

	/** {@inheritDoc} */
	@Override
	public void objectToEntry(final CacheValue value, final TupleOutput to) {
		final CacheValue cacheValue = value;
		to.writeString(PREFIX);
		to.writeLong(cacheValue.getCreateTime());
		serializableBinding.objectToEntry(cacheValue.getValue(), to);
	}
}
