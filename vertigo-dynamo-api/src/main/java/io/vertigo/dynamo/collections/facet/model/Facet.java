package io.vertigo.dynamo.collections.facet.model;

import io.vertigo.dynamo.collections.facet.metamodel.FacetDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionReference;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Facette. 
 * Valeur d'une définition de facette.
 * la facette est soit constituée
 * - de catégories (range) et dénombre alors le nombre d'éléments par Range  
 *  ex : prix de [0-10[ [10-100[ [100-*]  
 * - de terms distincts et dénombre alors le nombre d'éléments par term
 *  ex : marques de voiture renault, peugeot, ford 
 *  ex : villes ou départements    
 * @author pchretien, npiedeloup
 * @version $Id: Facet.java,v 1.3 2013/10/22 12:24:51 pchretien Exp $ 
 */
public final class Facet implements Serializable {
	private static final long serialVersionUID = -6496651592068817414L;

	private final DefinitionReference<FacetDefinition> facetDefinition;
	private final Map<FacetValue, Long> facetValues;

	/**
	 * Constructeur.
	 * @param facetDefinition Definition de la facette
	 * @param facetValues Liste des valeurs de facette
	 */
	public Facet(final FacetDefinition facetDefinition, final Map<FacetValue, Long> facetValues) {
		Assertion.checkNotNull(facetDefinition);
		Assertion.checkNotNull(facetValues);
		//---------------------------------------------------------------------
		this.facetDefinition = new DefinitionReference<>(facetDefinition);
		this.facetValues = Collections.unmodifiableMap(facetValues);
	}

	/**
	 * @return Définition de la facette.
	 */
	public FacetDefinition getDefinition() {
		return facetDefinition.get();
	}

	/**
	 * Valeurs des facettes. (Range ou Term)
	 * @return Map (range | term ; count) 
	 */
	public Map<FacetValue, Long> getFacetValues() {
		return facetValues;
	}
}
