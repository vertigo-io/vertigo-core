/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.store.cache;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.store.AbstractStoreManagerTest;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.dynamock.domain.famille.Famille;

/**
 * Test de l'implémentation avec cache.
 *
 * @author pchretien
 */
public final class CachedStoreManagerTest extends AbstractStoreManagerTest {

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		storeManager.getDataStoreConfig().registerCacheable(DtObjectUtil.findDtDefinition(Famille.class), 120, true, true);
	}

	/**
	 * On charge une liste, ajoute un element et recharge la liste pour verifier l'ajout.
	 */
	@Override
	@Test
	public void testAddFamille() {
		//ce test est modifier car le cache n'est pas transactionnel : la liste n'est pas accessible sans commit
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtListURI allFamilles = new DtListURIForCriteria<>(dtDefinitionFamille, null, null);
			final DtList<Famille> dtc = storeManager.getDataStore().findAll(allFamilles);
			Assert.assertEquals(0, dtc.size());
			//-----
			final Famille famille = new Famille();
			famille.setLibelle("encore un");
			storeManager.getDataStore().create(famille);
			// on attend un objet avec un ID non null ?
			Assert.assertNotNull(famille.getFamId());
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtListURI allFamilles = new DtListURIForCriteria<>(dtDefinitionFamille, null, null);
			final DtList<Famille> dtc = storeManager.getDataStore().findAll(allFamilles);
			Assert.assertEquals(1, dtc.size());

		}
	}
}
