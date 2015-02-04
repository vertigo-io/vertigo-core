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
package io.vertigo.dynamo.environment.java;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.FormatterDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.dynamox.domain.formatter.FormatterDefault;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'implémentation standard.
 *
 * @author dchallas
 */
public final class JavaEnvironmentManagerTest extends AbstractTestCaseJU4 {
	@Test
	public void testDefaultFormatter() {
		final FormatterDefinition formatter = Home.getDefinitionSpace().resolve(FormatterDefinition.FMT_DEFAULT, FormatterDefinition.class);
		Assert.assertEquals(FormatterDefault.class.getName(), formatter.getFormatterClassName());
	}

	@Test
	public void testDomain() {
		final io.vertigo.dynamo.domain.metamodel.Domain domain = Home.getDefinitionSpace().resolve("DO_IDENTIFIANT", Domain.class);
		Assert.assertEquals(DataType.Long, domain.getDataType());
		Assert.assertEquals(FormatterDefault.class.getName(), domain.getFormatter().getFormatterClassName());
	}

	public void testFamille() {
		final DtDefinition dtDefinition = Home.getDefinitionSpace().resolve("DT_FAMILLE", DtDefinition.class);
		Assert.assertEquals(true, dtDefinition.isPersistent());
		Assert.assertEquals(io.vertigo.dynamock.domain.famille.Famille.class.getCanonicalName(), dtDefinition.getClassCanonicalName());
		Assert.assertEquals(io.vertigo.dynamock.domain.famille.Famille.class.getPackage().getName(), dtDefinition.getPackageName());
		Assert.assertEquals(io.vertigo.dynamock.domain.famille.Famille.class.getSimpleName(), dtDefinition.getClassSimpleName());
	}

	@Test
	public void testCreateFamille() {
		final Famille famille = new Famille();
		famille.setFamId(45L);
		famille.setLibelle("Armes");

		Assert.assertEquals(45L, famille.getFamId().longValue());
		Assert.assertEquals("Armes", famille.getLibelle());
		Assert.assertEquals("Armes[45]", famille.getDescription());

		//--Vérification des appels dynamiques--
		final DtDefinition dtFamille = DtObjectUtil.findDtDefinition(Famille.class);

		final DtField libelleDtField = dtFamille.getField("LIBELLE");
		Assert.assertEquals("Armes", libelleDtField.getDataAccessor().getValue(famille));
		//-cas du id
		final DtField idDtField = dtFamille.getField("FAM_ID");
		Assert.assertEquals(45L, idDtField.getDataAccessor().getValue(famille));
		//-cas du computed
		final DtField descriptionDtField = dtFamille.getField("DESCRIPTION");
		Assert.assertEquals("Armes[45]", descriptionDtField.getDataAccessor().getValue(famille));
	}
}
