package io.vertigo.app.config.discovery;

import io.vertigo.app.config.discovery.ModuleDiscoveryFeatures;

public class TestModuleDiscoveryFeatures extends ModuleDiscoveryFeatures {

	public TestModuleDiscoveryFeatures() {
		super("test");
	}

	@Override
	protected String getPackageRoot() {
		return this.getClass().getPackage().getName();
	}

}
