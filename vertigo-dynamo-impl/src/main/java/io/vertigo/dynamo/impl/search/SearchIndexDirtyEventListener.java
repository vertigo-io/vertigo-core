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
package io.vertigo.dynamo.impl.search;

import io.vertigo.commons.event.Event;
import io.vertigo.commons.event.EventListener;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.search.SearchManager;

import java.util.Collections;
import java.util.List;

/**
 * Declare index dirty on event.
 * @author npiedeloup
 */
final class SearchIndexDirtyEventListener implements EventListener<URI> {

	private final SearchManager searchManager;

	/**
	 * Constructor.
	 * @param searchManager SearchManager
	 */
	SearchIndexDirtyEventListener(final SearchManager searchManager) {
		this.searchManager = searchManager;
	}

	/** {@inheritDoc} */
	@Override
	public void onEvent(final Event<URI> event) {
		final URI uri = event.getPayload();
		//On ne traite l'event que si il porte sur un KeyConcept
		if (uri.getDefinition().getStereotype() == DtStereotype.KeyConcept
				&& searchManager.hasIndexDefinitionByKeyConcept(uri.getDefinition())) {
			final List<URI<? extends KeyConcept>> list = Collections.<URI<? extends KeyConcept>> singletonList(uri);
			searchManager.markAsDirty(list);
		}
	}
}
