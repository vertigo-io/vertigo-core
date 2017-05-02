package io.vertigo.persona.plugins.security.loaders;

import java.util.List;

import io.vertigo.persona.security.metamodel.Permission2;
import io.vertigo.persona.security.metamodel.SecuredEntity;

/**
 * Configuration de la sécurité avancée.
 *
 * @author jgarnier
 */
public class AdvancedSecurityConfiguration {

	private final List<Permission2> permissions;
	private final List<SecuredEntity> securedEntities;

	/**
	 * Construct an instance of AdvancedSecurityConfiguration.
	 *
	 * @param permissions Permissions attribuables aux utilisateurs.
	 * @param securedEntities Description des entités sécurisés.
	 */
	public AdvancedSecurityConfiguration(final List<Permission2> permissions,
			final List<SecuredEntity> securedEntities) {
		super();
		this.permissions = permissions;
		this.securedEntities = securedEntities;
	}

	/**
	 * Give the value of permissions.
	 *
	 * @return the value of permissions.
	 */
	public List<Permission2> getPermissions() {
		return permissions;
	}

	/**
	 * Give the value of securedEntities.
	 *
	 * @return the value of securedEntities.
	 */
	public List<SecuredEntity> getSecuredEntities() {
		return securedEntities;
	}
}
