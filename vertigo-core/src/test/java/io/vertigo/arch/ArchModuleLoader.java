package io.vertigo.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.tngtech.archunit.lang.ArchRule;

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
	private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

	// ─── Records ──────────────────────────────────────────────────────────────

	record ScopeDef(
			String path,
			String description,
			List<String> excludes) {

		public List<String> excludes() {
			return excludes != null ? excludes : List.of();
		}
	}

	record Config(
			ScopeDef scope,
			Map<String, String> libs,
			Map<String, ModuleDef> modules) {
	}

	record ModuleDef(
			String path,
			String description,
			List<String> deps) {

		public List<String> deps() {
			return deps != null ? deps : List.of();
		}
	}

	private ArchModuleLoader() {
	}

	// ─── Public API ───────────────────────────────────────────────────────────

	/** Loads and concatenates rules from all given YAML file names. */
	public static List<ArchRule> load(final File... files) {
		final List<ArchRule> all = new ArrayList<>();
		for (final File file : files) {
			all.addAll(loadOne(file));
		}
		return all;
	}

	// ─── Per-file loading ─────────────────────────────────────────────────────

	private static List<ArchRule> loadOne(final File file) {
		final Config config = parseYaml(file);

		// ── base libs from scope.excludes ─────────────────────────────────────
		final List<ArchLib> baseLibList = new ArrayList<>();
		for (final String path : config.scope().excludes()) {
			baseLibList.add(new ArchLib(shortName(path), archPattern(path)));
		}
		ArchModule.setBaseLibs(baseLibList.toArray(ArchLib[]::new));

		// ── all libs (base + named) ───────────────────────────────────────────
		final Map<String, ArchLib> libs = new LinkedHashMap<>();
		baseLibList.forEach(lib -> libs.put(lib.name(), lib));
		if (config.libs() != null) {
			config.libs().forEach((name, path) -> libs.put(name, new ArchLib(name, archPattern(path))));
		}

		// ── create flat modules ───────────────────────────────────────────────
		final Map<String, ArchModule> modules = new LinkedHashMap<>();
		config.modules().forEach((name, def) -> modules.put(name, ArchModule.of(name, archPattern(def.path()))));

		// ── dep rules ─────────────────────────────────────────────────────────
		final List<ArchRule> rules = new ArrayList<>();
		config.modules().forEach((name, def) -> {
			final ArchDepBuilder db = modules.get(name).dependsOn();
			for (final String dep : def.deps()) {
				if (modules.containsKey(dep)) {
					db.modules(modules.get(dep));
				} else if (libs.containsKey(dep)) {
					db.libs(libs.get(dep));
				} else {
					throw new IllegalStateException(
							"Unknown dep '" + dep + "' in module '" + name + "'"
									+ " — known modules: " + modules.keySet()
									+ ", libs: " + libs.keySet());
				}
			}
			rules.add(db);
		});

		// ── coverage rule ─────────────────────────────────────────────────────
		final String scopePattern = archPattern(config.scope().path());
		final String[] excludePatterns = config.scope().excludes().stream()
				.map(ArchModuleLoader::archPattern)
				.toArray(String[]::new);
		final String[] modulePatterns = modules.values().stream()
				.flatMap(m -> java.util.Arrays.stream(m.packagePatterns()))
				.toArray(String[]::new);

		rules.add(0, classes()
				.that().resideInAPackage(scopePattern)
				.and().resideOutsideOfPackages(excludePatterns)
				.should().resideInAnyPackage(modulePatterns)
				.as("Every class in [" + config.scope().path() + "] must belong to a declared module"));
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

	// ─── YAML parsing ─────────────────────────────────────────────────────────

	private static Config parseYaml(final File file) {
		try (InputStream in = new FileInputStream(file)) {
			return YAML.readValue(in, Config.class);
		} catch (final IOException e) {
			throw new IllegalStateException("Cannot load " + file, e);
		}
	}
}
