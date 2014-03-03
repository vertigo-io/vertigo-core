package io.vertigo.dynamo.environment.plugins.loaders.kpr.definition;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.KDataType;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.definition.XPropertyEntry;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.XPropertyEntryRule;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public final class XPropertyEntryRuleTest {
	private static final String LABEL = "LABEL";
	private static final String SIZE = "SIZE";

	private static XPropertyEntryRule MAIN;
	static {
		final Set<EntityProperty> entityProperties = new HashSet<>();
		entityProperties.add(new EntityProperty() {
			public String getName() {
				return LABEL;
			}

			public DataType getDataType() {
				return KDataType.String;
			}
		});
		entityProperties.add(new EntityProperty() {
			public String getName() {
				return SIZE;
			}

			public DataType getDataType() {
				return KDataType.String;
			}
		});
		MAIN = new XPropertyEntryRule(entityProperties);
	}

	@Test
	public void test() throws NotFoundException {
		final Parser<XPropertyEntry> parser = MAIN.createParser();
		//---
		final String text = "label   : \"BLeU\"; non reconnu";
		final int end = parser.parse(text, 0);
		final XPropertyEntry propertyEntry = parser.get();
		Assert.assertEquals(LABEL, propertyEntry.getProperty().getName());
		Assert.assertEquals("BLeU", propertyEntry.getPropertyValueAsString());
		Assert.assertEquals(text.length() - " non reconnu".length(), end); //On vérfifie que le pointeur a avancé jusqu'à 'non reconnu'

	}

	@Test
	public void test2() throws NotFoundException {
		final Parser<XPropertyEntry> parser = MAIN.createParser();
		//---
		final String text = "label  :    \" vert \"";
		final int end = parser.parse(text, 0); //On ne met pas de séparateur final et on met un espace
		final XPropertyEntry propertyEntry = parser.get();
		Assert.assertEquals(LABEL, propertyEntry.getProperty().getName());
		Assert.assertEquals(" vert ", propertyEntry.getPropertyValueAsString()); //l'espace doit être conservé
		Assert.assertEquals(text.length(), end);
	}

	@Test
	public void test3() throws NotFoundException {
		final Parser<XPropertyEntry> parser = MAIN.createParser();
		//---
		final String text = "size   : \"54\";";
		final int end = parser.parse(text, 0);
		final XPropertyEntry propertyEntry = parser.get();
		Assert.assertEquals(SIZE, propertyEntry.getProperty().getName());
		Assert.assertEquals("54", propertyEntry.getPropertyValueAsString());
		Assert.assertEquals(text.length(), end);
	}

	@Test(expected = NotFoundException.class)
	public void testFail() throws NotFoundException {
		final Parser<XPropertyEntry> parser = MAIN.createParser();
		//---
		final String text = "maxlength   : \"54\";"; //La propriété maxlength n'est pas enregistrée
		/*final int end = */parser.parse(text, 0);
		Assert.fail();
	}

	@Test(expected = NotFoundException.class)
	public void testFail2() throws NotFoundException {
		final Parser<XPropertyEntry> parser = MAIN.createParser();
		//---
		final String text = "label  :    vert \"";
		parser.parse(text, 0); //On omet la quote de début
		Assert.fail();
	}

}
