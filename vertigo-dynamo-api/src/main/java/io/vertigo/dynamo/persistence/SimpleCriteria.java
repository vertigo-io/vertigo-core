package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;

/**
 * Critère de recherche simple.
 *
 * @author npiedeloup
 * @param <D> Type de l'objet
 */
public final class SimpleCriteria<D extends DtObject> implements Criteria<D> {
	private static final long serialVersionUID = -1279372740797454047L;
	private final String search;

	/**
	 * Constructeur.
	 * @param search recherche simple
	 */
	public SimpleCriteria(final String search) {
		Assertion.checkNotNull(search);
		//---------------------------------------------------------------------
		this.search = search;
	}

	/**
	* Recherche simple.
	* @return Recherche saisie par l'utilisateur.
	*/
	public String getSearch() {
		return search;
	}
}
