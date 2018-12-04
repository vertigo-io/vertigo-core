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
package io.vertigo.account.identityprovider;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU5;
import io.vertigo.account.identityprovider.model.User;
import io.vertigo.persona.security.VSecurityManager;

/**
 * Implementation standard de la gestion centralisee des droits d'acces.
 *
 * @author npiedeloup
 */
public final class IdentityProviderManagerTest extends AbstractTestCaseJU5 {

	@Inject
	private VSecurityManager securityManager;

	@Inject
	private IdentityProviderManager identityProviderManager;

	@Override
	public void doSetUp() {
		securityManager.startCurrentUserSession(securityManager.createUserSession());
	}

	@Override
	public void doTearDown() {
		securityManager.stopCurrentUserSession();
	}

	@Disabled
	@Test
	public void testUsersCount() {
		Assertions.assertEquals(1, identityProviderManager.getUsersCount());
	}

	@Test
	public void testAllUsers() {
		Assertions.assertEquals(1, identityProviderManager.getAllUsers().size());
	}

	@Disabled
	@Test
	public void testPhoto() {
		final List<User> users = identityProviderManager.getAllUsers();
		//Before the photo is the default photo
		Assertions.assertFalse(identityProviderManager.getPhoto(users.get(0).getUID()).isPresent());
		Assertions.assertEquals("defaultPhoto.png", identityProviderManager.getPhoto(users.get(0).getUID()).get().getFileName());
	}

	@Test
	public void testNoPhoto() {
		final List<User> users = identityProviderManager.getAllUsers();
		//Before the photo is the default photo
		Assertions.assertFalse(identityProviderManager.getPhoto(users.get(0).getUID()).isPresent());
	}

	@Test
	public void testUserByAuthToken() {
		final User user = identityProviderManager.getUserByAuthToken("admin");
		Assertions.assertNotNull(user, "Can't find user by login ");
	}

}
