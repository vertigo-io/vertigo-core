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
	private final DynamicDefinitionRepository dynamicDefinitionRepository = DefinitionTestUtil.createDynamicDefinitionRepository();

	@Test
	public void test1() throws NotFoundException {
		final Entity entity = dynamicDefinitionRepository.getGrammar().getEntity("Formatter");
		final DefinitionBodyRule definitionBodyRule = new DefinitionBodyRule(dynamicDefinitionRepository, entity);
		final Parser<XDefinitionBody> parser = definitionBodyRule.createParser();
		parser.parse("( args : \"UPPER\"; )", 0);
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
		parser.parse("( dataType : String ;  formatter : FMT_DEFAULT;  constraint : { CK_CODE_POSTAL }    ) ", 0);
	}

	@Test
	public void testError() {
		final Entity entity = dynamicDefinitionRepository.getGrammar().getEntity("Domain");
		final DefinitionBodyRule definitionBodyRule = new DefinitionBodyRule(dynamicDefinitionRepository, entity);
		final Parser<XDefinitionBody> parser = definitionBodyRule.createParser();
		final String testValue = "( dataType : String ;  formatter : FMT_DEFAULT;  constraint : { CK_CODE_POSTAL } ; maxLengh:\"true\"   ) ";
		try {
			parser.parse(testValue, 0);
			Assert.fail();
		} catch (final NotFoundException e) {
			System.out.println(e.getFullMessage());
			Assert.assertEquals(testValue.indexOf("maxLengh") + "maxLengh".length() - 1, e.getIndex());
		}
	}
}
