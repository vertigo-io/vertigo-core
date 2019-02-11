/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.account;

import io.vertigo.account.account.AccountManager;
import io.vertigo.account.authentication.AuthenticationManager;
import io.vertigo.account.authorization.AuthorizationManager;
import io.vertigo.account.impl.account.AccountDefinitionProvider;
import io.vertigo.account.impl.account.AccountManagerImpl;
import io.vertigo.account.impl.authentication.AuthenticationManagerImpl;
import io.vertigo.account.impl.authorization.AuthorizationManagerImpl;
import io.vertigo.account.impl.security.VSecurityManagerImpl;
import io.vertigo.account.plugins.account.cache.memory.MemoryAccountCachePlugin;
import io.vertigo.account.plugins.account.cache.redis.RedisAccountCachePlugin;
import io.vertigo.account.plugins.account.store.datastore.StoreAccountStorePlugin;
import io.vertigo.account.plugins.account.store.loader.LoaderAccountStorePlugin;
import io.vertigo.account.plugins.account.store.text.TextAccountStorePlugin;
import io.vertigo.account.plugins.authentication.ldap.LdapAuthenticationPlugin;
import io.vertigo.account.plugins.authentication.mock.MockAuthenticationPlugin;
import io.vertigo.account.plugins.authentication.store.StoreAuthenticationPlugin;
import io.vertigo.account.plugins.authentication.text.TextAuthenticationPlugin;
import io.vertigo.account.plugins.identityprovider.ldap.LdapIdentityProviderPlugin;
import io.vertigo.account.plugins.identityprovider.store.StoreIdentityProviderPlugin;
import io.vertigo.account.plugins.identityprovider.text.TextIdentityProviderPlugin;
import io.vertigo.app.config.Feature;
import io.vertigo.app.config.Features;
import io.vertigo.core.param.Param;
import io.vertigo.persona.security.VSecurityManager;

/**
 * Defines the 'account' extension
 * @author pchretien
 */
public final class AccountFeatures extends Features<AccountFeatures> {
	private boolean withAccountManager = false;

	/**
	 * Constructor.
	 */
	public AccountFeatures() {
		super("account");
	}

	/**
	 * Activates user session.
	 * @param params the user session class name
	 * @return these features
	 */
	@Feature("security")
	public AccountFeatures withSecurity(final Param... params) {
		getModuleConfigBuilder()
				.addComponent(VSecurityManager.class, VSecurityManagerImpl.class, params);
		return this;
	}

	/**
	 * Activates authentication.
	 * @return these features
	 */
	@Feature("authentication")
	public AccountFeatures withAuthentication() {
		getModuleConfigBuilder()
				.addComponent(AuthenticationManager.class, AuthenticationManagerImpl.class);
		withAccountManager = true;
		return this;
	}

	/**
	 * Activates text authentication.
	 * @return these features
	 */
	@Feature("textAuthentication")
	public AccountFeatures withTextAuthentication(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(TextAuthenticationPlugin.class, params);
		withAccountManager = true;
		return this;
	}

	/**
	 * Activates store authentication.
	 * @return these features
	 */
	@Feature("storeAuthentication")
	public AccountFeatures withStoreAuthentication(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(StoreAuthenticationPlugin.class, params);
		withAccountManager = true;
		return this;
	}

	/**
	 * Activates ldap authentication.
	 * @return these features
	 */
	@Feature("ldapAuthentication")
	public AccountFeatures withLdapAuthentication(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(LdapAuthenticationPlugin.class, params);
		withAccountManager = true;
		return this;
	}

	/**
	 * Activates mock authentication.
	 * @return these features
	 */
	@Feature("mockAuthentication")
	public AccountFeatures withMockAuthentication(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(MockAuthenticationPlugin.class, params);
		withAccountManager = true;
		return this;
	}

	/**
	 * Activates text authentication.
	 * @return these features
	 */
	@Feature("storeAccount")
	public AccountFeatures withStoreAccount(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(StoreAccountStorePlugin.class, params);
		withAccountManager = true;
		return this;
	}

	/**
	 * Activates text authentication.
	 * @return these features
	 */
	@Feature("textAccount")
	public AccountFeatures withTextAccount(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(TextAccountStorePlugin.class, params);
		withAccountManager = true;
		return this;
	}

	@Feature("loaderAccount")
	public AccountFeatures withLoaderAccount(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(LoaderAccountStorePlugin.class, params);
		return this;
	}

	@Feature("memoryAccountCache")
	public AccountFeatures withMemoryAccountCache(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(MemoryAccountCachePlugin.class, params);
		return this;
	}

	@Feature("redisAccountCache")
	public AccountFeatures withRedisAccountCache(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(RedisAccountCachePlugin.class, params);
		return this;
	}

	@Feature("storeIdentity")
	public AccountFeatures withStoreIdentity(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(StoreIdentityProviderPlugin.class, params);
		withAccountManager = true;
		return this;
	}

	@Feature("ldapIdentity")
	public AccountFeatures withLdapIdentity(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(LdapIdentityProviderPlugin.class, params);
		return this;
	}

	@Feature("textIdentity")
	public AccountFeatures withTextIdentity(final Param... params) {
		getModuleConfigBuilder()
				.addPlugin(TextIdentityProviderPlugin.class, params);
		withAccountManager = true;
		return this;
	}

	/**
	 * Activates authorization.
	 * @return these features
	 */
	@Feature("authorization")
	public AccountFeatures withAuthorization() {
		getModuleConfigBuilder()
				.addComponent(AuthorizationManager.class, AuthorizationManagerImpl.class);
		withAccountManager = true;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		if (withAccountManager) {
			getModuleConfigBuilder()
					.addComponent(AccountManager.class, AccountManagerImpl.class)
					.addDefinitionProvider(AccountDefinitionProvider.class);
		}
	}

}
