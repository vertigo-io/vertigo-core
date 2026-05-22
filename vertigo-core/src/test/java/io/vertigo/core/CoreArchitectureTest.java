package io.vertigo.core;

import java.io.File;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import io.vertigo.arch.ArchModuleLoader;

@AnalyzeClasses(
		packages = "io.vertigo.core",
		importOptions = { ImportOption.DoNotIncludeTests.class })
class CoreArchitectureTest {
	private static final File YAML_DIR = new File("src/test/java/io/vertigo/core");

	@ArchTest
	void module_dependency_rules_yaml(JavaClasses classes) {
		ArchModuleLoader.check(classes, new File(YAML_DIR, "core-modules.yaml"));
	}
}
