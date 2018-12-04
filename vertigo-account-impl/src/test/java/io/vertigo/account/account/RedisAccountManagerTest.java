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
package io.vertigo.account.account;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.app.config.AppConfig;
import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.commons.transaction.VTransactionWritable;
import redis.clients.jedis.Jedis;

public final class RedisAccountManagerTest extends AbstractAccountManagerTest {

	@Inject
	private AccountManager accountManager;
	@Inject
	private RedisConnector redisConnector;

	@Override
	protected AppConfig buildAppConfig() {
		return MyAppConfig.config(true, false);
	}

	@BeforeEach
	public void clean() {
		try (final Jedis jedis = redisConnector.getResource()) {
			jedis.flushAll();
		}

	}

	@Test
	public void testPhoto() {
		//Before the photo is the default photo
		Assertions.assertFalse(accountManager.getPhoto(accountURI0).isPresent());
		Assertions.assertEquals("defaultPhoto.png", accountManager.getDefaultPhoto().getFileName());
		//-----
		//	final VFile photo = fileManager.createFile(new File(this.getClass().getResource("../data/marianne.png").toURI()));
		//	identityManager.setPhoto(accountURI0, photo);
		//-----
		Assertions.assertTrue(accountManager.getPhoto(accountURI1).isPresent());
		Assertions.assertEquals("marianne.png", accountManager.getPhoto(accountURI1).get().getFileName());
	}

	@Test
	public void testGroups() {
		try (VTransactionWritable tx = obtainTx()) {
			//Assertions.assertEquals(2, identityManager.getGroupsCount());
			//----
			Assertions.assertEquals(1, accountManager.getGroupURIs(accountURI0).size());
			Assertions.assertEquals(2, accountManager.getGroupURIs(accountURI1).size());
			Assertions.assertEquals(2, accountManager.getGroupURIs(accountURI2).size());
			Assertions.assertEquals(2, accountManager.getAccountURIs(groupURI).size());
			Assertions.assertEquals(10 + 4, accountManager.getAccountURIs(groupAllURI).size());
			//---
			/*identityManager.attach(accountURI0, groupURI);
			Assertions.assertEquals(2, identityManager.getGroupURIs(accountURI0).size());
			Assertions.assertEquals(2, identityManager.getGroupURIs(accountURI1).size());
			Assertions.assertEquals(2, identityManager.getGroupURIs(accountURI2).size());
			Assertions.assertEquals(3, identityManager.getAccountURIs(groupURI).size());
			Assertions.assertEquals(10 + 3, identityManager.getAccountURIs(groupAllURI).size());*/
		}
	}

}
