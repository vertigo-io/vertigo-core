package io.vertigo.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tngtech.archunit.lang.ArchRule;

import io.vertigo.arch.config.ArchConfig;
import io.vertigo.arch.config.ArchYamlConfigLoader;
import io.vertigo.core.lang.Assertion;

/**
 * Loads module dependency rules from a YAML file.
 *
 * <h3>YAML format</h3>
 * 
 * <pre>
 * scope:
 *   path: io.vertigo.vivid.**
 *   description: "..."
 *   excludes: [java.**, io.vertigo.core.**]
 *
 * libs:
 *   jackson: com.fasterxml.jackson.**
 *
 * modules:
 *   reader:
 *     path: io.vertigo.vivid.reader.**
 *     deps: [schema, jackson]   # modules and libs in a single list
 * </pre>
 *
 * <h3>Rules generated per file</h3>
 * <ul>
 * <li>Coverage: every class in scope must belong to a declared module.</li>
 * <li>Deps: each module may only depend on its declared deps + base libs.</li>
 * </ul>
 *
 * <p>
 * Alternative: use {@link ArchScope} for the equivalent Java DSL.
 */
public final class ArchModuleLoader {
	// ─── Records ──────────────────────────────────────────────────────────────

	private ArchModuleLoader() {
	}

	// ─── Public API ───────────────────────────────────────────────────────────

	/** Loads and concatenates rules from all given YAML file names. */
	public static List<ArchRule> load(final File... files) {
		Assertion.check().isNotNull(files, "files are required");
		//---
		final List<ArchRule> all = new ArrayList<>();
		for (final File file : files) {
			all.addAll(loadOne(file));
		}
		return all;
	}

	// ─── Per-file loading ─────────────────────────────────────────────────────

	private static List<ArchRule> loadOne(final File file) {
		Assertion.check()
				.isNotNull(file, "file is required")
				.isTrue(file.exists(), "YAML file not found: {0}", file.getAbsolutePath());
		//---
		final ArchConfig archConfig = ArchYamlConfigLoader.parseYaml(file);
		// ── base libs from scope.excludes ─────────────────────────────────────
		final List<ArchLib> baseLibList = new ArrayList<>();
		for (final String path : archConfig.scope().excludes()) {
			baseLibList.add(new ArchLib(shortName(path), archPattern(path)));
		}
		ArchModule.setBaseLibs(baseLibList.toArray(ArchLib[]::new));

		// ── all libs (base + named) ───────────────────────────────────────────
		final Map<String, ArchLib> libs = new LinkedHashMap<>();
		baseLibList.forEach(lib -> libs.put(lib.name(), lib));
		if (archConfig.libs() != null) {
			archConfig.libs().forEach((name, path) -> libs.put(name, new ArchLib(name, archPattern(path))));
		}

		// ── create flat modules ───────────────────────────────────────────────
		final Map<String, ArchModule> modules = new LinkedHashMap<>();
		archConfig.modules().forEach((name, def) -> modules.put(name, ArchModule.of(name, archPattern(def.path()))));

		// ── dep rules ─────────────────────────────────────────────────────────
		final List<ArchRule> rules = new ArrayList<>();
		archConfig.modules().forEach((name, def) -> {
			final ArchDepBuilder db = modules.get(name).dependsOn();
			for (final String dep : def.deps()) {
				Assertion.check().isTrue(
						modules.containsKey(dep) || libs.containsKey(dep),
						"Unknown dep ''{0}'' in module ''{1}'' — known modules: {2}, libs: {3}",
						dep, name, modules.keySet(), libs.keySet());
				if (modules.containsKey(dep)) {
					db.modules(modules.get(dep));
				} else {
					db.libs(libs.get(dep));
				}
			}
			rules.add(db);
		});

		// ── coverage rule ─────────────────────────────────────────────────────
		final String scopePattern = archPattern(archConfig.scope().path());
		final String[] excludePatterns = archConfig.scope().excludes().stream()
				.map(ArchModuleLoader::archPattern)
				.toArray(String[]::new);
		final String[] modulePatterns = modules.values().stream()
				.flatMap(m -> java.util.Arrays.stream(m.packagePatterns()))
				.toArray(String[]::new);

		rules.add(0, classes()
				.that().resideInAPackage(scopePattern)
				.and().resideOutsideOfPackages(excludePatterns)
				.should().resideInAnyPackage(modulePatterns)
				.as("Every class in [" + archConfig.scope().path() + "] must belong to a declared module"));
		return rules;
	}

	// ─── Path conversion ──────────────────────────────────────────────────────

	/** "io.vertigo.vivid.schema.**" → "io.vertigo.vivid.schema.." (ArchUnit pattern). */
	static String archPattern(final String path) {
		if (path.endsWith(".**")) {
			return path.substring(0, path.length() - 3) + "..";
		}
		return path;
	}

	/** "java.**" → "java", "..stash.." → "stash". */
	private static String shortName(final String path) {
		final String stripped = path.replace("..", "");
		final int dot = stripped.indexOf('.');
		return dot < 0 ? stripped : stripped.substring(0, dot);
	}
}
