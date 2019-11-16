package io.vertigo.core.node.component.data;

import javax.inject.Inject;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Connector;
import io.vertigo.core.param.ParamValue;

public class SomeConnector implements Connector {

	private final String name;

	@Inject
	public SomeConnector(@ParamValue("name") final String name) {
		Assertion.checkArgNotEmpty(name);
		//----
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
