package io.vertigo.core.environment;

import static io.vertigo.core.environment.PersonGrammar.AGE;
import static io.vertigo.core.environment.PersonGrammar.CITY;
import static io.vertigo.core.environment.PersonGrammar.FIRST_NAME;
import static io.vertigo.core.environment.PersonGrammar.HEIGHT;
import static io.vertigo.core.environment.PersonGrammar.MAIN_ADDRESS;
import static io.vertigo.core.environment.PersonGrammar.MALE;
import static io.vertigo.core.environment.PersonGrammar.NAME;
import static io.vertigo.core.environment.PersonGrammar.POSTAL_CODE;
import static io.vertigo.core.environment.PersonGrammar.STREET;
import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.config.LogConfig;
import io.vertigo.core.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.core.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.core.impl.environment.kernel.model.DynamicDefinitionKey;

import org.junit.Assert;
import org.junit.Test;

public final class EnvironmentManagerTest extends AbstractTestCaseJU4 {

	@Override
	protected AppConfig buildAppConfig() {
		return new AppConfigBuilder()
				.beginBoot().withLogConfig(new LogConfig("/log4j.xml")).endBoot()
				.build();
	}

	private final DynamicDefinitionRepository dynamicDefinitionRepository = DslDynamicRegistryMock.createDynamicDefinitionRepository();

	@Test
	public void simpleTest() {

		final DynamicDefinition address1Definition = DynamicDefinitionRepository.createDynamicDefinitionBuilder("MAIN_ADDRESS", PersonGrammar.ADDRESS_ENTITY, "io.vertigo.test.model")
				.addPropertyValue(STREET, "1, rue du louvre")
				.addPropertyValue(POSTAL_CODE, "75008")
				.addPropertyValue(CITY, "Paris")
				.build();
		dynamicDefinitionRepository.addDefinition(address1Definition);

		final DynamicDefinition address2Definition = DynamicDefinitionRepository.createDynamicDefinitionBuilder("SECOND_ADDRESS", PersonGrammar.ADDRESS_ENTITY, "io.vertigo.test.model")
				.addPropertyValue(STREET, "105, rue martin")
				.addPropertyValue(POSTAL_CODE, "75008")
				.addPropertyValue(CITY, "Paris CEDEX")
				.build();
		dynamicDefinitionRepository.addDefinition(address2Definition);

		final DynamicDefinition personDefinition = DynamicDefinitionRepository.createDynamicDefinitionBuilder("MISTER_BEAN", PersonGrammar.PERSON_ENTITY, "io.vertigo.test.model")
				.addPropertyValue(NAME, "105, rue martin")
				.addPropertyValue(FIRST_NAME, "75008")
				.addPropertyValue(AGE, 42)
				.addPropertyValue(HEIGHT, 175.0d)
				.addPropertyValue(MALE, Boolean.TRUE)
				.addDefinition(MAIN_ADDRESS, new DynamicDefinitionKey("MAIN_ADDRESS"))
				.addDefinition(PersonGrammar.SECOND_ADDRESS, new DynamicDefinitionKey("SECOND_ADDRESS"))
				.build();
		dynamicDefinitionRepository.addDefinition(personDefinition);

		dynamicDefinitionRepository.solve();
		Assert.assertNotNull(personDefinition);
	}
}
