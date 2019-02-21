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

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

import io.vertigo.account.account.DatabaseAccountManagerTest;
import io.vertigo.account.account.MemoryAccountManagerTest;
import io.vertigo.account.account.RedisAccountManagerTest;
import io.vertigo.account.authentication.TextAuthenticationManagerTest;
import io.vertigo.account.authentication.RedisCacheAuthenticationManagerTest;
import io.vertigo.account.authorization.VSecurityManagerTest;
import io.vertigo.account.authorization.dsl.DslSecurityRulesBuilderTest;
import io.vertigo.account.identityprovider.IdentityProviderManagerTest;

/**
 * Test de l'implementation standard.
 *
 * @author pchretien
 */
@RunWith(JUnitPlatform.class)
@SelectClasses({
		RedisAccountManagerTest.class,
		DatabaseAccountManagerTest.class,
		MemoryAccountManagerTest.class,
		RedisCacheAuthenticationManagerTest.class,
		TextAuthenticationManagerTest.class,
		VSecurityManagerTest.class,
		IdentityProviderManagerTest.class,
		DslSecurityRulesBuilderTest.class
})

public final class AccountTestSuite {
	//
}
