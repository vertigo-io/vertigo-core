package io.vertigo.arch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;

/**
 * Fluent builder for module dependency rules.
 * Implements {@link ArchRule} so it can be used directly in {@code @ArchTest} fields.
 *
 * <pre>{@code
 * ArchRule rule = READER.dependsOn().modules(SCHEMA).libs(JACKSON);
 * }</pre>
 */
final class ArchDepBuilder implements ArchRule {
	private final ArchModule module;
	private final List<ArchModule> modules = new ArrayList<>();
	private final List<ArchLib> libs;

	ArchDepBuilder(final ArchModule module, final ArchLib... baseLibs) {
		this.module = module;
		this.libs = new ArrayList<>(Arrays.asList(baseLibs));
	}

	ArchDepBuilder modules(final ArchModule... deps) {
		modules.addAll(Arrays.asList(deps));
		return this;
	}

	ArchDepBuilder libs(final ArchLib... deps) {
		libs.addAll(Arrays.asList(deps));
		return this;
	}

	private ArchRule build() {
		final List<ArchDep> all = new ArrayList<>();
		all.addAll(modules);
		all.addAll(libs);
		return module.dependsOn(all.toArray(ArchDep[]::new));
	}

	@Override
	public EvaluationResult evaluate(final JavaClasses classes) {
		return build().evaluate(classes);
	}

	@Override
	public void check(final JavaClasses classes) {
		build().check(classes);
	}

	@Override
	public String getDescription() {
		return build().getDescription();
	}

	@Override
	public ArchRule because(final String reason) {
		return build().because(reason);
	}

	@Override
	public ArchRule as(final String description) {
		return build().as(description);
	}

	@Override
	public ArchRule allowEmptyShould(final boolean allowEmptyShould) {
		return build().allowEmptyShould(allowEmptyShould);
	}
}
