package io.vertigo.security.model;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

import java.util.List;

/**
 * Un rôle est la réunion d'un ensemble de permissions. 
 * Un utilisateur peut avoir  plusieurs rôles.
 * @author prahmoune
 */
@Prefix("R_")
public final class Role implements Definition {
	private final String name;
	private final String description;
	private final List<Permission> permissions;

	/**
	 * Constructeur.
	 * 
	 * @param name Nom du rôle
	 * @param description Description du rôle
	 * @param permissions Liste des permissions associées au rôle
	 */
	public Role(final String name, final String description, final List<Permission> permissions) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkArgNotEmpty(description);
		Assertion.checkNotNull(permissions);
		// ---------------------------------------------------------------------
		this.name = name;
		this.description = description;
		this.permissions = permissions;
	}

	/**
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Liste des permissions
	 */
	public List<Permission> getPermissions() {
		return permissions;
	}

	/**
	 * @return Nom du rôle
	 */
	public String getName() {
		return name;
	}
}
