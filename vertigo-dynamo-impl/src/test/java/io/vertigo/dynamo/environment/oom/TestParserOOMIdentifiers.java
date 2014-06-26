package io.vertigo.dynamo.environment.oom;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.kernel.Home;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de lecture d'un OOM.
 *
 * @author npiedeloup
 */
public final class TestParserOOMIdentifiers extends AbstractTestCaseJU4 {
	@Override
	protected String[] getManagersXmlFileName() {
		return new String[] { "managers-test.xml", "resources-test.xml" };
	}

	private static DtDefinition getDtDefinition(final String urn) {
		return Home.getDefinitionSpace().resolve(urn, DtDefinition.class);
	}

	@Test
	public void testIdentifiersVsPrimaryKey() {
		final DtDefinition loginDefinition = getDtDefinition("DT_LOGIN");
		Assert.assertTrue(loginDefinition.getIdField().isDefined());
	}
}
