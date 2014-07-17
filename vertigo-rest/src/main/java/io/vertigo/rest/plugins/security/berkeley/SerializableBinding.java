package io.vertigo.rest.plugins.security.berkeley;

import io.vertigo.commons.codec.Codec;
import io.vertigo.kernel.lang.Assertion;

import java.io.Serializable;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * @author npiedeloup
 * @version $Id: SerializableBinding.java,v 1.3 2014/01/29 10:21:01 pchretien Exp $
 */
final class SerializableBinding extends TupleBinding<Serializable> {
	private static final String PREFIX = "Serializable:";
	private final Codec<Serializable, byte[]> codec;

	/**
	 * @param codec codec de serialization
	 */
	public SerializableBinding(final Codec<Serializable, byte[]> codec) {
		Assertion.checkNotNull(codec);
		this.codec = codec;
	}

	/** {@inheritDoc} */
	@Override
	public Serializable entryToObject(final TupleInput ti) {
		final String prefix = ti.readString();
		Assertion.checkArgument(PREFIX.equals(prefix), "L'entr�e n'est pas du bon type {0}", prefix);
		try {
			final int size = ti.readInt();
			final byte[] buffer = new byte[size];
			ti.readFast(buffer);
			return codec.decode(buffer);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void objectToEntry(final Serializable value, final TupleOutput to) {
		to.writeString(PREFIX);
		try {
			final byte[] buffer = codec.encode(value);
			to.writeInt(buffer.length);
			to.writeFast(buffer);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
