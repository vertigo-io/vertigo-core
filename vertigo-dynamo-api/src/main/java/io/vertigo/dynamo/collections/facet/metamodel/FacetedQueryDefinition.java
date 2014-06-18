package io.vertigo.dynamo.collections.facet.metamodel;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Définition des requêtes d'accès à l'index de recherche.
 * 
 * les requêtes sont facettées.
 *
 * @author pchretien 
 */
@Prefix("QRY")
public final class FacetedQueryDefinition implements Definition {
	/**
	 * Nom de la définition.
	 */
	private final String name;

	/** Liste indexée des facettes.*/
	private final Map<String, FacetDefinition> facetDefinitions = new LinkedHashMap<>();

	/**
	 * Constructeur.
	 * @param facetDefinitions Liste des facettes
	 */
	public FacetedQueryDefinition(final String name, final List<FacetDefinition> facetDefinitions) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(facetDefinitions);
		//---------------------------------------------------------------------
		this.name = name;
		for (final FacetDefinition facetDefinition : facetDefinitions) {
			this.facetDefinitions.put(facetDefinition.getName(), facetDefinition);
		}
	}

	/**
	 * Retourne la facette identifié par son nom.
	 *
	 * @param facetName Nom de la facette recherché.
	 * @return Définition de la facette.
	 */
	public FacetDefinition getFacetDefinition(final String facetName) {
		Assertion.checkArgNotEmpty(facetName);
		//---------------------------------------------------------------------
		final FacetDefinition facetDefinition = facetDefinitions.get(facetName);
		//---------------------------------------------------------------------
		Assertion.checkNotNull(facetDefinition, "Aucune Définition de facette trouvée pour {0}", facetName);
		return facetDefinition;
	}

	/**
	 * @return Liste des facettes portées par l'index.
	 */
	public Collection<FacetDefinition> getFacetDefinitions() {
		return Collections.<FacetDefinition> unmodifiableCollection(facetDefinitions.values());
	}

	/** {@inheritDoc} */
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
