/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.environment.eaxmi;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.dynamo.domain.metamodel.ConstraintDefinition;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.domain.metamodel.FormatterDefinition;
import io.vertigo.dynamox.domain.formatter.FormatterDefault;
import io.vertigo.dynamox.domain.formatter.FormatterNumber;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public final class EAXmiEnvironmentManagerTest extends AbstractTestCaseJU4 {
	private DefinitionSpace definitionSpace;

	@Override
	protected void doSetUp() throws Exception {
		definitionSpace = getApp().getDefinitionSpace();
	}

	@Override
	protected String[] getManagersXmlFileName() {
		return new String[] { "managers-test.xml", "resources-test.xml" };
	}

	@Test
	public void testConstraint() {
		final ConstraintDefinition constraint = definitionSpace.resolve("CK_TELEPHONE", ConstraintDefinition.class);
		Assert.assertEquals(DtProperty.REGEX, constraint.getProperty());
	}

	@Test
	public void testDefaultFormatter() {
		final FormatterDefinition formatter = definitionSpace.resolve("FMT_DEFAULT", FormatterDefinition.class);
		Assert.assertEquals(FormatterDefault.class.getName(), formatter.getFormatterClassName());
	}

	@Test
	public void testFormatter() {
		final FormatterDefinition formatter = definitionSpace.resolve("FMT_TAUX", FormatterDefinition.class);
		Assert.assertEquals(FormatterNumber.class.getName(), formatter.getFormatterClassName());
	}

	@Test
	public void testDomain() {
		final io.vertigo.dynamo.domain.metamodel.Domain domain = definitionSpace.resolve("DO_EMAIL", Domain.class);
		Assert.assertEquals(DataType.String, domain.getDataType());
		Assert.assertEquals(FormatterDefault.class.getName(), domain.getFormatterClassName());
	}

	@Test
	public void testDtDefinition() {
		final DtDefinition dtDefinition = definitionSpace.resolve("DT_FAMILLE", DtDefinition.class);
		Assert.assertEquals("io.vertigo.dynamock.domain.famille.Famille", dtDefinition.getClassCanonicalName());
		Assert.assertTrue(dtDefinition.isPersistent());
		Assert.assertEquals("io.vertigo.dynamock.domain.famille", dtDefinition.getPackageName());
	}
}
