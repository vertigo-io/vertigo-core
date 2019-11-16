package io.vertigo.core.node.component.data;

import javax.inject.Inject;

public class SomeMonoConnectorPlugin implements SomePlugin {

	private final SomeConnector oneConnector;

	@Inject
	public SomeMonoConnectorPlugin(final SomeConnector oneConnector) {
		this.oneConnector = oneConnector;
	}

	@Override
	public String getConnectionNames() {
		return oneConnector.getName();
	}
}
