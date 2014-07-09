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
package io.vertigo.dynamo.impl.collections.functions.fulltext;

import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.IndexPlugin;
import io.vertigo.kernel.lang.Assertion;

import java.util.Collection;

/**
 * Fonction de filtrage en full text.
 * @author npiedeloup
 * @param <D> Type de l'objet
 */
public final class FullTextFilterFunction<D extends DtObject> implements DtListFunction<D> {
	private final IndexPlugin plugin;
	private final String keywords;
	private final int maxRows;
	private final Collection<DtField> searchedFields;

	/**
	 * Constructeur.
	 * @param keywords Liste de mot-clé.
	 * @param maxRows Nombre maximum de lignes retournées
	 * @param searchedFields Liste des champs surlesquels portent la recherche (nullable : tous)
	 * @param indexerPlugin Plugin dd'indexation et de recherche
	 */
	public FullTextFilterFunction(final String keywords, final int maxRows, final Collection<DtField> searchedFields, final IndexPlugin indexerPlugin) {
		Assertion.checkNotNull(keywords); //peut être vide
		Assertion.checkNotNull(indexerPlugin);
		//-----------------------------------------------------------------
		this.keywords = keywords;
		this.maxRows = maxRows;
		this.searchedFields = searchedFields;
		this.plugin = indexerPlugin;
	}

	/** {@inheritDoc} */
	public DtList<D> apply(final DtList<D> dtc) {
		Assertion.checkNotNull(dtc);
		//----------------------------------------------------------------------
		return plugin.getCollection(keywords, searchedFields, maxRows, null, dtc);
	}
}
