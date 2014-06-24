package io.vertigo.dynamo.environment.plugins.loaders.kpr.definition;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DynamicDefinitionRule;

import org.junit.Assert;
import org.junit.Test;

public class DefinitionRuleTest {
	private final DynamicDefinitionRepository dynamicDefinitionRepository = DefinitionTestUtil.createDynamicDefinitionRepository();

	@Test
	public void test1() throws NotFoundException {
		final DynamicDefinitionRule definitionRule = new DynamicDefinitionRule("create", dynamicDefinitionRepository);

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
		final DynamicDefinitionRule definitionRule = new DynamicDefinitionRule("create", dynamicDefinitionRepository);

		final Parser<DynamicDefinition> parser = definitionRule.createParser();
		parser.parse("create Domain DO_CODE_POSTAL { dataType : String ,  formatter:FMT_DEFAULT, constraint : [ CK_CODE_POSTAL ]   } ", 0);
		Assert.assertNotNull(parser.get());
	}

	@Test
	public void testTemplate() throws NotFoundException {
		final DynamicDefinitionRule DynamicDefinitionRule = new DynamicDefinitionRule("alter", dynamicDefinitionRepository);
		DynamicDefinitionRule.createParser().parse("alter Formatter FMT_DEFAULT {args : \"UPPER\"}", 0);
	}
}
