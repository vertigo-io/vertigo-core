package io.vertigo.studio.plugins.mda.task.test;

import java.util.List;

import io.vertigo.lang.Assertion;

/**
 * Objet utilisé par FreeMarker.
 *
 * @author sezratty
 */
public final class TemplateTestSuite {
	private final String packageName;	
	private final List<TemplateTestClass> testClasses;

	/**
	 * Constructeur.
	 */
	TemplateTestSuite(final List<TemplateTestClass> testClasses, final String packageName) {
		Assertion.checkNotNull(testClasses);
		Assertion.checkNotNull(packageName);
		//-----
		this.packageName = packageName;
		this.testClasses = testClasses;
	}

	/**
	 * @return Nom du package de la classe de suite.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Liste des classes de test.
	 */
	public List<TemplateTestClass> getTestClasses() {
		return testClasses;
	}
}
