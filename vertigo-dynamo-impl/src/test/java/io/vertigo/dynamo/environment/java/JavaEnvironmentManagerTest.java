package io.vertigo.dynamo.environment.java;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.KDataType;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.dynamox.domain.formatter.FormatterDefault;
import io.vertigo.kernel.Home;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'implémentation standard.
 * 
 * @author dchallas
 * @version $Id: JavaEnvironmentManagerTest.java,v 1.3 2014/01/20 17:51:47 pchretien Exp $
 */
public final class JavaEnvironmentManagerTest extends AbstractTestCaseJU4 {
	@Test
	public void testDefaultFormatter() {
		final Formatter formatter = Home.getDefinitionSpace().resolve(Formatter.FMT_DEFAULT, Formatter.class);
		Assert.assertEquals(FormatterDefault.class, formatter.getClass());
	}

	@Test
	public void testDomain() {
		final io.vertigo.dynamo.domain.metamodel.Domain domain = Home.getDefinitionSpace().resolve("DO_IDENTIFIANT", Domain.class);
		Assert.assertEquals(KDataType.Long, domain.getDataType());
		Assert.assertEquals(FormatterDefault.class, domain.getFormatter().getClass());
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
