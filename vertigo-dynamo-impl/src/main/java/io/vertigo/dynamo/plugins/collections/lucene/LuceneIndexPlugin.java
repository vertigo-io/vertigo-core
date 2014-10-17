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

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.impl.collections.IndexPlugin;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.MessageText;
import io.vertigo.lang.VUserException;
import io.vertigo.util.StringUtil;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Date;

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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.TooManyClauses;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

/**
 * Plugin de d'indexation de DtList utilisant Lucene en Ram.
 * 
 * @author npiedeloup
 */
public final class LuceneIndexPlugin implements IndexPlugin {
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

		//---------------------------------------------------------------------
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
		final String indexName = "INDEX_" + (dtcUri != null ? dtcUri.toURN() : "noURI");
		final String cacheContext = getContext(fullDtc.getDefinition());
		//TODO non threadSafe.
		cacheManager.addCache("dataCache", cacheContext, 1000, 1800, 3600);
		LuceneIndex<D> index = (LuceneIndex<D>) cacheManager.get(cacheContext, indexName);
		if (index == null) {
			index = createIndex(fullDtc, storeValue);
			cacheManager.put(getContext(fullDtc.getDefinition()), indexName, index);
		}
		return index;
	}

	private static String getContext(final DtDefinition dtDefinition) {
		//TODO : on met le même context que le cacheStore pour être sur la même durée de vie que la liste
		return "DataCache:" + dtDefinition.getName();
	}

	private <D extends DtObject> LuceneIndex<D> createIndex(final DtList<D> fullDtc, final boolean storeValue) throws IOException {
		Assertion.checkNotNull(fullDtc);
		//---------------------------------------------------------------------
		final RamLuceneIndex<D> luceneDb = new RamLuceneIndex<>(fullDtc.getDefinition(), indexAnalyser);
		try (final IndexWriter indexWriter = luceneDb.createIndexWriter()) {
			final DtField pkField = fullDtc.getDefinition().getIdField().get();
			final Collection<DtField> dtFields = fullDtc.getDefinition().getFields();

			for (final D dto : fullDtc) {
				final Document document = new Document();
				final String pkValue = String.valueOf(pkField.getDataAccessor().getValue(dto));
				document.add(createKeyword(pkField.getName(), pkValue, true));
				for (final DtField dtField : dtFields) {
					final Object value = dtField.getDataAccessor().getValue(dto);
					if (value != null) {
						if (value instanceof String) {
							document.add(createIndexed(dtField.getName(), getStringValue(dto, dtField), storeValue));
						} else if (value instanceof Date) {
							document.add(createKeyword(dtField.getName(), DateTools.dateToString((Date) value, DateTools.Resolution.DAY), storeValue));
						} else {
							document.add(createKeyword(dtField.getName(), value.toString(), storeValue));
						}
					}
				}
				indexWriter.addDocument(document);
				luceneDb.mapDocument(pkValue, dto);
			}
			luceneDb.makeUnmodifiable();
			return luceneDb;
		}
	}

	private static String getStringValue(final DtObject dto, final DtField field) {
		final String stringValue;
		if (field.getType() == DtField.FieldType.FOREIGN_KEY && getPersistenceManager().getMasterDataConfiguration().containsMasterData(field.getFkDtDefinition())) {
			//TODO voir pour mise en cache de cette navigation
			final DtListURIForMasterData mdlUri = getPersistenceManager().getMasterDataConfiguration().getDtListURIForMasterData(field.getFkDtDefinition());
			final DtField displayField = mdlUri.getDtDefinition().getDisplayField().get();
			final Object fkValue = field.getDataAccessor().getValue(dto);
			if (fkValue != null) {
				final URI<DtObject> uri = new URI<>(field.getFkDtDefinition(), fkValue);
				final DtObject fkDto = getPersistenceManager().getBroker().get(uri);
				final Object value = displayField.getDataAccessor().getValue(fkDto);
				stringValue = displayField.getDomain().getFormatter().valueToString(value, displayField.getDomain().getDataType());
			} else {
				stringValue = null;
			}
		} else {
			stringValue = (String) field.getDataAccessor().getValue(dto);
		}
		return stringValue != null ? stringValue.trim() : null;
	}

	private <D extends DtObject> DtList<D> getCollection(final String keywords, final Collection<DtField> searchedDtFieldList, final int maxRows, final DtField boostedField, final LuceneIndex<D> index) throws IOException {
		Assertion.checkNotNull(index);
		Assertion.checkNotNull(searchedDtFieldList);
		//---------------------------------------------------------------------
		final Query query = createQuery(keywords, searchedDtFieldList, boostedField);
		return executeQuery(index, query, maxRows);
	}

	private static <D extends DtObject> DtList<D> executeQuery(final LuceneIndex<D> luceneDb, final Query query, final int maxRow) throws IOException {
		try (final IndexReader indexReader = luceneDb.createIndexReader()) {
			final IndexSearcher searcher = new IndexSearcher(indexReader);
			//1. Exécution des la Requête
			final TopDocs topDocs = searcher.search(query, null, maxRow);

			//2. Traduction du résultat Lucene en une Collection
			return translateDocs(luceneDb, searcher, topDocs);
		} catch (final TooManyClauses e) {
			throw new VUserException(new MessageText(Resources.DYNAMO_COLLECTIONS_INDEXER_TOO_MANY_CLAUSES));
		}
	}

	private static <D extends DtObject> DtList<D> translateDocs(final LuceneIndex<D> luceneDb, final IndexSearcher searcher, final TopDocs topDocs) throws IOException {
		final DtDefinition dtDefinition = luceneDb.getDtDefinition();
		final DtField pkField = dtDefinition.getIdField().get();

		final DtList<D> dtcResult = new DtList<>(dtDefinition);
		for (final ScoreDoc scoreDoc : topDocs.scoreDocs) {
			final Document document = searcher.doc(scoreDoc.doc);
			dtcResult.add(luceneDb.getDtObjectIndexed(document.get(pkField.getName())));
		}
		return dtcResult;
	}

	private Query createQuery(final String keywords, final Collection<DtField> searchedFieldList, final DtField boostedField) throws IOException {
		if (StringUtil.isEmpty(keywords)) {
			return new MatchAllDocsQuery();
		}
		//----------------
		final BooleanQuery query = new BooleanQuery();

		for (final DtField dtField : searchedFieldList) {
			final Query queryWord = createParsedQuery(dtField.getName(), keywords);
			if (dtField.equals(boostedField)) {
				queryWord.setBoost(4);
			}
			query.add(queryWord, BooleanClause.Occur.SHOULD);
		}
		return query;
	}

	private Query createParsedQuery(final String fieldName, final String keywords) throws IOException {
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
	 * @param maxRows Nombre de résultat maximum
	 * @param boostedField Liste des champs boostés (boost de 4 en dur)
	 * @param dtc Liste source
	 * @return Liste résultat
	 */
	public <D extends DtObject> DtList<D> getCollection(final String keywords, final Collection<DtField> searchedFields, final int maxRows, final DtField boostedField, final DtList<D> dtc) {
		try {
			final LuceneIndex<D> index = indexList(dtc, false);
			return this.<D> getCollection(keywords, searchedFields, maxRows, boostedField, index);
		} catch (final IOException e) {
			throw new RuntimeException("Erreur d'indexation", e);
		}
	}
}
