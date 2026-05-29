package io.vertigo.arch.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.tngtech.archunit.core.domain.JavaClasses;

import io.vertigo.arch.ArchChecker;

/**
 * Fluent builder for {@link ArchConfig}.
 * Entry point: {@code ArchScope.in("io.vertigo.acme.**")}.
 *
 * <pre>{@code
 * ArchScope.in("io.vertigo.acme.**")
 *     .exclude("java.**", "io.vertigo.core.**")
 *     .lib("gson", "com.google.gson.**")
 *     .module("api").path("io.vertigo.acme.api.**")
 *     .module("impl").path("io.vertigo.acme.impl.**").deps("api", "gson")
 *     .check(classes);
 * }</pre>
 */
public final class ArchConfigBuilder {
	private final String scopePath;
	private String scopeDescription;
	private final List<String> excludes = new ArrayList<>();
	private final Map<String, String> libs = new LinkedHashMap<>();
	private final Map<String, ArchModule> modules = new LinkedHashMap<>();

	// directives
	private boolean noCycle;
	private boolean noDirectImplDep;

	// pending module being assembled
	private String pendingModuleName;
	private String pendingModulePath;
	private String pendingModuleDescription;
	private List<String> pendingModuleDeps;

	ArchConfigBuilder(final String scopePath) {
		Objects.requireNonNull(scopePath, "scope path is required");
		//---
		this.scopePath = scopePath;
	}

	/** Sets the human-readable description of the scope, or of the current module if called after module(). */
	public ArchConfigBuilder description(final String description) {
		if (pendingModuleName != null) {
			pendingModuleDescription = description;
		} else {
			//on est encore dans le scope
			scopeDescription = description;
		}
		return this;
	}

	/** Adds one or more package patterns that are globally allowed for all modules. */
	public ArchConfigBuilder exclude(final String... patterns) {
		excludes.addAll(Arrays.asList(patterns));
		return this;
	}

	/** Declares a named external library that modules can opt into via deps(). */
	public ArchConfigBuilder lib(final String name, final String pattern) {
		Objects.requireNonNull(name, "lib name is required");
		Objects.requireNonNull(pattern, "lib pattern is required");
		//---
		libs.put(name, pattern);
		return this;
	}

	/** Starts defining a new module. Flushes any previously pending module. */
	public ArchConfigBuilder module(final String name) {
		Objects.requireNonNull(name, "module name is required");
		//---
		flushPendingModule();
		//---
		pendingModuleName = name;
		pendingModulePath = null;
		pendingModuleDescription = null;
		pendingModuleDeps = null;
		return this;
	}

	/** Sets the package path for the current module (optional — defaults to scope prefix + module name). */
	public ArchConfigBuilder path(final String path) {
		Objects.requireNonNull(path, "module path is required");
		if (pendingModuleName == null) {
			throw new IllegalStateException("call module() before path()");
		}
		pendingModulePath = path;
		return this;
	}

	/** Sets the allowed dependencies for the current module. */
	public ArchConfigBuilder deps(final String... deps) {
		if (pendingModuleName == null) {
			throw new IllegalStateException("call module() before deps()");
		}
		pendingModuleDeps = Arrays.asList(deps);
		return this;
	}

	/** Enables a directive by name ({@code "no-cycle"} or {@code "no-direct-impl-dep"}). */
	public ArchConfigBuilder directive(final String name) {
		Objects.requireNonNull(name, "directive name is required");
		//---
		switch (name) {
			case "no-cycle" -> noCycle = true;
			case "no-direct-impl-dep" -> noDirectImplDep = true;
			default -> throw new IllegalArgumentException("Unknown directive: '" + name
					+ "' — supported: no-cycle, no-direct-impl-dep");
		}
		return this;
	}

	/** Builds the {@link ArchConfig}. */
	public ArchConfig build() {
		flushPendingModule();
		final var scope = new ArchScope(scopePath, scopeDescription,
				excludes.isEmpty() ? null : List.copyOf(excludes));
		return new ArchConfig(scope,
				libs.isEmpty() ? null : Map.copyOf(libs),
				Map.copyOf(modules),
				new ArchDirectives(noCycle, noDirectImplDep));
	}

	/** Builds the config and runs all architecture checks against the given classes. */
	public void check(final JavaClasses classes) {
		ArchChecker.check(classes, build());
	}

	// ─── private helpers ────────────────────────────────────────────────────

	private void flushPendingModule() {
		if (pendingModuleName == null) {
			return;
		}
		final String resolvedPath = pendingModulePath != null
				? pendingModulePath
				: defaultPath(pendingModuleName);
		modules.put(pendingModuleName, new ArchModule(resolvedPath, pendingModuleDescription, pendingModuleDeps));
	}

	/** Derives a default module path from the scope path and the module name. */
	private String defaultPath(final String moduleName) {
		if (scopePath.endsWith(".**")) {
			return scopePath.substring(0, scopePath.length() - 2) + moduleName + ".**";
		}
		return scopePath + "." + moduleName + ".**";
	}
}
