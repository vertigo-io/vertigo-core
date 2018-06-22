package io.vertigo.studio.plugins.mda.task.test;

import io.vertigo.lang.Assertion;

/**
 * Objet utilisé par FreeMarker.
 *
 * @author sezratty
 */
public final class TemplateTestClass {
	private final String packageName;
	private final String className;

	/**
	 * Constructeur.
	 */
	TemplateTestClass(final String packageName, final String className) {
		Assertion.checkNotNull(packageName);
		Assertion.checkNotNull(className);
		//-----
		this.packageName = packageName;
		this.className = className;
	}

	/**
	 * @return Nom du package de la classe de suite.
	 */
	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}
	
	/**
	 * @return Nom canonique de la classe de test
	 */
	public String getClassCanonicalName() {
		return packageName + "." + className;
	}
}
