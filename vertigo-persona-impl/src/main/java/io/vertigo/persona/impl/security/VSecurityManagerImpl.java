/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.persona.impl.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.app.Home;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.core.locale.LocaleProvider;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.ResourceNameFactory;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.VSecurityManager;
import io.vertigo.persona.security.metamodel.Permission;
import io.vertigo.persona.security.metamodel.Role;
import io.vertigo.util.ClassUtil;

/**
 * Implementation standard de la gestion centralisee des droits d'acces.
 *
 * @author npiedeloup
 */
public final class VSecurityManagerImpl implements VSecurityManager, Activeable {
	/**
	 * Thread local portant la session utilisteur.
	 * Utilisateur courant > peut etre null.
	 */
	private static final ThreadLocal<UserSession> USER_SESSION_THREAD_LOCAL = new ThreadLocal<>();

	private final LocaleManager localeManager;
	private final String userSessionClassName;
	private final Map<String, ResourceNameFactory> resourceNameFactories = new HashMap<>();

	/**
	 * Constructeur.
	 * Les deux namespace ne sont pas types pour eviter des couplages forts (notamment sur UI).
	 * @param localeManager Manager des messages localises
	 * @param userSessionClassName ClassName de l'objet de session utilisateur
	 */
	@Inject
	public VSecurityManagerImpl(final LocaleManager localeManager, @Named("userSessionClassName") final String userSessionClassName) {
		Assertion.checkNotNull(localeManager);
		Assertion.checkArgNotEmpty(userSessionClassName);
		//-----
		this.localeManager = localeManager;
		this.userSessionClassName = userSessionClassName;
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		localeManager.registerLocaleProvider(createLocaleProvider());
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		//
	}

	/** {@inheritDoc} */
	@Override
	public <U extends UserSession> U createUserSession() {
		return (U) ClassUtil.newInstance(userSessionClassName);
	}

	private LocaleProvider createLocaleProvider() {
		return () -> {
			final Optional<UserSession> userSession = getCurrentUserSession();
			return userSession.isPresent() ? userSession.get().getLocale() : null;
		};
	}

	/** {@inheritDoc} */
	@Override
	public void startCurrentUserSession(final UserSession user) {
		Assertion.checkNotNull(user);
		//On verifie que la UserSession précédante a bien été retiree (securite et memoire).
		if (USER_SESSION_THREAD_LOCAL.get() != null) {
			throw new IllegalStateException("UserSession already created in this thread, check to close session by stopCurrentUserSession in a finally");
		}
		//-----
		USER_SESSION_THREAD_LOCAL.set(user);
	}

	/** {@inheritDoc} */
	@Override
	public void stopCurrentUserSession() {
		USER_SESSION_THREAD_LOCAL.remove();
	}

	/** {@inheritDoc} */
	@Override
	public <U extends UserSession> Optional<U> getCurrentUserSession() {
		final U userSession = (U) USER_SESSION_THREAD_LOCAL.get();
		return Optional.ofNullable(userSession);
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasRole(final UserSession userSession, final Set<Role> authorizedRoleSet) {
		Assertion.checkNotNull(userSession);
		Assertion.checkNotNull(authorizedRoleSet);
		//-----
		if (authorizedRoleSet.isEmpty()) {
			return true;
		}
		// Si il existe au moins un role parmi la liste des roles autorises
		// il faut alors regarder si l'utilisateur possede un role de la liste.
		final Set<Role> userProfiles = userSession.getRoles();
		for (final Role role : authorizedRoleSet) {
			Assertion.checkArgument(Home.getApp().getDefinitionSpace().containsDefinition(role), "Le role {0} n est pas defini dans RoleRegistry.", role);
			if (userProfiles.contains(role)) {
				return true;
			}
		}
		// Si on a trouve aucun des roles autorises alors l'acces est interdit
		return false;

	}

	/** {@inheritDoc} */
	@Override
	public boolean isAuthorized(final String resource, final String operation) {
		// Note: il s'agit d'une implementation naïve non optimisee,
		// réalisée pour valider le modèle
		final Optional<UserSession> userSessionOption = getCurrentUserSession();

		if (!userSessionOption.isPresent()) {
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
		final String filter = permission.getFilter();
		final String personalFilter = applySecurityKeys(filter, securityKeys);
		final Pattern pFilter = Pattern.compile(personalFilter);
		final Pattern pOperation = Pattern.compile(permission.getOperation());
		return pFilter.matcher(resource).matches() && pOperation.matcher(operation).matches();
	}

	private static String applySecurityKeys(final String filter, final Map<String, String> securityKeys) {
		final StringBuilder personalFilter = new StringBuilder();
		int previousIndex = 0;
		int nextIndex = filter.indexOf("${", previousIndex);
		while (nextIndex >= 0) {
			personalFilter.append(filter.substring(previousIndex, nextIndex));
			final int endIndex = filter.indexOf('}', nextIndex + "${".length());
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
	@Override
	public boolean isAuthorized(final String resourceType, final Object resource, final String operation) {
		final ResourceNameFactory resourceNameFactory = resourceNameFactories.get(resourceType);
		Assertion.checkNotNull(resourceNameFactory, "Ce type de resource : {0}, ne possède pas de ResourceNameFactory.", resourceType);
		final String resourceName = resourceNameFactory.toResourceName(resource);
		return isAuthorized(resourceName, operation);
	}

	/** {@inheritDoc} */
	@Override
	public void registerResourceNameFactory(final String resourceType, final ResourceNameFactory resourceNameFactory) {
		Assertion.checkArgNotEmpty(resourceType);
		Assertion.checkNotNull(resourceNameFactory);
		//-----
		resourceNameFactories.put(resourceType, resourceNameFactory);
	}
}
