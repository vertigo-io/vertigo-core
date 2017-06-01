package io.vertigo.account.authorization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vertigo.account.authorization.metamodel.Permission;
import io.vertigo.account.authorization.metamodel.PermissionName;
import io.vertigo.account.authorization.metamodel.Role;
import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;

/**
 * Permissions d'un utilisateur.
 *
 * @author alauthier, pchretien, npiedeloup
 */
public final class UserPermissions implements Serializable {

	private static final long serialVersionUID = -7924146007592711123L;

	/**
	 * Set des permissions global autorisees pour la session utilisateur.
	 */
	private final Map<String, DefinitionReference<Permission>> permissionRefs = new HashMap<>();

	/**
	 * Set des permissions autorisees par entity pour la session utilisateur.
	 */
	private final Map<DefinitionReference<DtDefinition>, Set<DefinitionReference<Permission>>> permissionMapRefs = new HashMap<>();

	/**
	 * Set des roles autorises pour la session utilisateur.
	 * Pour compatibilité d'api.
	 */
	private final Set<DefinitionReference<Role>> roleRefs = new HashSet<>();

	//===========================================================================
	//=======================GESTION DES ROLES===================================
	//===========================================================================
	/**
	 * Ajoute un role pour l'utilisateur courant.
	 * Le role doit avoir ete prealablement enregistre.
	 *
	 * @param role Role e ajouter.
	 * @return this UserPermissions
	 */
	public final UserPermissions addRole(final Role role) {
		Assertion.checkNotNull(role);
		//-----
		roleRefs.add(new DefinitionReference<>(role));
		return this;
	}

	/**
	 * Retourne la liste des roles de securite pour l'utilisateur.
	 *
	 * @return Set des roles.
	 */
	public final Set<Role> getRoles() {
		final Set<Role> roleSet = new HashSet<>();
		for (final DefinitionReference<Role> roleReference : roleRefs) {
			roleSet.add(roleReference.get());
		}
		return Collections.unmodifiableSet(roleSet);
	}

	/**
	 * @param role Role
	 * @return Vrai si le role est present
	 */
	public final boolean hasRole(final Role role) {
		Assertion.checkNotNull(role);
		//-----
		return roleRefs.contains(new DefinitionReference<>(role));
	}

	/**
	 * Retrait de tous les roles possedes par l'utilisateur.
	 * Attention, cela signifie qu'il n'a plus aucun droit.
	 */
	public final void clearRoles() {
		roleRefs.clear();
	}

	/**
	 * Ajoute une permission pour l'utilisateur courant.
	 * La permission doit avoir ete prealablement enregistree.
	 *
	 * @param permission Permission à ajouter.
	 * @return This UserPermissions
	 */
	public final UserPermissions addPermission(final Permission permission) {
		Assertion.checkNotNull(permission);
		//-----
		permissionRefs.put(permission.getName(), new DefinitionReference<>(permission));
		if (permission.getEntityDefinition().isPresent()) {
			permissionMapRefs.computeIfAbsent(new DefinitionReference<>(permission.getEntityDefinition().get()), key -> new HashSet<>())
					.add(new DefinitionReference<>(permission));
			for (final Permission grantedPermission : permission.getGrants()) {
				if (!hasPermission(grantedPermission::getName)) { //On test pour ne pas créer de boucle
					addPermission(grantedPermission);
				}
			}
		}
		return this;
	}

	/**
	 * Retourne la liste des permissions de securite d'une entity pour l'utilisateur.
	 * @param entityDefinition Entity definition
	 * @return Set des permissions.
	 */
	public final Set<Permission> getEntityPermissions(final DtDefinition entityDefinition) {
		final Set<DefinitionReference<Permission>> entityPermissionRef = permissionMapRefs.get(new DefinitionReference<>(entityDefinition));
		final Set<Permission> permissionSet = new HashSet<>();
		if (entityPermissionRef != null) {
			for (final DefinitionReference<Permission> permissionReference : entityPermissionRef) {
				permissionSet.add(permissionReference.get());
			}
		}
		return Collections.unmodifiableSet(permissionSet);
	}

	/**
	 * @param permissionName Permission
	 * @return Vrai si la permission est presente
	 */
	public final boolean hasPermission(final PermissionName permissionName) {
		Assertion.checkNotNull(permissionName);
		//-----
		return permissionRefs.containsKey(permissionName.name());
	}

	/**
	 * Retrait de toutes les permissions possedes par l'utilisateur.
	 * Attention, cela signifie qu'il n'a plus aucun droit.
	 */
	public final void clearPermissions() {
		permissionRefs.clear();
		permissionMapRefs.clear();
	}

	private final Map<String, List<Serializable>> mySecurityKeys = new HashMap<>();

	/**
	 * Gestion de la sécurité.
	 * @return Liste des clés de sécurité et leur valeur.
	 */
	public Map<String, List<Serializable>> getSecurityKeys() {
		return mySecurityKeys;
	}

	/**
	 * Add a security key part of his security perimeter.
	 * A security key can be multi-valued (then withSecurityKeys is call multiple times).
	 * Value should be an array if this securityKey is a tree (hierarchical) key.
	 *
	 * @param securityKey Name
	 * @param value Value
	 * @return this UserPermissions
	 */
	public UserPermissions withSecurityKeys(final String securityKey, final Serializable value) {
		mySecurityKeys.computeIfAbsent(securityKey, v -> new ArrayList<>()).add(value);
		return this;
	}

	/**
	 * Clear Security Keys.
	 * Use when user change it security perimeter.
	 * @return this UserPermissions
	 */
	public UserPermissions clearSecurityKeys() {
		mySecurityKeys.clear();
		return this;
	}
}
