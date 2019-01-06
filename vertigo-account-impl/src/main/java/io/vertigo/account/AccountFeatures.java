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

import javax.inject.Named;

import io.vertigo.account.account.AccountManager;
import io.vertigo.account.authentication.AuthenticationManager;
import io.vertigo.account.authorization.AuthorizationManager;
import io.vertigo.account.impl.account.AccountDefinitionProvider;
import io.vertigo.account.impl.account.AccountManagerImpl;
import io.vertigo.account.impl.authentication.AuthenticationManagerImpl;
import io.vertigo.account.impl.authorization.AuthorizationManagerImpl;
import io.vertigo.account.impl.security.VSecurityManagerImpl;
import io.vertigo.app.config.Features;
import io.vertigo.app.config.json.Feature;
import io.vertigo.core.param.Param;
import io.vertigo.persona.security.VSecurityManager;

/**
 * Defines the 'account' extension
 * @author pchretien
 */
public final class AccountFeatures extends Features<AccountFeatures> {

	/**
	 * Constructor.
	 */
	public AccountFeatures() {
		super("account");
	}

	/**
	 * Activates user session.
	 * @param userSessionClass the user session class name
	 * @return these features
	 */
	@Feature("security")
	public AccountFeatures withSecurity(final @Named("userSessionClassName") String userSessionClass) {
		getModuleConfigBuilder()
				.addComponent(VSecurityManager.class, VSecurityManagerImpl.class,
						Param.of("userSessionClassName", userSessionClass));
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
		return this;
	}

	/** {@inheritDoc} */
	@Override
	protected void buildFeatures() {
		getModuleConfigBuilder()
				.addComponent(AccountManager.class, AccountManagerImpl.class)
				.addDefinitionProvider(AccountDefinitionProvider.class);
	}

}
