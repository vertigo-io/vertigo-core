/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.struts2.plugins.context.berkeley;

import io.vertigo.commons.codec.Codec;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

import java.io.Serializable;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * @author npiedeloup
 */
final class SerializableBinding extends TupleBinding {
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
	public Object entryToObject(final TupleInput ti) {
		final String prefix = ti.readString();
		Assertion.checkArgument(PREFIX.equals(prefix), "L'entrée n'est pas du bon type {0}", prefix);
		try {
			final int size = ti.readInt();
			final byte[] buffer = new byte[size];
			ti.readFast(buffer);
			return codec.decode(buffer);
		} catch (final Exception e) {
			throw new WrappedException(e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void objectToEntry(final Object value, final TupleOutput to) {
		to.writeString(PREFIX);
		try {
			final byte[] buffer = codec.encode((Serializable) value);
			to.writeInt(buffer.length);
			to.writeFast(buffer);
		} catch (final Exception e) {
			throw new WrappedException(e);
		}
	}
}
