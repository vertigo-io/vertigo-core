package io.vertigo.dynamo.kvdatastore;

import io.vertigo.kernel.lang.Option;

import java.util.List;

public interface KVDataStore {
	void put(String id, Object objet);

	<C> void delete(String id);

	<C> Option<C> find(String id, Class<C> clazz);

	<C> List<C> findAll(int skip, Integer limit, Class<C> clazz);
}
