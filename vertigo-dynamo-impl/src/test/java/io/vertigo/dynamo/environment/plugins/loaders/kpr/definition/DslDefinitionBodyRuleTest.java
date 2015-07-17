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
import io.vertigo.core.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.core.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.DslDefinitionBody;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DslDefinitionBodyRule;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class DslDefinitionBodyRuleTest {
	private final DynamicDefinitionRepository dynamicDefinitionRepository = DslDynamicRegistryMock.createDynamicDefinitionRepository();

	private static Entity find(final List<Entity> entities, final String entityName) {
		for (final Entity entity : entities) {
			if (entity.getName().equals(entityName)) {
				return entity;
			}
		}
		throw new RuntimeException("not found " + entityName);
	}

	@Test
	public void test1() throws NotFoundException {
		final List<Entity> entities = dynamicDefinitionRepository.getGrammar().getEntities();

		final Entity entity = find(entities, "Formatter");

		final DslDefinitionBodyRule definitionBodyRule = new DslDefinitionBodyRule(dynamicDefinitionRepository, entity);
		final Parser<DslDefinitionBody> parser = definitionBodyRule.createParser();
		parser.parse("{ args : \"UPPER\" }", 0);
		Assert.assertEquals(0, parser.get().getDefinitionEntries().size()); //On vérifie que l'on a une et une seule propriété
		Assert.assertEquals(1, parser.get().getPropertyEntries().size());
	}

	//Exemple de test sur la déclaration d'un Domain
	//	create Domain DO_CODE_POSTAL (
	//			dataType : String;
	//			formatter : FMT_DEFAULT;
	//			constraint : {CK_CODE_POSTAL}
	//		)
	@Test
	public void test2() throws NotFoundException {
		final List<Entity> entities = dynamicDefinitionRepository.getGrammar().getEntities();
		final Entity entity = find(entities, "Domain");
		final DslDefinitionBodyRule definitionBodyRule = new DslDefinitionBodyRule(dynamicDefinitionRepository, entity);
		final Parser<DslDefinitionBody> parser = definitionBodyRule.createParser();
		parser.parse("{ dataType : String ,  formatter : FMT_DEFAULT,  constraint : [ CK_CODE_POSTAL ]    } ", 0);
	}

	@Test
	public void testError() {
		final List<Entity> entities = dynamicDefinitionRepository.getGrammar().getEntities();
		final Entity entity = find(entities, "Domain");
		final DslDefinitionBodyRule definitionBodyRule = new DslDefinitionBodyRule(dynamicDefinitionRepository, entity);
		final Parser<DslDefinitionBody> parser = definitionBodyRule.createParser();
		final String testValue = "{ dataType : String ,  formatter : FMT_DEFAULT,  constraint : [ CK_CODE_POSTAL ] , maxLengh:\"true\"   } ";
		try {
			parser.parse(testValue, 0);
			Assert.fail();
		} catch (final NotFoundException e) {
			System.out.println(e.getFullMessage());
			Assert.assertEquals(testValue.indexOf("maxLengh") + "maxLengh".length() - 1, e.getIndex());
		}
	}
}
