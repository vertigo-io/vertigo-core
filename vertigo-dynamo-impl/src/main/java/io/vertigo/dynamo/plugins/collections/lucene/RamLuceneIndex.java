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

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Modifiable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

/**
 * Implémentation Ram de l'index Lucene.
 * Il existe une seule instance par JVM.
 * Il ne doit aussi exister qu'un seul writer.
 *
 * @author  pchretien, npiedeloup
 */
final class RamLuceneIndex<D extends DtObject> implements LuceneIndex<D>, Modifiable {
	private static final long serialVersionUID = -8810115927887053497L;
	private boolean modifiable = true;
	//DtDefinition est non serializable
	private final DtDefinition dtDefinition;
	private final Map<String, D> indexedObjectPerPk = new HashMap<>();
	private final Directory directory;
	private final Analyzer analyzer;

	/**
	 * @param dtDefinition DtDefinition des objets indexés
	 * @param analyzer Analyzer à utiliser
	 * @throws IOException Exception I/O
	 */
	RamLuceneIndex(final DtDefinition dtDefinition, final Analyzer analyzer) throws IOException {
		Assertion.checkNotNull(analyzer);
		Assertion.checkNotNull(dtDefinition);
		//-----
		this.analyzer = analyzer;
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

	/** {@inheritDoc} */
	@Override
	public IndexWriter createIndexWriter() throws IOException {
		checkModifiable();
		//-----
		final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		return new IndexWriter(directory, config);
	}

	/** {@inheritDoc} */
	@Override
	public IndexReader createIndexReader() throws IOException {
		return IndexReader.open(directory);
	}

	/** {@inheritDoc} */
	@Override
	public DtDefinition getDtDefinition() {
		return dtDefinition;
	}

	/** {@inheritDoc} */
	@Override
	public D getDtObjectIndexed(final String pkValue) {
		return indexedObjectPerPk.get(pkValue);
	}

	/**
	 * Associe une clé à un objet.
	 * @param pkValue Valeur de la clé
	 * @param dto Objet associé
	 */
	public void mapDocument(final String pkValue, final D dto) {
		checkModifiable();
		//-----
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

	void makeUnmodifiable() {
		checkModifiable();
		//-----
		modifiable = false;
	}
}
