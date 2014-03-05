package io.vertigo.studio.tools;

import io.vertigo.studio.tools.NameSpace2Java;
import junit.framework.TestCase;

/**
 * Test la génération à partir des oom et ksp.
 * @author dchallas
 * @version $Id: NameSpace2JavaTest.java,v 1.2 2014/02/07 16:05:38 npiedeloup Exp $
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
