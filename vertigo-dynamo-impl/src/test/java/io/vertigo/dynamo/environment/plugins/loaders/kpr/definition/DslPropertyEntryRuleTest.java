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
package io.vertigo.dynamo.environment.plugins.loaders.kpr.definition;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.core.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.core.impl.environment.kernel.meta.EntityPropertyType;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslPropertyEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslPropertyEntryRule;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public final class DslPropertyEntryRuleTest {
	private static final String LABEL = "LABEL";
	private static final String SIZE = "SIZE";

	private static DslPropertyEntryRule MAIN;
	static {
		final Set<EntityProperty> entityProperties = new HashSet<>();
		entityProperties.add(new EntityProperty(LABEL, EntityPropertyType.String));
		entityProperties.add(new EntityProperty(SIZE, EntityPropertyType.String));
		MAIN = new DslPropertyEntryRule(entityProperties);
	}

	@Test
	public void test() throws NotFoundException {
		final Parser<DslPropertyEntry> parser = MAIN.createParser();
		//---
		final String text = "label   : \"BLeU\", non reconnu";
		final int end = parser.parse(text, 0);
		final DslPropertyEntry propertyEntry = parser.get();
		Assert.assertEquals(LABEL, propertyEntry.getProperty().getName());
		Assert.assertEquals("BLeU", propertyEntry.getPropertyValueAsString());
		Assert.assertEquals(text.length() - " non reconnu".length(), end); //On vérfifie que le pointeur a avancé jusqu'à 'non reconnu'

	}

	@Test
	public void test2() throws NotFoundException {
		final Parser<DslPropertyEntry> parser = MAIN.createParser();
		//---
		final String text = "label  :    \" vert \"";
		final int end = parser.parse(text, 0); //On ne met pas de séparateur final et on met un espace
		final DslPropertyEntry propertyEntry = parser.get();
		Assert.assertEquals(LABEL, propertyEntry.getProperty().getName());
		Assert.assertEquals(" vert ", propertyEntry.getPropertyValueAsString()); //l'espace doit être conservé
		Assert.assertEquals(text.length(), end);
	}

	@Test
	public void test3() throws NotFoundException {
		final Parser<DslPropertyEntry> parser = MAIN.createParser();
		//---
		final String text = "size   : \"54\",";
		final int end = parser.parse(text, 0);
		final DslPropertyEntry propertyEntry = parser.get();
		Assert.assertEquals(SIZE, propertyEntry.getProperty().getName());
		Assert.assertEquals("54", propertyEntry.getPropertyValueAsString());
		Assert.assertEquals(text.length(), end);
	}

	@Test(expected = NotFoundException.class)
	public void testFail() throws NotFoundException {
		final Parser<DslPropertyEntry> parser = MAIN.createParser();
		//---
		final String text = "maxlength   : \"54\";"; //La propriété maxlength n'est pas enregistrée
		/*final int end = */parser.parse(text, 0);
		Assert.fail();
	}

	@Test(expected = NotFoundException.class)
	public void testFail2() throws NotFoundException {
		final Parser<DslPropertyEntry> parser = MAIN.createParser();
		//---
		final String text = "label  :    vert \"";
		parser.parse(text, 0); //On omet la quote de début
		Assert.fail();
	}

}
