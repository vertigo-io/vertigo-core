package io.vertigo.core.environment;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.config.LogConfig;
import io.vertigo.core.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.core.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.core.impl.environment.kernel.model.DynamicDefinitionKey;

import org.junit.Assert;
import org.junit.Test;

public class EnvironmentManagerTest extends AbstractTestCaseJU4 {

	@Override
	protected AppConfig buildAppConfig() {
		return new AppConfigBuilder()
				.beginBoot().withLogConfig(new LogConfig("/log4j.xml")).endBoot()
				.build();
	}

	private final DynamicDefinitionRepository dynamicDefinitionRepository = DslDynamicRegistryMock.createDynamicDefinitionRepository();

	@Test
	public void simpleTest() {

		final DynamicDefinition address1Definition = DynamicDefinitionRepository.createDynamicDefinitionBuilder("MAIN_ADDRESS", PersonnGrammar.ADDRESS_ENTITY, "io.vertigo.test.model")
				.addPropertyValue(PersonnGrammar.STREET, "1, rue du louvre")
				.addPropertyValue(PersonnGrammar.POSTAL_CODE, "75008")
				.addPropertyValue(PersonnGrammar.CITY, "Paris")
				.build();
		dynamicDefinitionRepository.addDefinition(address1Definition);

		final DynamicDefinition address2Definition = DynamicDefinitionRepository.createDynamicDefinitionBuilder("SECOND_ADDRESS", PersonnGrammar.ADDRESS_ENTITY, "io.vertigo.test.model")
				.addPropertyValue(PersonnGrammar.STREET, "105, rue martin")
				.addPropertyValue(PersonnGrammar.POSTAL_CODE, "75008")
				.addPropertyValue(PersonnGrammar.CITY, "Paris CEDEX")
				.build();
		dynamicDefinitionRepository.addDefinition(address2Definition);

		final DynamicDefinition personnDefinition = DynamicDefinitionRepository.createDynamicDefinitionBuilder("MISTER_BEAN", PersonnGrammar.PERSONN_ENTITY, "io.vertigo.test.model")
				.addPropertyValue(PersonnGrammar.NAME, "105, rue martin")
				.addPropertyValue(PersonnGrammar.FIRST_NAME, "75008")
				.addPropertyValue(PersonnGrammar.AGE, 42)
				.addPropertyValue(PersonnGrammar.HEIGHT, 175.0d)
				.addPropertyValue(PersonnGrammar.MALE, Boolean.TRUE)
				.addDefinition(PersonnGrammar.MAIN_ADDRESS, new DynamicDefinitionKey("MAIN_ADDRESS"))
				.addDefinition(PersonnGrammar.SECOND_ADDRESS, new DynamicDefinitionKey("SECOND_ADDRESS"))
				.build();
		dynamicDefinitionRepository.addDefinition(personnDefinition);

		dynamicDefinitionRepository.solve();
		Assert.assertNotNull(personnDefinition);
	}
}
