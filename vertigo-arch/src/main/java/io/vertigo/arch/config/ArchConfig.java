package io.vertigo.arch.config;

import java.util.Map;
import java.util.Objects;

/**
 * Root configuration: scope, optional named libs, and module definitions.
 * Loaded from YAML by {@link ArchYamlConfigLoader}.
 */
public record ArchConfig(
		ArchScope scope,
		Map<String, String> libs,
		Map<String, ArchModuleDef> modules) {

	public ArchConfig {
		Objects.requireNonNull(scope, "scope is required");
		Objects.requireNonNull(modules, "modules are required");
		//---
		if (modules.isEmpty()) {
			throw new IllegalArgumentException("at least one module must be declared");
		}
	}

	/** Starts a fluent builder scoped to the given package pattern (e.g. {@code "io.vertigo.acme.**"}). */
	public static ArchConfigBuilder builder(final String path) {
		return new ArchConfigBuilder(path);
	}
}
