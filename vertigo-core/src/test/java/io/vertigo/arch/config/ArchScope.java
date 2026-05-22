package io.vertigo.arch.config;

import java.util.List;

import io.vertigo.core.lang.Assertion;

public record ArchScope(
		String path,
		String description,
		List<String> excludes) {

	public ArchScope {
		Assertion.check().isNotBlank(path, "scope.path is required");
	}

	public List<String> excludes() {
		return excludes != null ? excludes : List.of();
	}
}
