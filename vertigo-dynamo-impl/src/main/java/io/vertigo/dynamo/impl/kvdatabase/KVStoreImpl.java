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
package io.vertigo.dynamo.impl.kvdatabase;

import io.vertigo.dynamo.kvdatabase.KVStore;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.util.List;

/**
 * @author npiedeloup, pchretien
 */
public final class KVStoreImpl implements KVStore {
	private final String collection;
	private final KVDataStorePlugin kvDataStorePlugin;

	public KVStoreImpl(final String collection, KVDataStorePlugin kvDataStorePlugin) {
		Assertion.checkArgNotEmpty(collection);
		Assertion.checkNotNull(kvDataStorePlugin);
		//-----
		this.collection = collection;
		this.kvDataStorePlugin = kvDataStorePlugin;
	}

	@Override
	public void put(final String id, final Object objet) {
		kvDataStorePlugin.put(collection, id, objet);
	}

	@Override
	public void remove(final String id) {
		kvDataStorePlugin.remove(collection, id);
	}

	@Override
	public <C> Option<C> find(final String id, final Class<C> clazz) {
		return kvDataStorePlugin.find(collection, id, clazz);
	}

	@Override
	public <C> List<C> findAll(final int skip, final Integer limit, final Class<C> clazz) {
		return kvDataStorePlugin.findAll(collection, skip, limit, clazz);
	}
}
