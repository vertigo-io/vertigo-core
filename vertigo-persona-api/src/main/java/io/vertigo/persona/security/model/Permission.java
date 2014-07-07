package io.vertigo.persona.security.model;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * Une permission est l'association d'une op�ration et d'une ressource.
 * 
 * @author prahmoune
 * @version $Id: Permission.java,v 1.3 2013/10/22 12:35:39 pchretien Exp $ 
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
	 * @param operation Op�ration
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
	 * @return Op�ration
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
