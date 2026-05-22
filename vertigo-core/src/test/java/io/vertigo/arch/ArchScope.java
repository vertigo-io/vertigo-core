package io.vertigo.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;

import io.vertigo.core.lang.Assertion;

/**
 * Fluent DSL for declaring a package scope and its module decomposition.
 *
 * <pre>{@code
 * ArchScope.in("io.vertigo.vivid.**")
 *     .description("Architecture globale Vivid")
 *     .exclude("java.**", "io.vertigo.core.**", "..stash..")
 *     .lib("jackson", "com.fasterxml.jackson.**")
 *     .module("schema").path("io.vertigo.vivid.schema.**").deps("jackson")
 *     .module("reader").path("io.vertigo.vivid.reader.**").deps("schema", "jackson")
 *     .module("mapper").path("io.vertigo.vivid.mapper.**").noDeps()
 *     .check(classes);
 * }</pre>
 *
 * <p>{@link #check} enforces three invariants:
 * <ol>
 *   <li>Coverage — every class in scope belongs to a declared module.</li>
 *   <li>Isolation — modules only depend on their declared deps.</li>
 *   <li>No orphan deps — every declared dep is actually used.</li>
 * </ol>
 */
public final class ArchScope {

	private final String path;
	private final List<String> excludes = new ArrayList<>();
	private final Map<String, String> libDefs = new LinkedHashMap<>();
	private final List<ModEntry> moduleEntries = new ArrayList<>();

	private record ModEntry(String name, String path, List<String> deps) {
	}

	private record Built(
			Map<String, ArchModule> modules,
			Map<String, ArchLib> libs,
			List<ArchRule> rules) {
	}

	private ArchScope(final String path) {
		Assertion.check().isNotBlank(path, "path is required");
		//---
		this.path = path;
	}

	// ─── Fluent API ───────────────────────────────────────────────────────────

	public static ArchScope in(final String path) {
		return new ArchScope(path);
	}

	/** Optional label — documents intent, has no effect on rules. */
	public ArchScope description(final String desc) {
		return this;
	}

	/** Packages excluded from the scope — also become implicit base libs. */
	public ArchScope exclude(final String... paths) {
		Assertion.check().isNotNull(paths, "paths are required");
		//---
		excludes.addAll(Arrays.asList(paths));
		return this;
	}

	/** Named external library that modules can declare as a dep. */
	public ArchScope lib(final String name, final String path) {
		Assertion.check()
				.isNotBlank(name, "lib name is required")
				.isNotBlank(path, "lib path is required");
		//---
		libDefs.put(name, path);
		return this;
	}

	/** Starts a module declaration — must be completed with {@link ArchModuleBuilder#deps} or {@link ArchModuleBuilder#noDeps}. */
	public ArchModuleBuilder module(final String name) {
		Assertion.check().isNotBlank(name, "module name is required");
		//---
		return new ArchModuleBuilder(name);
	}

	/** Fluent builder for a single module declaration. */
	public final class ArchModuleBuilder {
		private final String name;
		private String modulePath;

		private ArchModuleBuilder(final String name) {
			this.name = name;
		}

		public ArchModuleBuilder path(final String path) {
			Assertion.check().isNotBlank(path, "module path is required");
			//---
			this.modulePath = path;
			return this;
		}

		/** Declares deps and returns to the scope for the next declaration. */
		public ArchScope deps(final String... deps) {
			Assertion.check()
					.isNotNull(deps, "deps are required")
					.isNotBlank(modulePath, "path must be set before deps()");
			//---
			moduleEntries.add(new ModEntry(name, modulePath, Arrays.asList(deps)));
			return ArchScope.this;
		}

		/** Declares no deps and returns to the scope for the next declaration. */
		public ArchScope noDeps() {
			Assertion.check().isNotBlank(modulePath, "path must be set before noDeps()");
			//---
			moduleEntries.add(new ModEntry(name, modulePath, List.of()));
			return ArchScope.this;
		}
	}

	// ─── Rule execution ───────────────────────────────────────────────────────

	/**
	 * Runs all three invariants against the provided classes:
	 * coverage, isolation, and no orphan declared deps.
	 */
	public void check(final JavaClasses classes) {
		final Built built = build();

		for (final ArchRule rule : built.rules()) {
			rule.check(classes);
		}

		checkNoOrphanDeps(classes, built);
	}

	/** Returns ArchUnit rules for coverage and isolation (without orphan check). */
	public List<ArchRule> rules() {
		return build().rules();
	}

	// ─── Build ────────────────────────────────────────────────────────────────

	private Built build() {
		Assertion.check().isFalse(moduleEntries.isEmpty(), "at least one module must be declared");
		//---
		// base libs from excludes
		final List<ArchLib> baseLibList = excludes.stream()
				.map(p -> new ArchLib(shortName(p), archPattern(p)))
				.toList();
		ArchModule.setBaseLibs(baseLibList.toArray(ArchLib[]::new));

		// all libs (base + named)
		final Map<String, ArchLib> libs = new LinkedHashMap<>();
		baseLibList.forEach(lib -> libs.put(lib.name(), lib));
		libDefs.forEach((name, p) -> libs.put(name, new ArchLib(name, archPattern(p))));

		// flat modules
		final Map<String, ArchModule> modules = new LinkedHashMap<>();
		moduleEntries.forEach(e -> modules.put(e.name(), ArchModule.of(e.name(), archPattern(e.path()))));

		// dep rules
		final List<ArchRule> rules = new ArrayList<>();
		for (final ModEntry entry : moduleEntries) {
			final ArchDepBuilder db = modules.get(entry.name()).dependsOn();
			for (final String dep : entry.deps()) {
				if (modules.containsKey(dep)) {
					db.modules(modules.get(dep));
				} else if (libs.containsKey(dep)) {
					db.libs(libs.get(dep));
				} else {
					throw new IllegalStateException(
							"Unknown dep '" + dep + "' in module '" + entry.name() + "'"
									+ " — known modules: " + modules.keySet()
									+ ", libs: " + libs.keySet());
				}
			}
			rules.add(db);
		}

		// coverage rule
		final String[] excludePatterns = excludes.stream()
				.map(ArchScope::archPattern)
				.toArray(String[]::new);
		final String[] modulePatterns = modules.values().stream()
				.flatMap(m -> Arrays.stream(m.packagePatterns()))
				.toArray(String[]::new);

		rules.add(0, classes()
				.that().resideInAPackage(archPattern(path))
				.and().resideOutsideOfPackages(excludePatterns)
				.should().resideInAnyPackage(modulePatterns)
				.as("Every class in [" + path + "] must belong to a declared module"));

		return new Built(modules, libs, rules);
	}

	// ─── Orphan dep check ─────────────────────────────────────────────────────

	private void checkNoOrphanDeps(final JavaClasses classes, final Built built) {
		for (final ModEntry entry : moduleEntries) {
			final String[] fromPatterns = built.modules().get(entry.name()).packagePatterns();

			for (final String dep : entry.deps()) {
				final String[] toPatterns = resolvePatterns(dep, built);
				if (!isActuallyUsed(classes, fromPatterns, toPatterns)) {
					throw new AssertionError(
							"Module '" + entry.name() + "' declares dependency on '"
									+ dep + "' but never uses it — remove the orphan declaration");
				}
			}
		}
	}

	private static boolean isActuallyUsed(
			final JavaClasses classes,
			final String[] fromPatterns,
			final String[] toPatterns) {

		for (final JavaClass clazz : classes) {
			if (!matchesAny(clazz.getPackageName(), fromPatterns)) {
				continue;
			}
			for (final Dependency dep : clazz.getDirectDependenciesFromSelf()) {
				if (matchesAny(dep.getTargetClass().getPackageName(), toPatterns)) {
					return true;
				}
			}
		}
		return false;
	}

	private static String[] resolvePatterns(final String dep, final Built built) {
		if (built.modules().containsKey(dep)) {
			return built.modules().get(dep).packagePatterns();
		}
		if (built.libs().containsKey(dep)) {
			return built.libs().get(dep).packagePatterns();
		}
		throw new IllegalStateException("Unknown dep: " + dep);
	}

	// ─── Pattern matching ─────────────────────────────────────────────────────

	private static boolean matchesAny(final String packageName, final String[] patterns) {
		for (final String pattern : patterns) {
			if (matchesPattern(packageName, pattern)) {
				return true;
			}
		}
		return false;
	}

	/** Matches ArchUnit-style patterns: "pkg.." = prefix, "pkg" = exact. */
	private static boolean matchesPattern(final String packageName, final String pattern) {
		if (pattern.endsWith("..")) {
			final String prefix = pattern.substring(0, pattern.length() - 2);
			return packageName.equals(prefix) || packageName.startsWith(prefix + ".");
		}
		return packageName.equals(pattern);
	}

	// ─── Path helpers ─────────────────────────────────────────────────────────

	private static String archPattern(final String path) {
		if (path.endsWith(".**")) {
			return path.substring(0, path.length() - 3) + "..";
		}
		return path;
	}

	private static String shortName(final String path) {
		final String stripped = path.replace("..", "");
		final int dot = stripped.indexOf('.');
		return dot < 0 ? stripped : stripped.substring(0, dot);
	}
}
