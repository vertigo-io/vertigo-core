package io.vertigo.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import com.tngtech.archunit.lang.ArchRule;

import io.vertigo.core.lang.Assertion;

/**
 * A named module identified by a package selector.
 *
 * <p>
 * Use {@link #dependsOn()} to start a fluent rule:
 * 
 * <pre>{@code
 * ArchModule READER = ArchModule.of("reader", "..vivid.reader..");
 * ArchRule rule = READER.dependsOn().modules(SCHEMA).libs(JACKSON);
 * }</pre>
 *
 * <p>
 * Base libs (e.g. java, vertigo-core) are registered once via {@link #setBaseLibs}
 * and included in every rule automatically.
 */
final class ArchModule implements ArchDep {
	private static ArchLib[] baseLibs = new ArchLib[0];

	/** Registers libs that every module may depend on (e.g. java, vertigo-core). */
	static void setBaseLibs(final ArchLib... libs) {
		Assertion.check().isNotNull(libs, "libs are required");
		//---
		baseLibs = libs;
	}

	private final String name;
	private final String packagePattern;

	private ArchModule(final String name, final String packagePattern) {
		Assertion.check()
				.isNotBlank(name, "name is required")
				.isNotBlank(packagePattern, "packagePattern is required");
		//---
		this.name = name;
		this.packagePattern = packagePattern;
	}

	static ArchModule of(final String name, final String packagePattern) {
		return new ArchModule(name, packagePattern);
	}

	/** Returns a fluent builder pre-loaded with the registered base libs. */
	ArchDepBuilder dependsOn() {
		return new ArchDepBuilder(this, baseLibs);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String[] packagePatterns() {
		return new String[] { packagePattern };
	}

	ArchRule dependsOn(final ArchDep... deps) {
		final var allowed = new ArrayList<String>();
		allowed.addAll(Arrays.asList(packagePatterns()));
		for (final var dep : deps) {
			allowed.addAll(Arrays.asList(dep.packagePatterns()));
		}

		final String depList = Stream.of(deps)
				.map(ArchDep::name)
				.reduce((a, b) -> a + ", " + b)
				.map(s -> "self, " + s)
				.orElse("self");

		return classes()
				.that().resideInAnyPackage(packagePatterns())
				.should().onlyDependOnClassesThat()
				.resideInAnyPackage(allowed.toArray(String[]::new))
				.as("Module '" + name + "' may only depend on [" + depList + "]");
	}
}
