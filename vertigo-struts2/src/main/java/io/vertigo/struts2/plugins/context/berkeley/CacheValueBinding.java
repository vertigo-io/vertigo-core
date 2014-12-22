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

import io.vertigo.lang.Assertion;

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
		//-----
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
