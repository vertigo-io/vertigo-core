package io.vertigo.security;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionReference;
import io.vertigo.security.model.Role;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Session d'un utilisateur.
 * Un utilisateur
 * <ul>
 * <li>est authentifié ou non,</li>
 * <li>possède une liste de roles (préalablement enregistrés dans la RoleRegistry),</li>
 * <li>possède une liste d'attributs sérialisables</li>.
 * </ul>
 *
 * @author alauthier, pchretien
 */
public abstract class UserSession implements Serializable {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 467617818948129397L;

	/**
	 * Clé de la session (utilisé comme clé unique de connexion).
	 */
	private final UUID sessionUUID = createUUID();

	/**
	 * Set des roles autorisés pour la session utilisateur.
	 */
	private final Set<DefinitionReference<Role>> roles = new HashSet<>();

	/**
	 * Indique si l'utilisateur est authentifié.
	 * Par défaut l'utilisateur n'est pas authentifié.
	 */
	private boolean authenticated;

	private UUID createUUID() {
		//On utilise le mécanisme de création standard.
		return UUID.randomUUID();
	}

	//===========================================================================
	//=======================GESTION DES ROLES===================================
	//===========================================================================
	/**
	 * Ajoute un role pour l'utilisateur courant.
	 * Le role doit avoir été préalablement enregistré.
	 *
	 * @param role Role à ajouter.
	 */
	public final void addRole(final Role role) {
		Assertion.checkNotNull(role);
		// ----------------------------------------------------------------------
		roles.add(new DefinitionReference<>(role));
	}

	/**
	 * Retourne la liste des rôles de sécurité pour l'utilisateur.
	 *
	 * @return Set des rôles.
	 */
	public final Set<Role> getRoles() {
		final Set<Role> roleSet = new HashSet<>();
		for (final DefinitionReference<Role> roleReference : roles) {
			roleSet.add(roleReference.get());
		}
		return Collections.unmodifiableSet(roleSet);
	}

	/**
	 * @param role Role
	 * @return Vrai si le role est présent
	 */
	public final boolean hasRole(final Role role) {
		Assertion.checkNotNull(role);
		// ----------------------------------------------------------------------
		return roles.contains(new DefinitionReference<>(role));
	}

	/**
	 * Retrait de tous les roles possédés par l'utilisateur. 
	 * Attention, cela signifie qu'il n'a plus aucun droit.
	 */
	public final void clearRoles() {
		roles.clear();
	}

	//===========================================================================
	//=======================GESTION DES AUTHENTIFICATIONS=======================
	//===========================================================================

	/**
	 * @return UUID Indentifiant unique de cette connexion.
	 */
	public final UUID getSessionUUID() {
		return sessionUUID;
	}

	/**
	 * Indique si l'utilisateur est authentifié.
	 * L'authentification est actée par l'appel de la méthode <code>authenticate()</code>
	 *
	 * @return boolean Si l'utilisateur s'est authentifié.
	 */
	public final boolean isAuthenticated() {
		return authenticated;
	}

	/**
	 * Méthode permettant d'indiquer que l'utilisateur est authentifié.
	 */
	public final void authenticate() {
		authenticated = true;
	}

	/**
	 * Gestion multilingue.
	 * Local associée à l'utilisateur.
	 * @return Locale associée à l'utilisateur.
	 */
	public abstract Locale getLocale();

	/**
	 * Gestion de la sécurité.
	 * @return Liste des clés de sécurité et leur valeur.
	 */
	public Map<String, String> getSecurityKeys() {
		return Collections.emptyMap();
	}
}
