/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.commons.locale.LocaleProvider;
import io.vertigo.core.Home;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.persona.plugins.security.loaders.SecurityResourceLoaderPlugin;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.persona.security.ResourceNameFactory;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.metamodel.Permission;
import io.vertigo.persona.security.metamodel.Role;
import io.vertigo.util.ClassUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implementation standard de la gestion centralisee des droits d'acces.
 *
 * @author npiedeloup
 */
public final class KSecurityManagerImpl implements KSecurityManager, Activeable {
	/**
	 * Thread local portant la session utilisteur.
	 * Utilisateur courant > peut etre null.
	 */
	private static final ThreadLocal<UserSession> USER_SESSION_THREAD_LOCAL = new ThreadLocal<>();

	//private final Map<NameSpace<?>, String> nameSpaceNameMap = new HashMap<NameSpace<?>, String>();
	private final LocaleManager localeManager;
	private final String userSessionClassName;
	private final Map<String, ResourceNameFactory> resourceNameFactories = new HashMap<>();

	/**
	 * Constructeur.
	 * Les deux namespace ne sont pas types pour eviter des couplages forts (notamment sur UI).
	 * @param securityLoaderPlugin Plugin responsible for loading security model
	 * @param localeManager Manager des messages localises
	 * @param userSessionClassName ClassName de l'objet de session utilisateur
	 */
	@Inject
	public KSecurityManagerImpl(final SecurityResourceLoaderPlugin securityLoaderPlugin, final LocaleManager localeManager, @Named("userSessionClassName") final String userSessionClassName) {
		Assertion.checkNotNull(securityLoaderPlugin);
		Assertion.checkNotNull(localeManager);
		Assertion.checkArgNotEmpty(userSessionClassName);
		//-----
		this.localeManager = localeManager;
		this.userSessionClassName = userSessionClassName;
		//---
		Home.getDefinitionSpace().register(Role.class);
		Home.getDefinitionSpace().register(Permission.class);
		//---
		//We are populating xml loader or anything that can load security policy.
		Home.getDefinitionSpace().addLoader(securityLoaderPlugin);
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
		return new LocaleProvider() {
			@Override
			public Locale getCurrentLocale() {
				final Option<UserSession> userSession = getCurrentUserSession();
				return userSession.isDefined() ? userSession.get().getLocale() : null;
			}
		};
	}

	/** {@inheritDoc} */
	@Override
	public void startCurrentUserSession(final UserSession user) {
		Assertion.checkNotNull(user);
		//On verifie que la UserSession precedante a bien été retiree (securite et memoire).
		if (USER_SESSION_THREAD_LOCAL.get() != null) {
			throw new IllegalStateException("UserSession deje creee, verifier l'utilisation du stopCurrentUserSession dans un block finally ");
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
	public <U extends UserSession> Option<U> getCurrentUserSession() {
		final U userSession = (U) USER_SESSION_THREAD_LOCAL.get();
		return Option.option(userSession);
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
			Assertion.checkArgument(Home.getDefinitionSpace().containsValue(role), "Le rele {0} n est pas defini dans RoleRegistry.", role);
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
		final String filter = permission.getFilter();
		final String personalFilter = applySecurityKeys(filter, securityKeys);
		final Pattern p = Pattern.compile(personalFilter);
		return p.matcher(resource).matches() && permission.getOperation().matches(operation);
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
