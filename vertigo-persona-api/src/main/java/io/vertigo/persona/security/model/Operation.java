package io.vertigo.persona.security.model;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * @author prahmoune
 * @version $Id: Operation.java,v 1.3 2013/10/22 12:35:39 pchretien Exp $ 
 */
@Prefix("OP_")
public final class Operation implements Definition {
	private final String name;
	private final String description;

	/**
	 * Constructeur.
	 * 
	 * @param name Nom de l'op�ration
	 * @param description Description de l'op�ration
	 */
	public Operation(final String name, final String description) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(description);
		// ---------------------------------------------------------------------
		this.name = name;
		this.description = description;
	}

	/**
	 * @return Description de l'op�ration
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Nom de l'Op�ration
	 */
	public String getName() {
		return name;
	}
}
