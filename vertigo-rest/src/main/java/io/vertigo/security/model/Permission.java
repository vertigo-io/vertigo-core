package io.vertigo.security.model;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * Une permission est l'association d'une opération et d'une ressource.
 * @author prahmoune
 */
@Prefix("PRM_")
public final class Permission implements Definition {
	private final String name;
	private final Operation operation;
	private final Resource resource;

	/**
	 * Constructeur.
	 * 
	 * @param name Nom de la permission
	 * @param operation Opération
	 * @param resource Ressource
	 */
	public Permission(final String name, final Operation operation, final Resource resource) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(operation);
		Assertion.checkNotNull(resource);
		// ---------------------------------------------------------------------
		this.name = name;
		this.operation = operation;
		this.resource = resource;
	}

	/**
	 * @return Ressource
	 */
	public Resource getResource() {
		return resource;
	}

	/**	 
	 * @return Opération
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * @return Nom de la permission
	 */
	public String getName() {
		return name;
	}

}
