package io.vertigo.core.node.component.data;

public class ConnectorB implements SomeTypeOfConnector {
	@Override
	public String getClient() {
		return "hello B";
	}
}
