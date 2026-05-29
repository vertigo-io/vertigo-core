package io.vertigo.arch;

import java.io.File;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import io.vertigo.arch.config.ArchConfig;

@AnalyzeClasses(packages = "io.vertigo.arch.test")
class ArchCheckerTest {
	private static final File YAML = new File("src/test/java/io/vertigo/arch/test/arch-test-modules.yaml");

	@ArchTest
	void module_dependency_rules_yaml(final JavaClasses classes) {
		ArchChecker.check(classes, YAML);
	}

	@ArchTest
	void module_dependency_rules_java(final JavaClasses classes) {
		ArchConfig.builder("io.vertigo.arch.test.**")
				.description("Test fixture — 4 modules with explicit cross-module dependencies")
				.exclude("java.**")
				.module("alpha").description("Entry point — depends on beta and gamma").deps("beta", "gamma")
				.module("beta").description("Intermediate layer — depends on delta").deps("delta")
				.module("gamma")
				.module("delta")
				.check(classes);
	}

	@ArchTest
	void module_dependency_rules_with_directives(final JavaClasses classes) {
		ArchConfig.builder("io.vertigo.arch.test.**")
				.exclude("java.**")
				.module("alpha").deps("beta", "gamma")
				.module("beta").deps("delta")
				.module("gamma")
				.module("delta")
				.directive("no-cycle")
				.directive("no-direct-impl-dep")
				.check(classes);
	}
}
