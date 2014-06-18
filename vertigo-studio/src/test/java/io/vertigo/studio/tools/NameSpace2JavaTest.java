package io.vertigo.studio.tools;

import junit.framework.TestCase;

/**
 * Test la génération à partir des oom et ksp.
 * @author dchallas
 */
public class NameSpace2JavaTest extends TestCase {
	/**
	 * Lancement du test.
	 */
	public void testGenerate() {
		NameSpace2Java.main(new String[] { "/io/vertigo/studio/tools/test.properties" });
	}

	/**
	 * Lancement du test.
	 */
	public void testGenerateFile() {
		NameSpace2Java.main(new String[] { "/io/vertigo/studio/tools/testFile.properties" });
	}
}
