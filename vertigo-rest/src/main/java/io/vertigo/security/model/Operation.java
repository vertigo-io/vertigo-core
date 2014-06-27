package io.vertigo.security.model;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * Type d'opération sécurisée.
 * @author prahmoune
 */
@Prefix("OP_")
public final class Operation implements Definition {
	private final String name;
	private final String description;

	/**
	 * Constructeur.
	 * 
	 * @param name Nom de l'opération
	 * @param description Description de l'opération
	 */
	public Operation(final String name, final String description) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(description);
		// ---------------------------------------------------------------------
		this.name = name;
		this.description = description;
	}

	/**
	 * @return Description de l'opération
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Nom de l'Opération
	 */
	public String getName() {
		return name;
	}
}
