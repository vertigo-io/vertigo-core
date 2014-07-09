/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
