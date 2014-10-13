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
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DSLDynamicDefinitionRule;

import org.junit.Assert;
import org.junit.Test;

public class DslDefinitionRuleTest {
	private final DynamicDefinitionRepository dynamicDefinitionRepository = DslDynamicRegistryMock.createDynamicDefinitionRepository();

	@Test
	public void test1() throws NotFoundException {
		final DSLDynamicDefinitionRule definitionRule = new DSLDynamicDefinitionRule("create", dynamicDefinitionRepository);

		final Parser<DynamicDefinition> parser = definitionRule.createParser();
		parser.parse("create Formatter FMT_TEST { args : \"UPPER\" }", 0);

		Assert.assertNotNull(parser.get());
	}

	//Exemple de test sur la déclaration d'un Domain
	//	create Domain DO_CODE_POSTAL (
	//			dataType : String;
	//			formatter : FMT_DEFAULT;
	//			constraint : {CK_CODE_POSTAL}
	//		)
	@Test
	public void test2() throws NotFoundException {
		final DSLDynamicDefinitionRule definitionRule = new DSLDynamicDefinitionRule("create", dynamicDefinitionRepository);

		final Parser<DynamicDefinition> parser = definitionRule.createParser();
		parser.parse("create Domain DO_CODE_POSTAL { dataType : String ,  formatter:FMT_DEFAULT, constraint : [ CK_CODE_POSTAL ]   } ", 0);
		Assert.assertNotNull(parser.get());
	}

	@Test
	public void testTemplate() throws NotFoundException {
		final DSLDynamicDefinitionRule DynamicDefinitionRule = new DSLDynamicDefinitionRule("alter", dynamicDefinitionRepository);
		DynamicDefinitionRule.createParser().parse("alter Formatter FMT_DEFAULT {args : \"UPPER\"}", 0);
	}
}
