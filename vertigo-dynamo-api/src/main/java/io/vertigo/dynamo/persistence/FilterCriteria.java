package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;

import java.util.Collections;
import java.util.Map;

/**
 * Critère de recherche par champs.
 * Les champs de l'objet recherché sont filtrés par la valeur associée au champ.
 * - Soit de type égalité.
 * - Soit de type préfixe (Commence par).
 *
 * @author npiedeloup
 * @version $Id: FilterCriteria.java,v 1.5 2013/11/15 17:13:52 npiedeloup Exp $
 * @param <D> Type de l'objet
 */
public final class FilterCriteria<D extends DtObject> implements Criteria<D> {
	private static final long serialVersionUID = -4980252957531667077L;
	private final Map<String, Object> mapFilter;
	private final Map<String, String> mapPrefix;

	/**
	 * Constructeur.
	 * @param mapFilter Liste des filtrages
	 * @param mapPrefix Liste des prefixes
	 */
	FilterCriteria(final Map<String, Object> mapFilter, final Map<String, String> mapPrefix) {
		Assertion.checkNotNull(mapFilter);
		Assertion.checkNotNull(mapPrefix);
		//---------------------------------------------------------------------
		this.mapFilter = Collections.unmodifiableMap(mapFilter);
		this.mapPrefix = Collections.unmodifiableMap(mapPrefix);
	}

	/**
	* Critère de recherche par champs.
	* @return Map des filtres existant.
	*/
	public Map<String, Object> getFilterMap() {
		return mapFilter;
	}

	/**
	* Critère de prefix par champs.
	* @return Map des prefixes existant.
	*/
	public Map<String, String> getPrefixMap() {
		return mapPrefix;
	}

	/**
	 * @return Si le groupe est vide
	 */
	public boolean isEmpty() {
		return mapFilter.isEmpty() && mapPrefix.isEmpty();
	}
}
