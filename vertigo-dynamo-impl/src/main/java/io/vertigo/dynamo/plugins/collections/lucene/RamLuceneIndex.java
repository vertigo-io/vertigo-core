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

import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.MessageText;
import io.vertigo.lang.Modifiable;
import io.vertigo.lang.VUserException;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.BooleanQuery.TooManyClauses;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 * Implémentation Ram de l'index Lucene.
 * Il existe une seule instance par JVM.
 * Il ne doit aussi exister qu'un seul writer.
 *
 * @author  pchretien, npiedeloup
 * @param <D> Type d'objet
 */
final class RamLuceneIndex<D extends DtObject> implements LuceneIndex<D>, Modifiable {
	static final String SORT_FIELD_PREFIX = "4SORT_";

	private static final long serialVersionUID = -8810115927887053497L;
	private boolean modifiable = true;
	//DtDefinition est non serializable
	private final DtDefinition dtDefinition;
	private final Map<String, D> indexedObjectPerPk = new HashMap<>();
	private final Directory directory;
	private final Analyzer indexAnalyser;

	/**
	 * @param dtDefinition DtDefinition des objets indexés
	 * @throws IOException Exception I/O
	 */
	RamLuceneIndex(final DtDefinition dtDefinition) throws IOException {
		Assertion.checkNotNull(dtDefinition);
		//-----
		indexAnalyser = new DefaultAnalyzer(false); //les stop word marchent mal si asymétrique entre l'indexation et la query
		this.dtDefinition = dtDefinition;
		directory = new RAMDirectory();

		//l'index est crée automatiquement la premiere fois.
		buildIndex();
	}

	private void buildIndex() throws IOException {
		try (final IndexWriter indexWriter = createIndexWriter()) {
			// we are creating an empty index if it does not exist
		}
	}

	private IndexWriter createIndexWriter() throws IOException {
		checkModifiable();
		//-----
		final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_9, indexAnalyser);
		return new IndexWriter(directory, config);
	}

	/**
	 * @param pkValue Clé de l'objet
	 * @return Objet associé dans cet index.
	 */
	private D getDtObjectIndexed(final String pkValue) {
		return indexedObjectPerPk.get(pkValue);
	}

	/**
	 * Associe une clé à un objet.
	 * @param pkValue Valeur de la clé
	 * @param dto Objet associé
	 */
	private void mapDocument(final String pkValue, final D dto) {
		indexedObjectPerPk.put(pkValue, dto);
	}

	private void checkModifiable() {
		Assertion.checkArgument(modifiable, "mode écriture désactivé");
	}

	/** {@inheritDoc} */
	@Override
	public boolean isModifiable() {
		return modifiable;
	}

	/**
	 * Passe l'index en mode non modifiable.
	 */
	void makeUnmodifiable() {
		checkModifiable();
		//-----
		modifiable = false;
	}

	/** {@inheritDoc} */
	@Override
	public DtList<D> executeQuery(final Query query, final int skip, final int top, final Sort sort) throws IOException {
		try (final IndexReader indexReader = DirectoryReader.open(directory)) {
			final IndexSearcher searcher = new IndexSearcher(indexReader);
			//1. Exécution des la Requête
			final TopDocs topDocs;
			if (sort != null) {
				topDocs = searcher.search(query, skip + top, sort);
			} else {
				topDocs = searcher.search(query, skip + top);
			}
			//2. Traduction du résultat Lucene en une Collection
			return translateDocs(searcher, topDocs, skip, top);
		} catch (final TooManyClauses e) {
			throw new VUserException(new MessageText(Resources.DYNAMO_COLLECTIONS_INDEXER_TOO_MANY_CLAUSES));
		}
	}

	private DtList<D> translateDocs(final IndexSearcher searcher, final TopDocs topDocs, final int skip, final int top) throws IOException {
		final DtField pkField = dtDefinition.getIdField().get();

		final DtList<D> dtcResult = new DtList<>(dtDefinition);
		final int resultLength = topDocs.scoreDocs.length;
		if (resultLength > skip) {
			for (int i = skip; i < Math.min(skip + top, resultLength); i++) {
				final ScoreDoc scoreDoc = topDocs.scoreDocs[i];
				final Document document = searcher.doc(scoreDoc.doc);
				dtcResult.add(getDtObjectIndexed(document.get(pkField.name())));
			}
		}
		return dtcResult;
	}

	@Override
	public void addAll(final DtList<D> fullDtc, final boolean storeValue) throws IOException {
		checkModifiable();
		//-----
		try (final IndexWriter indexWriter = createIndexWriter()) {
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
				mapDocument(indexedPkValue, dto);
			}
		}
	}

	private static PersistenceManager getPersistenceManager() {
		return Home.getComponentSpace().resolve(PersistenceManager.class);
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

	private static IndexableField createKeyword(final String fieldName, final String fieldValue, final boolean storeValue) {
		return new StringField(fieldName, fieldValue, storeValue ? Field.Store.YES : Field.Store.NO);
	}

	private static IndexableField createIndexed(final String fieldName, final String fieldValue, final boolean storeValue) {
		return new TextField(fieldName, fieldValue, storeValue ? Field.Store.YES : Field.Store.NO);
	}

}
