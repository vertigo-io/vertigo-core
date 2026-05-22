package io.vertigo.arch.config;

import java.util.List;

import io.vertigo.core.lang.Assertion;

public record ArchModuleDef(
		String path,
		String description,
		List<String> deps) {

	public ArchModuleDef {
		Assertion.check().isNotBlank(path, "module.path is required");
	}

	public List<String> deps() {
		return deps != null ? deps : List.of();
	}
}
