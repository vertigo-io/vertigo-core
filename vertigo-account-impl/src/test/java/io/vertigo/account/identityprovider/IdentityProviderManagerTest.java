/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.account.identityprovider.model.User;
import io.vertigo.persona.security.VSecurityManager;

/**
 * Implementation standard de la gestion centralisee des droits d'acces.
 *
 * @author npiedeloup
 */
public final class IdentityProviderManagerTest extends AbstractTestCaseJU4 {

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

	@Ignore
	@Test
	public void testUsersCount() {
		Assert.assertEquals(1, identityProviderManager.getUsersCount());
	}

	@Test
	public void testAllUsers() {
		Assert.assertEquals(1, identityProviderManager.getAllUsers().size());
	}

	@Ignore
	@Test
	public void testPhoto() {
		final List<User> users = identityProviderManager.getAllUsers();
		//Before the photo is the default photo
		Assert.assertFalse(identityProviderManager.getPhoto(users.get(0).getURI()).isPresent());
		Assert.assertEquals("defaultPhoto.png", identityProviderManager.getPhoto(users.get(0).getURI()).get().getFileName());
	}

	@Test
	public void testNoPhoto() {
		final List<User> users = identityProviderManager.getAllUsers();
		//Before the photo is the default photo
		Assert.assertFalse(identityProviderManager.getPhoto(users.get(0).getURI()).isPresent());
	}

	@Test
	public void testUserByAuthToken() {
		final User user = identityProviderManager.getUserByAuthToken("admin");
		Assert.assertNotNull("Can't find user by login ", user);
	}

}
