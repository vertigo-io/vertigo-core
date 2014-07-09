package io.vertigo.labs;

import io.vertigo.labs.france.FranceManagerTest;
import io.vertigo.labs.gedcom.GedcomManagerTest;
import io.vertigo.labs.geocoder.google.GoogleGeoCoderManagerTest;
import io.vertigo.labs.job.JobManagerTest;
import io.vertigo.labs.mail.MailManagerTest;
import io.vertigo.labs.trait.TraitManagerTest;

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
@SuiteClasses({ //
FranceManagerTest.class,//
		GedcomManagerTest.class,//
		GoogleGeoCoderManagerTest.class,//
		TraitManagerTest.class,//
		MailManagerTest.class,//
		JobManagerTest.class,//
})
public final class LabsTestSuite {
	//
}
