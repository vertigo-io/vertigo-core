package io.vertigo.persona;

import io.vertigo.persona.security.KSecurityManagerTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test de l'implementation standard.
 *
 * @author pchretien
 * @version $Id: CommonsTestSuite.java,v 1.15 2014/06/30 12:21:52 pchretien Exp $
 */
@RunWith(Suite.class)
@SuiteClasses({ KSecurityManagerTest.class })
public final class PersonaTestSuite {
	//
}
