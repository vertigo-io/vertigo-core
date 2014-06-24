package io.vertigo.studio.tools;

import org.junit.Test;

/**
 * Test la génération à partir des oom et ksp.
 * @author dchallas
 */
public class NameSpace2JavaTest {
	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerate() {
		NameSpace2Java.main(new String[] { "/io/vertigo/studio/tools/test.properties" });
	}

	/**
	 * Lancement du test.
	 */
	@Test
	public void testGenerateFile() {
		NameSpace2Java.main(new String[] { "/io/vertigo/studio/tools/testFile.properties" });
	}
}
