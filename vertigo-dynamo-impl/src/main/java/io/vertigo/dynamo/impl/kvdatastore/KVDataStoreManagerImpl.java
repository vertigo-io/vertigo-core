package io.vertigo.dynamo.impl.kvdatastore;

import io.vertigo.dynamo.kvdatastore.KVDataStore;
import io.vertigo.dynamo.kvdatastore.KVDataStoreManager;
import io.vertigo.kernel.lang.Assertion;

import javax.inject.Inject;

public class KVDataStoreManagerImpl implements KVDataStoreManager {
	private final KVDataStorePlugin kvDataStorePlugin;

	@Inject
	public KVDataStoreManagerImpl(KVDataStorePlugin kvDataStorePlugin) {
		Assertion.checkNotNull(kvDataStorePlugin);
		//---------------------------------------------------------------------
		this.kvDataStorePlugin = kvDataStorePlugin;
	}

	@Override
	public KVDataStore getDataStore() {
		return kvDataStorePlugin;
	}

}
