package io.vertigo.core.node.component.data;

import javax.inject.Inject;

import io.vertigo.core.node.component.Manager;

public class SomeManager implements Manager {

	private final SomePlugin somePlugin;

	@Inject
	public SomeManager(final SomePlugin somePlugin) {
		this.somePlugin = somePlugin;
	}

	public String getSomeNames() {
		return somePlugin.getConnectionNames();
	}

}
