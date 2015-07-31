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

import io.vertigo.core.impl.environment.kernel.meta.Entity;
import io.vertigo.core.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.core.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.core.impl.environment.kernel.meta.EntityPropertyType;
import io.vertigo.core.impl.environment.kernel.meta.Grammar;

/**
 * @author npiedeloup
 */
public final class PersonnGrammar {

	static final EntityProperty NAME = new EntityProperty("name", EntityPropertyType.String);
	static final EntityProperty FIRST_NAME = new EntityProperty("firstName", EntityPropertyType.String);
	static final EntityProperty AGE = new EntityProperty("age", EntityPropertyType.Integer);
	static final EntityProperty HEIGHT = new EntityProperty("height", EntityPropertyType.Double);
	static final EntityProperty MALE = new EntityProperty("male", EntityPropertyType.Boolean);
	static final Entity PERSONN_ENTITY;

	static final String MAIN_ADDRESS = "mainAddress";
	static final String SECOND_ADDRESS = "secondaryAddress";
	static final EntityProperty STREET = new EntityProperty("street", EntityPropertyType.String);
	static final EntityProperty POSTAL_CODE = new EntityProperty("postalCode", EntityPropertyType.String);
	static final EntityProperty CITY = new EntityProperty("city", EntityPropertyType.String);
	static final Entity ADDRESS_ENTITY;

	/** Personn Grammar instance. */
	public static final Grammar GRAMMAR;

	static {
		ADDRESS_ENTITY = new EntityBuilder("address")
				.addProperty(STREET, true)
				.addProperty(POSTAL_CODE, false)
				.addProperty(CITY, false)
				.build();
		PERSONN_ENTITY = new EntityBuilder("personn")
				.addProperty(NAME, true)
				.addProperty(FIRST_NAME, true)
				.addProperty(AGE, false)
				.addProperty(HEIGHT, false)
				.addProperty(MALE, true)
				.addAttribute(MAIN_ADDRESS, ADDRESS_ENTITY, true)
				.addAttribute("secondaryAddress", ADDRESS_ENTITY, false)
				.build();

		GRAMMAR = new Grammar(
				ADDRESS_ENTITY,
				PERSONN_ENTITY
				);
	}

	private PersonnGrammar() {
		//private
	}
}
