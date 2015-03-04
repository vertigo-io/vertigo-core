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
package io.vertigo.dynamo.environment.oom;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtStereotype;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de lecture d'un OOM.
 *
 * @author npiedeloup
 */
public final class OOMParserStereotypesTest extends AbstractTestCaseJU4 {
	@Override
	protected String[] getManagersXmlFileName() {
		return new String[] { "managers-test.xml", "resources-test-stereotypes.xml" };
	}

	private static DtDefinition getDtDefinition(final String urn) {
		return Home.getDefinitionSpace().resolve(urn, DtDefinition.class);
	}

	/**
	 * Test du stereotype MasterData
	 */
	@Test
	public void testStereotypeMasterData() {
		final DtDefinition dtDefinitionDepartement = getDtDefinition("DT_DEPARTEMENT");
		Assert.assertNotNull(dtDefinitionDepartement);
		Assert.assertEquals(DtStereotype.MasterData, dtDefinitionDepartement.getStereotype());

		final DtDefinition dtDefinitionDossierType = getDtDefinition("DT_DOSSIER_TYPE");
		Assert.assertNotNull(dtDefinitionDossierType);
		Assert.assertEquals(DtStereotype.MasterData, dtDefinitionDossierType.getStereotype());
	}

	/**
	 * Test du stereotype Subject
	 */
	@Test
	public void testStereotypeSubject() {
		final DtDefinition dtDefinitionDossier = getDtDefinition("DT_DOSSIER");
		Assert.assertNotNull(dtDefinitionDossier);
		Assert.assertEquals(DtStereotype.Subject, dtDefinitionDossier.getStereotype());

	}

	/**
	 * Test du stereotype Data
	 */
	@Test
	public void testStereotypeData() {
		final DtDefinition dtDefinitionAttachment = getDtDefinition("DT_ATTACHMENT");
		Assert.assertNotNull(dtDefinitionAttachment);
		Assert.assertEquals(DtStereotype.Data, dtDefinitionAttachment.getStereotype());

		final DtDefinition dtDefinitionDossierValidation = getDtDefinition("DT_DOSSIER_VALIDATION");
		Assert.assertNotNull(dtDefinitionDossierValidation);
		Assert.assertEquals(DtStereotype.Data, dtDefinitionDossierValidation.getStereotype());
	}
}
