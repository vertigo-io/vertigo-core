package io.vertigo.core;

import java.io.File;
import java.util.List;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import io.vertigo.arch.ArchModuleLoader;

@AnalyzeClasses(
		packages = "io.vertigo.core",
		importOptions = { ImportOption.DoNotIncludeTests.class })
class CoreArchitectureTest {
	private static final File YAML_DIR = new File("src/test/java/io/vertigo/core");

	@ArchTest
	void module_dependency_rules_yaml(JavaClasses classes) throws Exception {
		//		// importPackages ne fonctionne pas sous Java 25 + WSL avec Surefire (0 classe trouvée).
		//		// On pointe directement sur target/classes via la location d'une classe du module.
		//		final URI mainClassesUri = Assertion.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		//		final Path mainClassesDir = Path.of(mainClassesUri);
		//		System.err.println("mainClassesDir=" + mainClassesDir + " exists=" + mainClassesDir.toFile().exists());
		//		final JavaClasses classes = new ClassFileImporter().importPath(mainClassesDir);
		//		System.err.println("classes.size()=" + classes.size());
		final List<ArchRule> rules = ArchModuleLoader.load(new File(YAML_DIR, "core-modules.yaml"));
		for (final ArchRule rule : rules) {
			rule.check(classes);
		}
	}
}
