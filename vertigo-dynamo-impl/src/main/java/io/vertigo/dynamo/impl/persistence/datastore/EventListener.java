package io.vertigo.dynamo.impl.persistence.datastore;

public interface EventListener {

	void onEvent(String event);

}
