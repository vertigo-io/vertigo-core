package io.vertigo.arch.config;

import java.util.Map;

import io.vertigo.core.lang.Assertion;

public record ArchConfig(
		ArchScope scope,
		Map<String, String> libs,
		Map<String, ArchModule> modules) {

	public ArchConfig {
		Assertion.check()
				.isNotNull(scope, "scope is required")
				.isNotNull(modules, "modules are required")
				.isFalse(modules.isEmpty(), "at least one module must be declared");
	}
}
