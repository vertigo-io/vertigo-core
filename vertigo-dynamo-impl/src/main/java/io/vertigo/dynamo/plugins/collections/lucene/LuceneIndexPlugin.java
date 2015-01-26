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
import io.vertigo.commons.locale.LocaleManager;
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
import io.vertigo.util.StringUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

/**
 * Plugin de d'indexation de DtList utilisant Lucene en Ram.
 *
 * @author npiedeloup
 */
public final class LuceneIndexPlugin implements IndexPlugin {

	private final Analyzer queryAnalyser;
	//	private final int nbTermsMax = 1024; //a paramètrer
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
		queryAnalyser = new DefaultAnalyzer(false);
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

	private <D extends DtObject> LuceneIndex<D> createIndex(final DtList<D> fullDtc, final boolean storeValue) throws IOException {
		Assertion.checkNotNull(fullDtc);
		//-----
		final RamLuceneIndex<D> luceneDb = new RamLuceneIndex<>(fullDtc.getDefinition());
		luceneDb.addAll(fullDtc, storeValue);
		luceneDb.makeUnmodifiable();
		return luceneDb;
	}

	private <D extends DtObject> DtList<D> getCollection(final String keywords, final Collection<DtField> searchedFields, final List<ListFilter> listFilters, final int skip, final int top, final Option<SortState> sortState, final Option<DtField> boostedField, final LuceneIndex<D> index) throws IOException {
		Assertion.checkNotNull(index);
		Assertion.checkNotNull(searchedFields);
		//-----
		final Query filterQuery = createFilterQuery(keywords, searchedFields, listFilters, boostedField);
		final Sort sortQuery = createSortQuery(sortState);
		return index.executeQuery(filterQuery, skip, top, sortQuery);
	}

	private Query createFilterQuery(final String keywords, final Collection<DtField> searchedFields, final List<ListFilter> listFilters, final Option<DtField> boostedField) throws IOException {
		final Query filteredQuery;
		final Query keywordsQuery = createKeywordQuery(keywords, searchedFields, boostedField);
		if (!listFilters.isEmpty()) {
			filteredQuery = createFilteredQuery(keywordsQuery, listFilters);
		} else {
			filteredQuery = keywordsQuery;
		}
		return filteredQuery;
	}

	private static Sort createSortQuery(final Option<SortState> sortStateOpt) {
		if (sortStateOpt.isDefined()) {
			final SortState sortState = sortStateOpt.get();
			Assertion.checkArgument(sortState.isIgnoreCase(), "Sort by index is always case insensitive. Set sortState.isIgnoreCase to true.");
			//-----
			final SortField.Type luceneType = SortField.Type.STRING; //TODO : check if other type are necessary
			final String fieldName = RamLuceneIndex.SORT_FIELD_PREFIX + sortState.getFieldName(); //can't use the tokenized field with sorting
			final SortField sortField = new SortField(fieldName, luceneType, sortState.isDesc());
			final boolean nullLast = sortState.isDesc() ? !sortState.isNullLast() : sortState.isNullLast(); //oh yeah : lucene use nullLast first then revert list if sort Desc :)
			sortField.setMissingValue(nullLast ? SortField.STRING_LAST : SortField.STRING_FIRST);
			return new Sort(sortField);
		}
		return null;//default null -> sort by score
	}

	private Query createKeywordQuery(final String keywords, final Collection<DtField> searchedFieldList, final Option<DtField> boostedField) throws IOException {
		if (StringUtil.isEmpty(keywords)) {
			return new MatchAllDocsQuery();
		}
		//-----
		final BooleanQuery query = new BooleanQuery();
		for (final DtField dtField : searchedFieldList) {
			final Query queryWord = createParsedKeywordsQuery(queryAnalyser, dtField.name(), keywords);
			if (boostedField.isDefined() && dtField.equals(boostedField.get())) {
				queryWord.setBoost(4);
			}
			query.add(queryWord, BooleanClause.Occur.SHOULD);
		}
		return query;
	}

	private Query createFilteredQuery(final Query keywordsQuery, final List<ListFilter> filters) {
		final BooleanQuery query = new BooleanQuery();
		query.add(keywordsQuery, BooleanClause.Occur.MUST);

		for (final ListFilter filter : filters) {
			final StandardQueryParser queryParser = new StandardQueryParser(queryAnalyser);
			try {
				query.add(queryParser.parse(filter.getFilterValue(), null), isExclusion(filter) ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.MUST);
			} catch (final QueryNodeException e) {
				throw new RuntimeException("Erreur lors de la création du filtrage de la requete", e);
			}
		}
		return query;
	}

	private static boolean isExclusion(final ListFilter listFilter) {
		final String listFilterValue = listFilter.getFilterValue().trim();
		return listFilterValue.startsWith("-");
	}

	private static Query createParsedKeywordsQuery(final Analyzer queryAnalyser, final String fieldName, final String keywords) throws IOException {
		final BooleanQuery query = new BooleanQuery();
		final Reader reader = new StringReader(keywords);
		try (final TokenStream tokenStream = queryAnalyser.tokenStream(fieldName, reader)) {
			tokenStream.reset();
			try {
				final CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
				while (tokenStream.incrementToken()) {
					final String term = new String(termAttribute.buffer(), 0, termAttribute.length());
					final PrefixQuery termQuery = new PrefixQuery(new Term(fieldName, term));
					query.add(termQuery, BooleanClause.Occur.MUST);
				}
			} finally {
				reader.reset();
				tokenStream.end();
			}
		}
		return query;
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
			return this.<D> getCollection(keywords, searchedFields, listFilters, skip, top, sortState, boostedField, index);
		} catch (final IOException e) {
			throw new RuntimeException("Erreur d'indexation", e);
		}
	}
}
