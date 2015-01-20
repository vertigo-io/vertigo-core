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
import io.vertigo.core.Home;
import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.impl.collections.IndexPlugin;
import io.vertigo.dynamo.impl.collections.functions.sort.SortState;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.MessageText;
import io.vertigo.lang.Option;
import io.vertigo.lang.VUserException;
import io.vertigo.util.StringUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.TooManyClauses;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;

/**
 * Plugin de d'indexation de DtList utilisant Lucene en Ram.
 *
 * @author npiedeloup
 */
public final class LuceneIndexPlugin implements IndexPlugin {
	private static final String SORT_FIELD_PREFIX = "4SORT_";
	private final Analyzer indexAnalyser;
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
		indexAnalyser = new DefaultAnalyzer(false); //les stop word marchent mal si asymétrique entre l'indexation et la query
		queryAnalyser = new DefaultAnalyzer(false);
		localeManager.add(Resources.class.getName(), Resources.values());
	}

	private static PersistenceManager getPersistenceManager() {
		return Home.getComponentSpace().resolve(PersistenceManager.class);
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
		final RamLuceneIndex<D> luceneDb = new RamLuceneIndex<>(fullDtc.getDefinition(), indexAnalyser);
		try (final IndexWriter indexWriter = luceneDb.createIndexWriter()) {
			final DtField pkField = fullDtc.getDefinition().getIdField().get();
			final Collection<DtField> dtFields = fullDtc.getDefinition().getFields();

			for (final D dto : fullDtc) {
				final Document document = new Document();
				final Object pkValue = pkField.getDataAccessor().getValue(dto);
				Assertion.checkNotNull(pkValue, "Indexed DtObject must have a not null primary key. {0}.{1} was null.", fullDtc.getDefinition().getName(), pkField.name());
				final String indexedPkValue = String.valueOf(pkValue);
				document.add(createKeyword(pkField.name(), indexedPkValue, true));
				for (final DtField dtField : dtFields) {
					final Object value = dtField.getDataAccessor().getValue(dto);
					if (value != null) {
						if (value instanceof String) {
							final String valueAsString = getStringValue(dto, dtField);
							document.add(createIndexed(dtField.name(), valueAsString, storeValue));
							//we add a special field for sorting
							document.add(createKeyword(SORT_FIELD_PREFIX + dtField.name(), valueAsString.toLowerCase(), storeValue));
						} else if (value instanceof Date) {
							final String valueAsString = DateTools.dateToString((Date) value, DateTools.Resolution.DAY);
							document.add(createKeyword(dtField.name(), valueAsString, storeValue));
						} else {
							document.add(createKeyword(dtField.name(), value.toString(), storeValue));
						}
					}
				}
				indexWriter.addDocument(document);
				luceneDb.mapDocument(indexedPkValue, dto);
			}
			luceneDb.makeUnmodifiable();
			return luceneDb;
		}
	}

	private static String getStringValue(final DtObject dto, final DtField field) {
		final String stringValue;
		final Object value = field.getDataAccessor().getValue(dto);
		if (value != null) {
			if (field.getType() == DtField.FieldType.FOREIGN_KEY && getPersistenceManager().getMasterDataConfiguration().containsMasterData(field.getFkDtDefinition())) {
				//TODO voir pour mise en cache de cette navigation
				final DtListURIForMasterData mdlUri = getPersistenceManager().getMasterDataConfiguration().getDtListURIForMasterData(field.getFkDtDefinition());
				final DtField displayField = mdlUri.getDtDefinition().getDisplayField().get();
				final URI<DtObject> uri = new URI<>(field.getFkDtDefinition(), value);
				final DtObject fkDto = getPersistenceManager().getBroker().get(uri);
				final Object displayValue = displayField.getDataAccessor().getValue(fkDto);
				stringValue = displayField.getDomain().getFormatter().valueToString(displayValue, displayField.getDomain().getDataType());
			} else {
				stringValue = String.valueOf(field.getDataAccessor().getValue(dto));
			}
			return stringValue.trim();
		}
		return null;
	}

	private <D extends DtObject> DtList<D> getCollection(final String keywords, final Collection<DtField> searchedFields, final List<ListFilter> listFilters, final int skip, final int top, final Option<SortState> sortState, final Option<DtField> boostedField, final LuceneIndex<D> index) throws IOException {
		Assertion.checkNotNull(index);
		Assertion.checkNotNull(searchedFields);
		//-----
		final Query keywordsQuery = createKeywordQuery(keywords, searchedFields, boostedField);
		final Query filteredQuery;
		if (!listFilters.isEmpty()) {
			filteredQuery = createFilteredQuery(keywordsQuery, listFilters);
		} else {
			filteredQuery = keywordsQuery;
		}
		final Sort sortQuery = createSortQuery(sortState);
		return executeQuery(index, filteredQuery, skip, top, sortQuery);
	}

	private Sort createSortQuery(final Option<SortState> sortStateOpt) {
		if (sortStateOpt.isDefined()) {
			final SortState sortState = sortStateOpt.get();
			Assertion.checkArgument(sortState.isIgnoreCase(), "Sort by index is always case insensitive. Set sortState.isIgnoreCase to true.");
			//-----
			final SortField.Type luceneType = SortField.Type.STRING; //TODO : check if other type are necessary
			final String fieldName = SORT_FIELD_PREFIX + sortState.getFieldName(); //can't use the tokenized field with sorting
			final SortField sortField = new SortField(fieldName, luceneType, sortState.isDesc());
			final boolean nullLast = sortState.isDesc() ? !sortState.isNullLast() : sortState.isNullLast(); //oh yeah : lucene use nullLast first then revert list if sort Desc :)
			sortField.setMissingValue(nullLast ? SortField.STRING_LAST : SortField.STRING_FIRST);
			return new Sort(sortField);
		}
		return null;//default null -> sort by score
	}

	private static <D extends DtObject> DtList<D> executeQuery(final LuceneIndex<D> luceneDb, final Query query, final int skip, final int top, final Sort sort) throws IOException {
		try (final IndexReader indexReader = luceneDb.createIndexReader()) {
			final IndexSearcher searcher = new IndexSearcher(indexReader);
			//1. Exécution des la Requête
			final TopDocs topDocs;
			if (sort != null) {
				topDocs = searcher.search(query, skip + top, sort);
			} else {
				topDocs = searcher.search(query, skip + top);
			}
			//2. Traduction du résultat Lucene en une Collection
			return translateDocs(luceneDb, searcher, topDocs, skip, top);
		} catch (final TooManyClauses e) {
			throw new VUserException(new MessageText(Resources.DYNAMO_COLLECTIONS_INDEXER_TOO_MANY_CLAUSES));
		}
	}

	private static <D extends DtObject> DtList<D> translateDocs(final LuceneIndex<D> luceneDb, final IndexSearcher searcher, final TopDocs topDocs, final int skip, final int top) throws IOException {
		final DtDefinition dtDefinition = luceneDb.getDtDefinition();
		final DtField pkField = dtDefinition.getIdField().get();

		final DtList<D> dtcResult = new DtList<>(dtDefinition);
		final int resultLength = topDocs.scoreDocs.length;
		if (resultLength > skip) {
			for (int i = skip; i < Math.min(skip + top, resultLength); i++) {
				final ScoreDoc scoreDoc = topDocs.scoreDocs[i];
				final Document document = searcher.doc(scoreDoc.doc);
				dtcResult.add(luceneDb.getDtObjectIndexed(document.get(pkField.name())));
			}
		}
		return dtcResult;
	}

	private Query createKeywordQuery(final String keywords, final Collection<DtField> searchedFieldList, final Option<DtField> boostedField) throws IOException {
		if (StringUtil.isEmpty(keywords)) {
			return new MatchAllDocsQuery();
		}
		//-----
		final BooleanQuery query = new BooleanQuery();
		for (final DtField dtField : searchedFieldList) {
			final Query queryWord = createParsedKeywordsQuery(dtField.name(), keywords);
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

	private Query createParsedKeywordsQuery(final String fieldName, final String keywords) throws IOException {
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

	private static IndexableField createKeyword(final String fieldName, final String fieldValue, final boolean storeValue) {
		return new StringField(fieldName, fieldValue, storeValue ? Field.Store.YES : Field.Store.NO);
	}

	private static IndexableField createIndexed(final String fieldName, final String fieldValue, final boolean storeValue) {
		return new TextField(fieldName, fieldValue, storeValue ? Field.Store.YES : Field.Store.NO);
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
