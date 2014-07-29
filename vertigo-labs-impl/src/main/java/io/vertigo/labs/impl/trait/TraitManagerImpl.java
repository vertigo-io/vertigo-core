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
package io.vertigo.labs.impl.trait;

import io.vertigo.dynamo.kvdatastore.KVDataStoreManager;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.labs.trait.Trait;
import io.vertigo.labs.trait.TraitManager;

import javax.inject.Inject;

public final class TraitManagerImpl implements TraitManager {
	private final String storeName;
	private final KVDataStoreManager kvDataStoreManager;
	private final KTransactionManager transactionManager;

	@Inject
	public TraitManagerImpl(final String storeName, final KVDataStoreManager kvDataStoreManager, final KTransactionManager transactionManager) {
		Assertion.checkArgNotEmpty(storeName);
		Assertion.checkNotNull(kvDataStoreManager);
		Assertion.checkNotNull(transactionManager);
		//---------------------------------------------------------------------s
		this.storeName = storeName;
		this.kvDataStoreManager = kvDataStoreManager;
		this.transactionManager = transactionManager;
	}

	/** {@inheritDoc} */
	public <T extends Trait> Option<T> findTrait(final Class<T> traitClass, final String subjectId) {
		Assertion.checkNotNull(traitClass);
		//---------------------------------------------------------------------
		return doFind(subjectId, traitClass.getSimpleName(), traitClass);
	}

	/** {@inheritDoc} */
	public <T extends Trait> void putTrait(final Class<T> traitClass, final String subjectId, final T trait) {
		Assertion.checkNotNull(traitClass);
		//---------------------------------------------------------------------
		doStore(subjectId, traitClass.getSimpleName(), trait);
	}

	/** {@inheritDoc} */
	public <T extends Trait> void deleteTrait(final Class<T> traitClass, final String subjectId) {
		doDelete(subjectId, traitClass.getSimpleName());
	}

	//=========================================================================
	//=========================================================================
	//=========================================================================

	private void doStore(final String subjectId, final String traitType, final Trait trait) {
		Assertion.checkNotNull(trait);
		Assertion.checkArgNotEmpty(traitType);
		//---------------------------------------------------------------------
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			kvDataStoreManager.put(storeName, traitType + ":" + subjectId, trait);
			transaction.commit();
		}
	}

	private <C extends Trait> Option<C> doFind(final String subjectId, final String traitType, final Class<C> clazz) {
		Assertion.checkNotNull(subjectId);
		Assertion.checkArgNotEmpty(traitType);
		//---------------------------------------------------------------------
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			return kvDataStoreManager.find(storeName,traitType + ":" + subjectId, clazz);
		}
	}

	private void doDelete(final String subjectId, final String traitType) {
		Assertion.checkNotNull(subjectId);
		Assertion.checkArgNotEmpty(traitType);
		//---------------------------------------------------------------------
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			kvDataStoreManager.remove(storeName,traitType + ":" + subjectId);
			transaction.commit();
		}
	}

}
