package io.vertigo.persona.security.model;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

import java.util.List;


/**
 * Un r�le est la r�union d'un ensemble de permissions. 
 * Un utilisateur peut avoir  plusieurs r�les.
 * 
 * @author prahmoune
 * @version $Id: Role.java,v 1.3 2013/10/22 12:35:39 pchretien Exp $ 
 */
@Prefix("R_")
public final class Role implements Definition {
	private final String name;
	private final String description;
	private final List<Permission> permissions;

	/**
	 * Constructeur.
	 * 
	 * @param name Nom du r�le
	 * @param description Description du r�le
	 * @param permissions Liste des permissions associ�es au r�le
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
	 * @return Nom du r�le
	 */
	public String getName() {
		return name;
	}
}
