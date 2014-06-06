package io.vertigo.dynamo.environment.eaxmi;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.Constraint;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.KDataType;
import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.dynamox.domain.constraint.ConstraintRegex;
import io.vertigo.dynamox.domain.formatter.FormatterDefault;
import io.vertigo.dynamox.domain.formatter.FormatterNumber;
import io.vertigo.kernel.Home;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'implémentation standard.
 * 
 * @author pchretien 
 * @version $Id: OOMEnvironmentManagerTest.java,v 1.3 2014/01/20 17:51:47 pchretien Exp $
 */
public final class EAXmiEnvironmentManagerTest extends AbstractTestCaseJU4 {
	@Test
	public void testConstraint() {
		final Constraint<?, ?> constraint = Home.getDefinitionSpace().resolve("CK_TELEPHONE", Constraint.class);
		Assert.assertEquals(ConstraintRegex.class, constraint.getClass());
	}

	@Test
	public void testDefaultFormatter() {
		final Formatter formatter = Home.getDefinitionSpace().resolve(Formatter.FMT_DEFAULT, Formatter.class);
		Assert.assertEquals(FormatterDefault.class, formatter.getClass());
	}

	@Test
	public void testFormatter() {
		final Formatter formatter = Home.getDefinitionSpace().resolve("FMT_TAUX", Formatter.class);
		Assert.assertEquals(FormatterNumber.class, formatter.getClass());
	}

	@Test
	public void testDomain() {
		final io.vertigo.dynamo.domain.metamodel.Domain domain = Home.getDefinitionSpace().resolve("DO_EMAIL", Domain.class);
		Assert.assertEquals(KDataType.String, domain.getDataType());
		Assert.assertEquals(FormatterDefault.class, domain.getFormatter().getClass());
	}

	@Test
	public void testDtDefinition() {
		final DtDefinition dtDefinition = Home.getDefinitionSpace().resolve("DT_FAMILLE", DtDefinition.class);
		Assert.assertEquals(Famille.class.getCanonicalName(), dtDefinition.getClassCanonicalName());
		Assert.assertEquals(true, dtDefinition.isPersistent());
		Assert.assertEquals(Famille.class.getPackage().getName(), dtDefinition.getPackageName());
	}
}
