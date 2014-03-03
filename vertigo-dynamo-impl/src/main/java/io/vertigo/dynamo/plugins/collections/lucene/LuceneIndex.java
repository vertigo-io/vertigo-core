package io.vertigo.dynamo.plugins.collections.lucene;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtObject;

import java.io.IOException;
import java.io.Serializable;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

/**
 * Interface réprésentant un IndexLucene.
 * 
 * @author npiedeloup
 * @version $Id: LuceneIndex.java,v 1.2 2014/01/20 17:46:01 pchretien Exp $
 * @param <D> type d'objet indexé
 */
public interface LuceneIndex<D extends DtObject> extends Serializable {

	/**
	 * @return IndexWriter pour mettre à jour l'index. Doit-être close dans un finally.
	 * @throws IOException Exception I/O
	 */
	IndexWriter createIndexWriter() throws IOException;

	/**
	 * @return Searcher pour consulter l'index. Doit-être close dans un finally.
	 * @throws IOException Exception I/O
	 */
	IndexReader createIndexReader() throws IOException;

	//	/**
	//	 * Fermer la base lucene.
	//	 * @throws IOException Exception I/O
	//	 */
	//	void close() throws IOException;

	/**
	 * @param pkValue Clé de l'objet
	 * @return Objet associé dans cet index.
	 */
	D getDtObjectIndexed(String pkValue);

	/**
	 * @return DtDefinition des objets indexés
	 */
	DtDefinition getDtDefinition();
}
