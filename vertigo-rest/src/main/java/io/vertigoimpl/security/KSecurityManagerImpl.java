package io.vertigoimpl.security;

import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.commons.locale.LocaleProvider;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.util.ClassUtil;
import io.vertigo.security.KSecurityManager;
import io.vertigo.security.ResourceNameFactory;
import io.vertigo.security.UserSession;
import io.vertigo.security.model.Permission;
import io.vertigo.security.model.Role;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation standard de la gestion centralisée des droits d'accès.
 * @author npiedeloup
 */
public final class KSecurityManagerImpl implements KSecurityManager, Activeable {
	/**
	 * Thread local portant la session utilisteur.
	 * Utilisateur courant > peut être null.
	 */
	private static final ThreadLocal<UserSession> USER_SESSION_THREAD_LOCAL = new ThreadLocal<>();

	//private final Map<NameSpace<?>, String> nameSpaceNameMap = new HashMap<NameSpace<?>, String>();
	private final LocaleManager localeManager;
	private final String userSessionClassName;
	private final Map<String, ResourceNameFactory> resourceNameFactories = new HashMap<>();

	/** 
	 * Constructeur.
	 * Les deux namespace ne sont pas typés pour éviter des couplages forts (notamment sur UI).
	 * @param localeManager Manager des messages localisés
	 * @param userSessionClassName ClassName de l'objet de session utilisateur
	 */
	@Inject
	public KSecurityManagerImpl(final LocaleManager localeManager, @Named("userSessionClassName") final String userSessionClassName) {
		Assertion.checkNotNull(localeManager);
		Assertion.checkArgNotEmpty(userSessionClassName);
		//---------------------------------------------------
		this.localeManager = localeManager;
		this.userSessionClassName = userSessionClassName;
	}

	/** {@inheritDoc} */
	public void start() {
		localeManager.registerLocaleProvider(createLocaleProvider());
	}

	/** {@inheritDoc} */
	public void stop() {
		//
	}

	/** {@inheritDoc} */
	public <U extends UserSession> U createUserSession() {
		return (U) ClassUtil.newInstance(userSessionClassName);
	}

	private LocaleProvider createLocaleProvider() {
		return new LocaleProvider() {
			public Locale getCurrentLocale() {
				final Option<UserSession> userSession = getCurrentUserSession();
				return userSession.isDefined() ? userSession.get().getLocale() : null;
			}
		};
	}

	/** {@inheritDoc} */
	public void startCurrentUserSession(final UserSession user) {
		Assertion.checkNotNull(user);
		//On vérifie que la UserSession précédante a bien été retirée (sécurité et memoire).
		if (USER_SESSION_THREAD_LOCAL.get() != null) {
			throw new IllegalStateException("UserSession déjà créée, vérifier l'utilisation du stopCurrentUserSession dans un block finally ");
		}
		//---------------------------------------------------------------------
		USER_SESSION_THREAD_LOCAL.set(user);
	}

	/** {@inheritDoc} */
	public void stopCurrentUserSession() {
		USER_SESSION_THREAD_LOCAL.remove();
	}

	/** {@inheritDoc} */
	public <U extends UserSession> Option<U> getCurrentUserSession() {
		final U userSession = (U) USER_SESSION_THREAD_LOCAL.get();
		return Option.option(userSession);
	}

	/** {@inheritDoc} */
	public boolean hasRole(final UserSession userSession, final Set<Role> authorizedRoleSet) {
		Assertion.checkNotNull(userSession);
		Assertion.checkNotNull(authorizedRoleSet);
		// ----------------------------------------------------------------------
		if (authorizedRoleSet.isEmpty()) {
			return true;
		}
		// Si il existe au moins un role parmi la liste des roles autorisés
		// il faut alors regarder si l'utilisateur possède un role de la liste.
		final Set<Role> userProfiles = userSession.getRoles();
		for (final Role role : authorizedRoleSet) {
			Assertion.checkArgument(Home.getDefinitionSpace().containsValue(role), "Le rôle {0} n est pas défini dans RoleRegistry.", role);
			if (userProfiles.contains(role)) {
				return true;
			}
		}
		// Si on a trouvé aucun des roles autorisés alors l'accès est interdit
		return false;

	}

	/** {@inheritDoc} */
	public boolean isAuthorized(final String resource, final String operation) {
		// Note: il s'agit d'une implémentation naïve non optimisée,
		// réalisée pour valider le modèle
		final Option<UserSession> userSessionOption = getCurrentUserSession();

		if (userSessionOption.isEmpty()) {
			//Si il n'y a pas de session alors pas d'autorisation.
			return false;
		}
		final UserSession userSession = userSessionOption.get();
		final Map<String, String> securityKeys = userSessionOption.get().getSecurityKeys();
		for (final Role role : userSession.getRoles()) {
			if (isAuthorized(role, resource, operation, securityKeys)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isAuthorized(final Role role, final String resource, final String operation, final Map<String, String> securityKeys) {
		for (final Permission permission : role.getPermissions()) {
			if (isAuthorized(permission, resource, operation, securityKeys)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isAuthorized(final Permission permission, final String resource, final String operation, final Map<String, String> securityKeys) {
		final String filter = permission.getResource().getFilter();
		final String personalFilter = applySecurityKeys(filter, securityKeys);
		final Pattern p = Pattern.compile(personalFilter);
		return p.matcher(resource).matches() && permission.getOperation().getName().matches(operation);
	}

	private static String applySecurityKeys(final String filter, final Map<String, String> securityKeys) {
		final StringBuilder personalFilter = new StringBuilder();
		int previousIndex = 0;
		int nextIndex = filter.indexOf("${", previousIndex);
		while (nextIndex >= 0) {
			personalFilter.append(filter.substring(previousIndex, nextIndex));
			final int endIndex = filter.indexOf("}", nextIndex + "${".length());
			Assertion.checkState(endIndex >= nextIndex, "missing \\} : {0} à {1}", filter, nextIndex);
			final String key = filter.substring(nextIndex + "${".length(), endIndex);
			final String securityValue = securityKeys.get(key); //peut etre null, ce qui donnera /null/
			personalFilter.append(securityValue);
			previousIndex = endIndex + "}".length();
			nextIndex = filter.indexOf("${", previousIndex);
		}
		if (previousIndex < filter.length()) {
			personalFilter.append(filter.substring(previousIndex, filter.length()));
		}
		return personalFilter.toString();
	}

	/** {@inheritDoc} */
	public boolean isAuthorized(final String resourceType, final Object resource, final String operation) {
		final ResourceNameFactory resourceNameFactory = resourceNameFactories.get(resourceType);
		Assertion.checkNotNull(resourceNameFactory, "Ce type de resource : {0}, ne possède pas de ResourceNameFactory.", resourceType);
		final String resourceName = resourceNameFactory.toResourceName(resource);
		return isAuthorized(resourceName, operation);
	}

	/** {@inheritDoc} */
	public void registerResourceNameFactory(final String resourceType, final ResourceNameFactory resourceNameFactory) {
		Assertion.checkArgNotEmpty(resourceType);
		Assertion.checkNotNull(resourceNameFactory);
		// ----------------------------------------------------------------------
		resourceNameFactories.put(resourceType, resourceNameFactory);
	}
}
