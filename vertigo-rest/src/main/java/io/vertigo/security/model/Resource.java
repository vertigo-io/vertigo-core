package io.vertigo.security.model;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * Représente une ressource. Une ressource peut être un élément de l'interface graphique,
 * une page, un service ou encore une donnée. 
 * Le schéma d'identification des ressources est spécifique à chaque projet.
 * 
 * Note : L'attribut 'filter' est une expression régulière permettant de 'collecter' une liste de
 * ressources.
 * @author prahmoune
 */
@Prefix("RSR_")
public final class Resource implements Definition {
	private final String name;
	private final String filter;
	private final String description;

	/**
	 * Constructeur.
	 * 
	 * @param name Nom de la ressource
	 * @param filter Filtre associé
	 * @param description Description de la ressource
	 */
	public Resource(final String name, final String filter, final String description) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkArgNotEmpty(filter);
		Assertion.checkArgNotEmpty(description);
		// ---------------------------------------------------------------------
		this.name = name;
		this.filter = filter;
		this.description = description;
	}

	/**
	 * @return Filtre
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Nom de la ressouce
	 */
	public String getName() {
		return name;
	}

}
