package io.vertigo.core;

import java.io.File;
import java.net.URISyntaxException;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import io.vertigo.arch.ArchChecker;

@AnalyzeClasses(
		packages = "io.vertigo.core",
		importOptions = { ImportOption.DoNotIncludeTests.class })
class CoreArchitectureTest {

	@ArchTest
	void module_dependency_rules_yaml(JavaClasses classes) throws URISyntaxException {
		final File yaml = new File(CoreArchitectureTest.class.getResource("core-modules.yaml").toURI());
		ArchChecker.check(classes, yaml);
	}
}
