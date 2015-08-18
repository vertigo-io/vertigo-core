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
package io.vertigo.dynamo.plugins.collections.lucene;

import io.vertigo.commons.cache.CacheConfig;
import io.vertigo.commons.cache.CacheManager;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.impl.collections.IndexPlugin;
import io.vertigo.dynamo.impl.collections.functions.sort.SortState;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

/**
 * Plugin de d'indexation de DtList utilisant Lucene en Ram.
 *
 * @author npiedeloup
 */
public final class LuceneIndexPlugin implements IndexPlugin {

	//	private final int nbTermsMax = 1024; //a paramétrer
	private final CacheManager cacheManager;

	/**
	 * Constructeur.
	 * @param localeManager Manager des messages localisés
	 * @param cacheManager Manager des caches
	 */
	@Inject
	public LuceneIndexPlugin(final LocaleManager localeManager, final CacheManager cacheManager) {
		Assertion.checkNotNull(localeManager);
		Assertion.checkNotNull(cacheManager);
		//-----
		this.cacheManager = cacheManager;
		localeManager.add(Resources.class.getName(), Resources.values());
	}

	private <D extends DtObject> LuceneIndex<D> indexList(final DtList<D> fullDtc, final boolean storeValue) throws IOException {
		//TODO : gestion du cache a revoir... et le lien avec le CacheStore.
		//L'index devrait être interrogé par le Broker ? on pourrait alors mettre en cache dans le DataCache.
		final DtListURI dtcUri = fullDtc.getURI();
		final boolean useCache = dtcUri != null; //no cache if no URI
		LuceneIndex<D> index;
		if (useCache) {
			final String indexName = "INDEX_" + dtcUri.toURN();
			final String cacheContext = getContext(fullDtc.getDefinition());
			//TODO non threadSafe.
			cacheManager.addCache(cacheContext, new CacheConfig("dataCache", 1000, 1800, 3600));
			index = (LuceneIndex<D>) cacheManager.get(cacheContext, indexName);
			if (index == null) {
				index = createIndex(fullDtc, storeValue);
				cacheManager.put(getContext(fullDtc.getDefinition()), indexName, index);
			}
		} else {
			index = createIndex(fullDtc, storeValue);
		}
		return index;
	}

	private static String getContext(final DtDefinition dtDefinition) {
		//TODO : on met le même context que le cacheStore pour être sur la même durée de vie que la liste
		return "DataCache:" + dtDefinition.getName();
	}

	private static <D extends DtObject> LuceneIndex<D> createIndex(final DtList<D> fullDtc, final boolean storeValue) throws IOException {
		Assertion.checkNotNull(fullDtc);
		//-----
		final RamLuceneIndex<D> luceneDb = new RamLuceneIndex<>(fullDtc.getDefinition());
		luceneDb.addAll(fullDtc, storeValue);
		luceneDb.makeUnmodifiable();
		return luceneDb;
	}

	/**
	 * Filtre une liste par des mots clés et une recherche fullText.
	 * @param <D> type d'objet de la liste
	 * @param keywords Mots clés de la recherche
	 * @param searchedFields Liste des champs sur lesquels porte la recheche
	 * @param listFilters Liste des filtres supplémentaires (facettes, sécurité, ...)
	 * @param skip Nombre de résultat à sauter
	 * @param top Nombre de résultat maximum
	 * @param boostedField Liste des champs boostés (boost de 4 en dur)
	 * @param dtc Liste d'origine à filtrer
	 * @return Liste résultat
	 */
	@Override
	public <D extends DtObject> DtList<D> getCollection(final String keywords, final Collection<DtField> searchedFields, final List<ListFilter> listFilters, final int skip, final int top, final Option<SortState> sortState, final Option<DtField> boostedField, final DtList<D> dtc) {
		if (top == 0) { //like arrayList sublist implementation : accept top==0 but return empty list.
			return new DtList<>(dtc.getDefinition());
		}
		try {
			final LuceneIndex<D> index = indexList(dtc, false);
			return index.<D> getCollection(keywords, searchedFields, listFilters, skip, top, sortState, boostedField);
		} catch (final IOException e) {
			throw new RuntimeException("Erreur d'indexation", e);
		}
	}

}
