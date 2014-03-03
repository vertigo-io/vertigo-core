package io.vertigo.dynamo.kvdatastore;

import io.vertigo.kernel.component.Manager;

public interface KVDataStoreManager extends Manager {
	KVDataStore getDataStore();
}
