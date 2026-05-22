package io.vertigo.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import com.tngtech.archunit.core.domain.JavaClasses;

import io.vertigo.arch.config.ArchConfig;
import io.vertigo.arch.config.ArchYamlConfigLoader;
import io.vertigo.core.lang.Assertion;

/**
 * Checks module dependency rules loaded from YAML files against a set of Java classes.
 *
 * <h3>YAML format</h3>
 *
 * <pre>
 * scope:
 *   path: io.vertigo.core.**
 *   description: "..."
 *   excludes: [java.**, jakarta.**]
 *
 * libs:
 *   jackson: com.fasterxml.jackson.**
 *
 * modules:
 *   reader:
 *     path: io.vertigo.core.reader.**
 *     deps: [schema, jackson]
 * </pre>
 *
 * <h3>Rules checked per file</h3>
 * <ul>
 * <li>No intersection: module package paths must not overlap.</li>
 * <li>Coverage: every class in scope must belong to exactly one module.</li>
 * <li>Isolation: each module may only depend on its declared deps + scope excludes.</li>
 * <li>No orphan deps: every declared dep must be effectively used.</li>
 * </ul>
 */
public final class ArchModuleLoader {

	private ArchModuleLoader() {
	}

	/** Loads configs from YAML files and checks all rules against the given classes. */
	public static void check(final JavaClasses classes, final File... files) {
		Assertion.check()
				.isNotNull(classes, "classes are required")
				.isNotNull(files, "files are required");
		//---
		for (final File file : files) {
			checkOne(ArchYamlConfigLoader.parseYaml(file), classes);
		}
	}

	// ─── Per-config checks ────────────────────────────────────────────────────

	private static void checkOne(final ArchConfig config, final JavaClasses classes) {
		checkNoIntersection(config);
		checkCoverage(config, classes);
		checkIsolation(config, classes);
		checkNoOrphanDeps(config, classes);
	}

	/** Vérifie qu'aucun chemin de module n'englobe un autre (pas d'intersection possible). */
	private static void checkNoIntersection(final ArchConfig config) {
		final List<Map.Entry<String, String>> entries = config.modules().entrySet().stream()
				.map(e -> Map.entry(e.getKey(), packagePrefix(archPattern(e.getValue().path()))))
				.toList();

		for (int i = 0; i < entries.size(); i++) {
			for (int j = i + 1; j < entries.size(); j++) {
				final String p1 = entries.get(i).getValue();
				final String p2 = entries.get(j).getValue();
				Assertion.check().isFalse(
						p1.equals(p2) || p1.startsWith(p2 + ".") || p2.startsWith(p1 + "."),
						"Modules ''{0}'' and ''{1}'' have overlapping package paths",
						entries.get(i).getKey(), entries.get(j).getKey());
			}
		}
	}

	/** Vérifie que chaque classe du scope appartient à un module déclaré. */
	private static void checkCoverage(final ArchConfig config, final JavaClasses classes) {
		final String scopePattern = archPattern(config.scope().path());
		final String[] excludePatterns = config.scope().excludes().stream()
				.map(ArchModuleLoader::archPattern)
				.toArray(String[]::new);
		final String[] modulePatterns = config.modules().values().stream()
				.map(def -> archPattern(def.path()))
				.toArray(String[]::new);

		classes()
				.that().resideInAPackage(scopePattern)
				.and().resideOutsideOfPackages(excludePatterns)
				.should().resideInAnyPackage(modulePatterns)
				.as("Every class in [" + config.scope().path() + "] must belong to a declared module")
				.check(classes);
	}

	/** Vérifie que chaque module ne dépend que de ses dépendances déclarées et des excludes de base. */
	private static void checkIsolation(final ArchConfig config, final JavaClasses classes) {
		final Map<String, String> allPatterns = buildPatternIndex(config);
		final String[] basePatterns = config.scope().excludes().stream()
				.map(ArchModuleLoader::archPattern)
				.toArray(String[]::new);

		config.modules().forEach((name, def) -> {
			for (final String dep : def.deps()) {
				Assertion.check().isTrue(
						allPatterns.containsKey(dep),
						"Unknown dep ''{0}'' in module ''{1}'' — known: {2}",
						dep, name, allPatterns.keySet());
			}

			final List<String> allowed = new ArrayList<>();
			allowed.add(archPattern(def.path()));
			allowed.addAll(Arrays.asList(basePatterns));
			for (final String dep : def.deps()) {
				allowed.add(allPatterns.get(dep));
			}

			final String depLabel = def.deps().isEmpty() ? "self"
					: "self, " + String.join(", ", def.deps());

			classes()
					.that().resideInAnyPackage(archPattern(def.path()))
					.should().onlyDependOnClassesThat()
					.resideInAnyPackage(allowed.toArray(String[]::new))
					.as("Module '" + name + "' may only depend on [" + depLabel + "]")
					.check(classes);
		});
	}

	/** Vérifie que chaque dépendance déclarée est effectivement utilisée (pas de dépendances orphelines). */
	private static void checkNoOrphanDeps(final ArchConfig config, final JavaClasses classes) {
		final Map<String, String> allPatterns = buildPatternIndex(config);

		config.modules().forEach((name, def) -> {
			final String modulePrefix = packagePrefix(archPattern(def.path()));

			for (final String dep : def.deps()) {
				final String depPrefix = packagePrefix(allPatterns.get(dep));

				final boolean used = StreamSupport.stream(classes.spliterator(), false)
						.filter(c -> matchesPrefix(c.getPackageName(), modulePrefix))
						.anyMatch(c -> c.getDirectDependenciesFromSelf().stream()
								.anyMatch(d -> matchesPrefix(d.getTargetClass().getPackageName(), depPrefix)));

				Assertion.check().isTrue(used,
						"Module ''{0}'' declares dep ''{1}'' but never uses it (orphan dependency)",
						name, dep);
			}
		});
	}

	// ─── Helpers ─────────────────────────────────────────────────────────────

	/** Builds a name → ArchUnit pattern index for all modules and named libs. */
	private static Map<String, String> buildPatternIndex(final ArchConfig config) {
		final Map<String, String> index = new LinkedHashMap<>();
		config.modules().forEach((name, def) -> index.put(name, archPattern(def.path())));
		if (config.libs() != null) {
			config.libs().forEach((name, path) -> index.put(name, archPattern(path)));
		}
		return index;
	}

	/** "io.vertigo.core.lang.." → "io.vertigo.core.lang" (removes ArchUnit recursive suffix). */
	private static String packagePrefix(final String archUnitPattern) {
		if (archUnitPattern.endsWith("..")) {
			return archUnitPattern.substring(0, archUnitPattern.length() - 2);
		}
		return archUnitPattern;
	}

	/** Returns true if packageName equals prefix or is a sub-package of prefix. */
	private static boolean matchesPrefix(final String packageName, final String prefix) {
		return packageName.equals(prefix) || packageName.startsWith(prefix + ".");
	}

	/** "io.vertigo.vivid.schema.**" → "io.vertigo.vivid.schema.." (ArchUnit pattern). */
	static String archPattern(final String path) {
		if (path.endsWith(".**")) {
			return path.substring(0, path.length() - 3) + "..";
		}
		return path;
	}
}
