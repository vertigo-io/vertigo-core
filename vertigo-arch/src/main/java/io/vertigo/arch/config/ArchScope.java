package io.vertigo.arch.config;

import java.util.List;

/**
 * Scope of the analysis: the root package pattern and packages to exclude (e.g. java.**, jakarta.**).
 * Excluded packages are implicitly allowed as dependencies for every module.
 */
public record ArchScope(
		String path,
		String description,
		List<String> excludes) {

	public ArchScope {
		if (path == null || path.isBlank()) {
			throw new IllegalArgumentException("scope.path is required");
		}
	}

	public List<String> excludes() {
		return excludes != null ? excludes : List.of();
	}
}
