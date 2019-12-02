package io.vertigo.core.node.component.data;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.core.node.component.Component;

public class SomeManagerWithSomeTypeOfConnector implements Component {

	private final List<SomeTypeOfConnector> someConnectors;

	@Inject
	public SomeManagerWithSomeTypeOfConnector(final List<SomeTypeOfConnector> someConnectors) {
		this.someConnectors = someConnectors;
	}

	public String sayHello() {
		return someConnectors.stream()
				.map(SomeTypeOfConnector::sayHello)
				.collect(Collectors.joining(";"));

	}

}
