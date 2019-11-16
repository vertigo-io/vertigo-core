package io.vertigo.core.node.component.data;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.core.node.component.Connector;

public class SomeMultiConnectorPlugin implements SomePlugin {

	private final List<SomeConnector> allConnectors;

	@Inject
	public SomeMultiConnectorPlugin(final List<SomeConnector> allConnectors) {
		this.allConnectors = allConnectors;
	}

	@Override
	public String getConnectionNames() {
		return allConnectors.stream()
				.map(Connector::getName)
				.collect(Collectors.joining(","));
	}

}
