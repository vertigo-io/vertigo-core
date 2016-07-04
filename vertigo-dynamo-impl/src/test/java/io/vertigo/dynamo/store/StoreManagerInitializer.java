/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.store;

import javax.inject.Inject;

import io.vertigo.core.spaces.component.ComponentInitializer;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamock.domain.car.Car;

/**
 * Initialisation des listes de références.
 *
 * @author jmforhan
 */
public class StoreManagerInitializer implements ComponentInitializer {
	@Inject
	private StoreManager storeManager;

	/** {@inheritDoc} */
	@Override
	public void init() {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(Car.class);
		storeManager.getDataStoreConfig().registerCacheable(dtDefinition, 3600, true, true);
	}
}
