package io.vertigo.persona.security.metamodel;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionPrefix;
import io.vertigo.lang.Assertion;

/**
 * Une permission est l'association d'une opération et d'une ressource.
 *
 * @author prahmoune
 */
@DefinitionPrefix("PRM_")
public final class Permission implements Definition {
	private final String name;
	private final String operation;
	private final String filter;

	/**
	 * Constructeur.
	 *
	 * @param name Nom de la permission
	 * @param operation Operation
	 */
	public Permission(final String name, final String operation, final String filter) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(operation);
		Assertion.checkArgNotEmpty(filter);
		//-----
		this.name = name;
		this.operation = operation;
		this.filter = filter;
	}

	/**
	 * @return Filter used to check permission
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * @return Operation
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * @return Nom de la permission
	 */
	@Override
	public String getName() {
		return name;
	}

}
