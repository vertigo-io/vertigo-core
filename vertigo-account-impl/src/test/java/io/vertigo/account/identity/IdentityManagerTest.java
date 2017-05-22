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
package io.vertigo.account.identity;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.vertigo.account.MyAppConfig;
import io.vertigo.account.data.Identities;
import io.vertigo.account.identity.Account;
import io.vertigo.account.identity.AccountGroup;
import io.vertigo.account.identity.IdentityManager;
import io.vertigo.app.AutoCloseableApp;
import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.core.component.di.injector.DIInjector;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.persona.security.UserSession;
import io.vertigo.persona.security.VSecurityManager;
import redis.clients.jedis.Jedis;

@RunWith(Parameterized.class)
public final class IdentityManagerTest {
	private AutoCloseableApp app;

	@Inject
	private IdentityManager identityManager;

	@Inject
	private FileManager fileManager;

	@Inject
	private VSecurityManager securityManager;

	private URI<Account> accountURI0;
	private URI<Account> accountURI1;
	private URI<Account> accountURI2;
	private URI<AccountGroup> groupURI;
	private URI<AccountGroup> groupAllURI;

	@Parameters
	public static Collection<Object[]> params() {
		return Arrays.asList(
				//redis
				new Object[] { true },
				//memory (redis= false)
				new Object[] { false });
	}

	final boolean redis;

	/**
	 * Constructor
	 * @param redis use redis or memory
	 */
	public IdentityManagerTest(final boolean redis) {
		//params are automatically injected
		this.redis = redis;
	}

	@Before
	public void setUp() {
		app = new AutoCloseableApp(MyAppConfig.config(redis));

		DIInjector.injectMembers(this, app.getComponentSpace());
		if (redis) {
			final RedisConnector redisConnector = app.getComponentSpace().resolve(RedisConnector.class);
			try (final Jedis jedis = redisConnector.getResource()) {
				jedis.flushAll();
			}
		}

		accountURI0 = Identities.createAccountURI("0");
		accountURI1 = Identities.createAccountURI("1");
		accountURI2 = Identities.createAccountURI("2");
		groupURI = Identities.createGroupURI("100");
		groupAllURI = Identities.createGroupURI("ALL");

		Identities.initData(identityManager);
	}

	@After
	public void tearDown() {
		if (app != null) {
			app.close();
		}
	}

	@Test
	public void testLogin() {
		securityManager.startCurrentUserSession(new UserSession() {
			private static final long serialVersionUID = 1L;

			@Override
			public Locale getLocale() {
				return null;
			}
		});
		//identityManager.login(accountURI1);
		//Assert.assertEquals(accountURI1, identityManager.getLoggedAccount());
		securityManager.stopCurrentUserSession();
	}

	@Test
	public void testAccounts() {
		Assert.assertEquals("Palmer Luckey", identityManager.getStore().getAccount(accountURI1).getDisplayName());
		Assert.assertEquals(10 + 3, identityManager.getStore().getAccountsCount());
	}

	@Test
	public void testPhoto() throws URISyntaxException {
		//Before the photo is the default photo
		Assert.assertFalse(identityManager.getStore().getPhoto(accountURI0).isPresent());
		Assert.assertEquals("defaultPhoto.png", identityManager.getDefaultPhoto().getFileName());
		//-----
		final VFile photo = fileManager.createFile(new File(this.getClass().getResource("../data/marianne.png").toURI()));
		identityManager.getStore().setPhoto(accountURI0, photo);
		//-----
		Assert.assertTrue(identityManager.getStore().getPhoto(accountURI0).isPresent());
		Assert.assertEquals("marianne.png", identityManager.getStore().getPhoto(accountURI0).get().getFileName());
	}

	@Test
	public void testGroups() {
		Assert.assertEquals(2, identityManager.getStore().getGroupsCount());
		//----
		Assert.assertEquals(1, identityManager.getStore().getGroupURIs(accountURI0).size());
		Assert.assertEquals(2, identityManager.getStore().getGroupURIs(accountURI1).size());
		Assert.assertEquals(2, identityManager.getStore().getGroupURIs(accountURI2).size());
		Assert.assertEquals(2, identityManager.getStore().getAccountURIs(groupURI).size());
		Assert.assertEquals(10 + 3, identityManager.getStore().getAccountURIs(groupAllURI).size());
		//---
		identityManager.getStore().attach(accountURI0, groupURI);
		Assert.assertEquals(2, identityManager.getStore().getGroupURIs(accountURI0).size());
		Assert.assertEquals(2, identityManager.getStore().getGroupURIs(accountURI1).size());
		Assert.assertEquals(2, identityManager.getStore().getGroupURIs(accountURI2).size());
		Assert.assertEquals(3, identityManager.getStore().getAccountURIs(groupURI).size());
		Assert.assertEquals(10 + 3, identityManager.getStore().getAccountURIs(groupAllURI).size());
	}

}
