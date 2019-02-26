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
package io.vertigo.account.authentication;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.account.account.Account;
import io.vertigo.account.authentication.MyAppConfig.AuthentPlugin;
import io.vertigo.account.impl.authentication.UsernameAuthenticationToken;
import io.vertigo.account.security.UserSession;
import io.vertigo.app.config.AppConfig;

/**
 * Implementation standard de la gestion centralisee des droits d'acces.
 *
 * @author npiedeloup
 */
public final class RedisCacheMockAuthenticationManagerTest extends AbstractAuthenticationManagerTest {
	@Override
	protected AppConfig buildAppConfig() {
		return MyAppConfig.config(AuthentPlugin.mock, true);
	}

	@Test
	public Optional<Account> loginMockSuccess() {
		final AuthenticationToken token = new UsernameAuthenticationToken("admin");
		final Optional<Account> account = authenticationManager.login(token);
		Assertions.assertTrue(account.isPresent(), "Authent fail");

		final Optional<UserSession> userSession = securityManager.getCurrentUserSession();
		Assertions.assertTrue(userSession.isPresent(), "No UserSession");
		Assertions.assertTrue(userSession.get().isAuthenticated(), "Not authenticated");
		return account;
	}

}
