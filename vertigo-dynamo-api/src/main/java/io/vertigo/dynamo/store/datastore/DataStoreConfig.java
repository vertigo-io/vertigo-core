/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.store.datastore;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;

/**
 * Data store configuration.
 *
 * @author pchretien
 */
public interface DataStoreConfig {

	/**
	 * Register DtDefinition as Cacheable and define cache behaviors.
	 * @param dtDefinition Dt definition
	 * @param timeToLiveInSeconds time to live in cache
	 * @param isReloadedByList Set if reload should be done by full list or one by one when missing
	 * @param serializeElements Set if elements should be serialized or not (serialization guarantee elements are cloned and not modified)
	 */
	void registerCacheable(
			final DtDefinition dtDefinition,
			final int timeToLiveInSeconds,
			final boolean isReloadedByList,
			boolean serializeElements);

	/**
	 * @param dataSpace the dataSpace
	 * @return connectionName use for this store
	 */
	String getConnectionName(String dataSpace);

}
