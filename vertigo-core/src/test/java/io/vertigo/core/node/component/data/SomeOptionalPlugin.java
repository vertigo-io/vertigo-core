package io.vertigo.core.node.component.data;

import java.util.Optional;

import javax.inject.Inject;

public class SomeOptionalPlugin implements SomePlugin {

	private final Optional<SomeConnector> oneConnectorOpt;

	@Inject
	public SomeOptionalPlugin(final Optional<SomeConnector> oneConnectorOpt) {
		this.oneConnectorOpt = oneConnectorOpt;
	}

	@Override
	public String getConnectionNames() {
		return oneConnectorOpt.isPresent() ? oneConnectorOpt.get().getName() : "none";
	}

}
