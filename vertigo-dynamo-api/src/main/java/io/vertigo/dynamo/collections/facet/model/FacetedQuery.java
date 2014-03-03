package io.vertigo.dynamo.collections.facet.model;

import io.vertigo.dynamo.collections.ListFilter;
import io.vertigo.dynamo.collections.facet.metamodel.FacetedQueryDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionReference;

import java.io.Serializable;
import java.util.List;

/**
 * Requete de filtrage par facettes.
 * @author npiedeloup
 * @version $Id: FacetedQuery.java,v 1.3 2013/10/22 12:24:51 pchretien Exp $ 
 */
public final class FacetedQuery implements Serializable {
	private static final long serialVersionUID = -3215786603726103410L;

	private final DefinitionReference<FacetedQueryDefinition> facetedQueryDefinition;
	private final List<ListFilter> listFilters;

	/**
	 * Constructeur.
	 * @param facetedQueryDefinition Definition de la requête
	 * @param listFilters Liste de filtres supplémentaires
	 */
	public FacetedQuery(final FacetedQueryDefinition facetedQueryDefinition, final List<ListFilter> listFilters) {
		Assertion.checkNotNull(facetedQueryDefinition);
		Assertion.checkNotNull(listFilters);
		//---------------------------------------------------------------------
		this.facetedQueryDefinition = new DefinitionReference<>(facetedQueryDefinition);
		this.listFilters = listFilters;
	}

	/**
	 * @return Définition du FacetedQuery.
	 */
	public FacetedQueryDefinition getDefinition() {
		return facetedQueryDefinition.get();
	}

	/**
	 * Liste de filtres supplémentaires.
	 * @return Liste des filtres.
	 */
	public List<ListFilter> getListFilters() {
		return listFilters;
	}
}
