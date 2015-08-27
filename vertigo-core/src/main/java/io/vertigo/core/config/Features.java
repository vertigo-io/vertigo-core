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
		Assertion.checkState(this.moduleConfigBuilder == null, "appConfigBuilder is alreay defined");
		//---
		moduleConfigBuilder = appConfigBuilder.beginModule(name);
		setUp();
	}

	protected abstract void setUp();

	public ModuleConfigBuilder getModuleConfigBuilder() {
		return moduleConfigBuilder;
	}

	public final AppConfigBuilder endModule() {
		return moduleConfigBuilder.endModule();
	}

}
