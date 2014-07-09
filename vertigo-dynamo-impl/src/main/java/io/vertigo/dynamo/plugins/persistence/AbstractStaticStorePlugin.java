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
package io.vertigo.dynamo.plugins.persistence;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIAll;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.persistence.Criteria;
import io.vertigo.dynamo.persistence.StorePlugin;

/**
 * Class abstraite des Stores de données static et immutable.
 * @author npiedeloup
 */
public abstract class AbstractStaticStorePlugin implements StorePlugin {

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		return loadList(new DtListURIAll(dtDefinition)).size();
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public <D extends DtObject> DtList<D> loadList(final DtDefinition dtDefinition, final Criteria<D> criteria, final Integer maxRows) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public void put(final DtObject dto) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public void remove(final URI<? extends DtObject> uri) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc} */
	@Override
	public void merge(final DtObject dto) {
		throw new UnsupportedOperationException();
	}

}
