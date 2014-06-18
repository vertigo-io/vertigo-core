package io.vertigo.dynamo.domain;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinitionBuilder;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamox.domain.formatter.FormatterDefault;

import org.junit.Assert;
import org.junit.Test;

public class DomainManagerTest {

	@Test
	public void createDtDefinitionTest() {
		final Formatter formatter = new FormatterDefault("FMT_DEF");
		final Domain domain = new Domain("DO_NAME", DataType.String, formatter);

		//@formatter:off
		final DtDefinition dtDefinition = new DtDefinitionBuilder("DT_MOVIE")
		.withPersistent(false)
		.withDynamic(true)
		.withDataField("NAME", "nom du film", domain, true, true, false, false)
		.build();
		//@formatter:on

		final DtObject dto = DtObjectUtil.createDtObject(dtDefinition);
		dtDefinition.getField("NAME").getDataAccessor().setValue(dto, "dupond");

		Assert.assertEquals("dupond", dtDefinition.getField("NAME").getDataAccessor().getValue(dto));
	}
}
