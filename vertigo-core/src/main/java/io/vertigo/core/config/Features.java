package io.vertigo.core.config;

import io.vertigo.lang.Assertion;

/**
 * Defines a module by the features.
 * @author pchretien
 */
public abstract class Features {
	private final String name;
	private ModuleConfigBuilder moduleConfigBuilder;

	protected Features(final String name) {
		Assertion.checkArgNotEmpty(name);
		//-----
		this.name = name;
	}

	public final void init(final AppConfigBuilder appConfigBuilder) {
		Assertion.checkNotNull(appConfigBuilder);
		Assertion.checkState(moduleConfigBuilder == null, "appConfigBuilder is alreay defined");
		//---
		moduleConfigBuilder = appConfigBuilder.beginModule(name);
		setUp();
	}

	protected abstract void setUp();

	public ModuleConfigBuilder getModuleConfigBuilder() {
		return moduleConfigBuilder;
	}

	protected void buildFeatures() {
		//overrided if needed
	}

	public final AppConfigBuilder endModule() {
		buildFeatures();
		return moduleConfigBuilder.endModule();
	}

}
