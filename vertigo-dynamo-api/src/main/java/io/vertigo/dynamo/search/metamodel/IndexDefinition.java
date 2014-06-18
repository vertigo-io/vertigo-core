package io.vertigo.dynamo.search.metamodel;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * Définition de l'index de recherche.
 * 
 * Un index est constitué de deux types d'objets.
 * - Un objet d'index (les champs indexés)
 * - Un objet d'affichage 
 * 
 * L'objet d'affichage peut être simple (Ex: résultat google) alors qu'il se réfère à un index plus riche.
 *  
 * @author dchallas
 */
@Prefix("IDX")
public final class IndexDefinition implements Definition {
	/**
	* Nom de l'index.
	*/
	private final String name;

	/** Structure des éléments indexés. */
	private final DtDefinition indexDtDefinition;

	/** Structure des éléments de résultat.*/
	private final DtDefinition resultDtDefinition;

	/**
	 * Constructeur.
	 * @param indexDtDefinition Structure des éléments indexés.
	 * @param resultDtDefinition Structure des éléments de résultat.
	 */
	public IndexDefinition(final String name, final DtDefinition indexDtDefinition, final DtDefinition resultDtDefinition) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(indexDtDefinition);
		Assertion.checkNotNull(resultDtDefinition);
		//---------------------------------------------------------------------
		this.name = name;
		this.indexDtDefinition = indexDtDefinition;
		this.resultDtDefinition = resultDtDefinition;
	}

	/**
	 * Définition des champs indexés.
	 * @return Définition des champs indexés.
	 */
	public DtDefinition getIndexDtDefinition() {
		return indexDtDefinition;
	}

	/**
	 * Définition des éléments résultats.
	 * Les éléments de résultats doivent être conservés, stockés dans l'index.
	 * @return Définition des éléments de résultats.
	 */
	public DtDefinition getResultDtDefinition() {
		return resultDtDefinition;
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
