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
 * @version $Id: FullTextFilterFunction.java,v 1.4 2014/01/20 17:46:01 pchretien Exp $
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
