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
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DefinitionBodyRule;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.XDefinitionBody;

import org.junit.Assert;
import org.junit.Test;

public class DefinitionBodyRuleTest {
	private final DynamicDefinitionRepository dynamicDefinitionRepository = DynamicRegistryMock.createDynamicDefinitionRepository();

	@Test
	public void test1() throws NotFoundException {
		final Entity entity = dynamicDefinitionRepository.getGrammar().getEntity("Formatter");
		final DefinitionBodyRule definitionBodyRule = new DefinitionBodyRule(dynamicDefinitionRepository, entity);
		final Parser<XDefinitionBody> parser = definitionBodyRule.createParser();
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
		final Entity entity = dynamicDefinitionRepository.getGrammar().getEntity("Domain");
		final DefinitionBodyRule definitionBodyRule = new DefinitionBodyRule(dynamicDefinitionRepository, entity);
		final Parser<XDefinitionBody> parser = definitionBodyRule.createParser();
		parser.parse("{ dataType : String ,  formatter : FMT_DEFAULT,  constraint : [ CK_CODE_POSTAL ]    } ", 0);
	}

	@Test
	public void testError() {
		final Entity entity = dynamicDefinitionRepository.getGrammar().getEntity("Domain");
		final DefinitionBodyRule definitionBodyRule = new DefinitionBodyRule(dynamicDefinitionRepository, entity);
		final Parser<XDefinitionBody> parser = definitionBodyRule.createParser();
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
