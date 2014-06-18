package io.vertigo.dynamo.plugins.collections.lucene;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Modifiable;

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
		//---------------------------------------------------------------------
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
	public IndexWriter createIndexWriter() throws IOException {
		checkModifiable();
		//---------------------------------------------------------------------
		final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
		return new IndexWriter(directory, config);
	}

	/** {@inheritDoc} */
	public IndexReader createIndexReader() throws IOException {
		return IndexReader.open(directory);
	}

	/** {@inheritDoc} */
	public DtDefinition getDtDefinition() {
		return dtDefinition;
	}

	/** {@inheritDoc} */
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
		//---------------------------------------------------------------------
		indexedObjectPerPk.put(pkValue, dto);
	}

	private void checkModifiable() {
		Assertion.checkArgument(modifiable, "mode écriture désactivé");
	}

	/** {@inheritDoc} */
	public boolean isModifiable() {
		return modifiable;
	}

	/** {@inheritDoc} */
	public void makeUnmodifiable() {
		checkModifiable();
		//---------------------------------------------------------------------
		modifiable = false;
	}
}
