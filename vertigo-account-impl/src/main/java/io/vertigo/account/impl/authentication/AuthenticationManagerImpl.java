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
package io.vertigo.account.impl.authentication;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import io.vertigo.account.authentication.AuthenticationToken;
import io.vertigo.account.authentication.AuthenticationManager;
import io.vertigo.account.identity.Account;
import io.vertigo.account.identity.IdentityManager;
import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.VSecurityManager;

/**
 * Implementation standard de la gestion centralisee des droits d'acces.
 *
 * @author npiedeloup
 */
public final class AuthenticationManagerImpl implements AuthenticationManager {
	private static final String USER_SESSION_ACCOUNT_KEY = "vertigo.account.authc";

	private final List<AuthenticatingRealmPlugin> authenticatingRealmPlugins;
	private final IdentityManager identityManager;
	private final VSecurityManager securityManager;

	/**
	 * Constructor.
	 * @param identityManager Identity Manager
	 * @param securityManager Security Manager
	 * @param authenticatingRealmPlugins List of authenticatingRealmPlugins
	 */
	@Inject
	public AuthenticationManagerImpl(final IdentityManager identityManager, final VSecurityManager securityManager, final List<AuthenticatingRealmPlugin> authenticatingRealmPlugins) {
		Assertion.checkNotNull(identityManager);
		Assertion.checkNotNull(securityManager);
		Assertion.checkNotNull(authenticatingRealmPlugins);
		//----
		this.identityManager = identityManager;
		this.securityManager = securityManager;
		this.authenticatingRealmPlugins = authenticatingRealmPlugins;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> authenticate(final AuthenticationToken token) {
		final Optional<Account> account = tryAuthenticateAccount(token);
		if (account.isPresent()) {
			final Optional<UserSession> userSession = securityManager.getCurrentUserSession();
			if (userSession.isPresent()) {
				userSession.get().authenticate();
				userSession.get().putAttribute(USER_SESSION_ACCOUNT_KEY, account.get());
			}
		}
		return account;
	}

	/** {@inheritDoc} */
	@Override
	public Optional<Account> getLoggedAccount() {
		final Optional<UserSession> userSession = securityManager.getCurrentUserSession();
		if (userSession.isPresent()) {
			return Optional.ofNullable(userSession.get().getAttribute(USER_SESSION_ACCOUNT_KEY));
		}
		return Optional.empty();
	}

	/** {@inheritDoc} */
	@Override
	public void logout() {
		final Optional<UserSession> userSession = securityManager.getCurrentUserSession();
		if (userSession.isPresent()) {
			userSession.get().logout();
		}
	}

	private Optional<Account> tryAuthenticateAccount(final AuthenticationToken token) {
		boolean tokenSupported = false;
		for (final AuthenticatingRealmPlugin authenticatingRealmPlugin : authenticatingRealmPlugins) {
			if (authenticatingRealmPlugin.supports(token)) {
				tokenSupported = true;
				final Optional<String> accountAuthToken = authenticatingRealmPlugin.authenticateAccount(token);
				if (accountAuthToken.isPresent()) {
					return identityManager.getStore().getAccountByAuthToken(accountAuthToken.get());
				}
			}
		}
		Assertion.checkState(tokenSupported, "Can't found any realm to support this token ({0}), in realms ({1})", token.getClass().getSimpleName(), authenticatingRealmPlugins);
		return Optional.empty();
	}
}
