/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.core.locale.LocaleManager;
import io.vertigo.core.locale.LocaleProvider;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.ResourceNameFactory;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.VSecurityManager;
import io.vertigo.persona.security.dsl.model.DslMultiExpression;
import io.vertigo.persona.security.metamodel.OperationName;
import io.vertigo.persona.security.metamodel.Permission;
import io.vertigo.persona.security.metamodel.PermissionName;
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
	 * Constructor.
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
	public boolean hasPermission(final PermissionName permissionName) {
		Assertion.checkNotNull(permissionName);
		final Optional<UserSession> userSessionOption = getCurrentUserSession();
		if (!userSessionOption.isPresent()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return false;
		}
		final UserSession userSession = userSessionOption.get();
		return userSession.hasPermission(permissionName);

	}

	/** {@inheritDoc} */
	@Override
	public <K extends KeyConcept> boolean isAuthorized(final K keyConcept, final OperationName<K> operationName) {
		Assertion.checkNotNull(keyConcept);
		Assertion.checkNotNull(operationName);
		final Optional<UserSession> userSessionOption = getCurrentUserSession();
		if (!userSessionOption.isPresent()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return false;
		}
		final UserSession userSession = userSessionOption.get();

		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConcept);
		return userSession.getEntityPermissions(dtDefinition).stream()
				.filter(permission -> permission.getOperation().get().equals(operationName.name()))
				.flatMap(permission -> permission.getRules().stream())
				.anyMatch(rule -> new CriteriaSecurityRuleTranslator()
						.on(dtDefinition)
						.withRule(rule)
						.withCriteria(userSession.getSecurityKeys())
						.toCriteria()
						.toPredicate().test(keyConcept));
	}

	@Override
	public <K extends KeyConcept> String getSearchSecurity(final K keyConcept, final OperationName<K> operationName) {
		Assertion.checkNotNull(keyConcept);
		Assertion.checkNotNull(operationName);
		final Optional<UserSession> userSessionOption = getCurrentUserSession();
		if (!userSessionOption.isPresent()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return ""; //Attention : pas de *:*
		}
		final UserSession userSession = userSessionOption.get();
		final SearchSecurityRuleTranslator securityRuleTranslator = new SearchSecurityRuleTranslator();
		securityRuleTranslator.withCriteria(userSession.getSecurityKeys());

		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConcept);
		final List<Permission> permissions = userSession.getEntityPermissions(dtDefinition).stream()
				.filter(permission -> permission.getOperation().get().equals(operationName.name()))
				.collect(Collectors.toList());
		for (final Permission permission : permissions) {
			for (final DslMultiExpression ruleExpression : permission.getRules()) {
				securityRuleTranslator.withRule(ruleExpression);
			}
		}
		return securityRuleTranslator.toSearchQuery();
	}

	/** {@inheritDoc} */
	@Override
	public <K extends KeyConcept> List<String> getAuthorizedOperations(final K keyConcept) {
		Assertion.checkNotNull(keyConcept);
		final Optional<UserSession> userSessionOption = getCurrentUserSession();
		if (!userSessionOption.isPresent()) {
			// Si il n'y a pas de session alors pas d'autorisation.
			return new ArrayList<>();
		}
		final UserSession userSession = userSessionOption.get();
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConcept);
		return userSession.getEntityPermissions(dtDefinition).stream()
				.map(permission -> permission.getOperation().get())
				.collect(Collectors.toList());
	}

}
