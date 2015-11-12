/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.kvdatabase;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.kvdatabase.data.Flower;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.lang.Option;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 */
public abstract class AbstractKVStoreManagerTest extends AbstractTestCaseJU4 {
	protected static final String COLLECTION = "MyDB:flowers";
	@Inject
	protected KVDataBaseManager kvDataBaseManager;
	@Inject
	protected VTransactionManager transactionManager;

	//	protected static Tree buildTree(final String name, final double price) {
	//		return new Tree()
	//				.setName(name)
	//				.setPrice(price);
	//	}

	protected static Flower buildFlower(final String name, final double price) {
		return new Flower()
				.setName(name)
				.setPrice(price);
	}

	@Test
	public void testFind() {
		final KVStore kvStore = kvDataBaseManager.getKVStore("flowers");
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Option<Flower> foundFlower = kvStore.find("1", Flower.class);
			Assert.assertTrue(foundFlower.isEmpty());
			final Flower tulip = buildFlower("tulip", 100);

			kvStore.put("1", tulip);
			final Option<Flower> foundFlower2 = kvStore.find("1", Flower.class);
			Assert.assertTrue(foundFlower2.isDefined());
			Assert.assertEquals("tulip", foundFlower2.get().getName());
			Assert.assertEquals(100d, foundFlower2.get().getPrice(), 0); //"Price must be excatly 100",
		}
	}

	@Test
	public void testRemove() {
		final KVStore kvStore = kvDataBaseManager.getKVStore("flowers");

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Option<Flower> flower = kvStore.find("10", Flower.class);
			Assert.assertTrue("There is already a flower id 10", flower.isEmpty());

			kvStore.put("10", buildFlower("daisy", 60));
			kvStore.put("11", buildFlower("tulip", 100));

			final Option<Flower> flower1 = kvStore.find("10", Flower.class);
			final Option<Flower> flower2 = kvStore.find("11", Flower.class);
			Assert.assertTrue("Flower id 10 not found", flower1.isDefined());
			Assert.assertTrue("Flower id 11 not found", flower2.isDefined());

			transaction.commit();
		}

		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Option<Flower> flower = kvStore.find("10", Flower.class);
			Assert.assertTrue("Flower id 10 not found", flower.isDefined());

			kvStore.remove("10");

			final Option<Flower> flower1 = kvStore.find("10", Flower.class);
			final Option<Flower> flower2 = kvStore.find("11", Flower.class);
			Assert.assertTrue("Remove flower id 10 failed", flower1.isEmpty());
			Assert.assertTrue("Flower id 11 not found", flower2.isDefined());
		}
	}

}
