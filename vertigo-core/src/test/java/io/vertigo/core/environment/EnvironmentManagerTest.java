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
import io.vertigo.core.Home;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.config.LogConfig;
import io.vertigo.core.dsl.dynamic.DynamicDefinition;
import io.vertigo.core.dsl.dynamic.DynamicDefinitionRepository;

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
				.addDefinition(MAIN_ADDRESS, "MAIN_ADDRESS")
				.addDefinition(PersonGrammar.SECOND_ADDRESS, "SECOND_ADDRESS")
				.build();
		dynamicDefinitionRepository.addDefinition(personDefinition);

		dynamicDefinitionRepository.solve(Home.getDefinitionSpace());
		Assert.assertNotNull(personDefinition);
	}
}
