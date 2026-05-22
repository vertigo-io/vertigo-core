package io.vertigo.arch.config;

import java.util.List;

/**
 * Definition of a module: its package path and the names of its allowed dependencies
 * (other module names or lib names declared in {@link ArchConfig#libs()}).
 */
public record ArchModule(
		String path,
		String description,
		List<String> deps) {

	public ArchModule {
		if (path == null || path.isBlank()) {
			throw new IllegalArgumentException("module.path is required");
		}
	}

	public List<String> deps() {
		return deps != null ? deps : List.of();
	}
}
