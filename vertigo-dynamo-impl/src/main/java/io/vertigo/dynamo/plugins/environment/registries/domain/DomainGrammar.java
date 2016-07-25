/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.environment.registries.domain;

import static io.vertigo.core.definition.dsl.entity.DslPropertyType.Boolean;
import static io.vertigo.core.definition.dsl.entity.DslPropertyType.Integer;
import static io.vertigo.core.definition.dsl.entity.DslPropertyType.String;
import static io.vertigo.dynamo.plugins.environment.KspProperty.ARGS;
import static io.vertigo.dynamo.plugins.environment.KspProperty.CLASS_NAME;
import static io.vertigo.dynamo.plugins.environment.KspProperty.DATA_SPACE;
import static io.vertigo.dynamo.plugins.environment.KspProperty.DISPLAY_FIELD;
import static io.vertigo.dynamo.plugins.environment.KspProperty.DYNAMIC;
import static io.vertigo.dynamo.plugins.environment.KspProperty.EXPRESSION;
import static io.vertigo.dynamo.plugins.environment.KspProperty.FK_FIELD_NAME;
import static io.vertigo.dynamo.plugins.environment.KspProperty.INDEX_TYPE;
import static io.vertigo.dynamo.plugins.environment.KspProperty.LABEL;
import static io.vertigo.dynamo.plugins.environment.KspProperty.LABEL_A;
import static io.vertigo.dynamo.plugins.environment.KspProperty.LABEL_B;
import static io.vertigo.dynamo.plugins.environment.KspProperty.MAX_LENGTH;
import static io.vertigo.dynamo.plugins.environment.KspProperty.MSG;
import static io.vertigo.dynamo.plugins.environment.KspProperty.MULTIPLICITY_A;
import static io.vertigo.dynamo.plugins.environment.KspProperty.MULTIPLICITY_B;
import static io.vertigo.dynamo.plugins.environment.KspProperty.NAVIGABILITY_A;
import static io.vertigo.dynamo.plugins.environment.KspProperty.NAVIGABILITY_B;
import static io.vertigo.dynamo.plugins.environment.KspProperty.NOT_NULL;
import static io.vertigo.dynamo.plugins.environment.KspProperty.PERSISTENT;
import static io.vertigo.dynamo.plugins.environment.KspProperty.ROLE_A;
import static io.vertigo.dynamo.plugins.environment.KspProperty.ROLE_B;
import static io.vertigo.dynamo.plugins.environment.KspProperty.SORT_FIELD;
import static io.vertigo.dynamo.plugins.environment.KspProperty.STEREOTYPE;
import static io.vertigo.dynamo.plugins.environment.KspProperty.STORE_TYPE;
import static io.vertigo.dynamo.plugins.environment.KspProperty.TABLE_NAME;
import static io.vertigo.dynamo.plugins.environment.KspProperty.TYPE;
import static io.vertigo.dynamo.plugins.environment.KspProperty.UNIT;

import java.util.List;

import io.vertigo.core.definition.dsl.entity.DslEntity;
import io.vertigo.core.definition.dsl.entity.DslEntityBuilder;
import io.vertigo.core.definition.dsl.entity.DslGrammar;
import io.vertigo.core.definition.loader.KernelGrammar;
import io.vertigo.util.ListBuilder;

/**
 * @author pchretien
 */
public final class DomainGrammar implements DslGrammar {
	/**
	 * Clé des FIELD_DEFINITION de type PK utilisés dans les DT_DEFINITION.
	 */
	public static final String ID = "key";
	/**
	 * Clé des FIELD_DEFINITION de type FIELD utilisés dans les DT_DEFINITION.
	 */
	public static final String FIELD = "field";
	/**
	 * Clé des FIELD_DEFINITION de type COMPUTED utilisés dans les DT_DEFINITION.
	 */
	public static final String COMPUTED = "computed";

	/**Définition d'une constraint.*/
	public static final DslEntity CONSTRAINT_ENTITY;
	/**Définition d'un formatter.*/
	public static final DslEntity FORMATTER_ENTITY;
	/**Définition d'une propriété.*/
	private static final DslEntity PROPERTY_ENTITY;
	/**Définition d'un domain.*/
	public static final DslEntity DOMAIN_ENTITY;

	/**Définition d'un champ de DT.*/
	public static final DslEntity DT_FIELD_ENTITY;
	/**Définition d'un champ computed de DT.*/
	private static final DslEntity FT_COMPUTED_FIELD_ENTITY;
	/**Définition d'un DT.*/
	public static final DslEntity DT_DEFINITION_ENTITY;
	/**Définition d'une association simple.*/
	public static final DslEntity ASSOCIATION_ENTITY;
	/**Définition d'une association NN.*/
	public static final DslEntity ASSOCIATION_NN_ENTITY;

	static {
		CONSTRAINT_ENTITY = new DslEntityBuilder("Constraint")
				.addField(CLASS_NAME, String, true)
				.addField(ARGS, String, false)
				.addField(MSG, String, false)
				.build();
		FORMATTER_ENTITY = new DslEntityBuilder("Formatter")
				.addField(CLASS_NAME, String, true)
				.addField(ARGS, String, false)
				.build();
		PROPERTY_ENTITY = new DslEntityBuilder("Property").build();

		DOMAIN_ENTITY = new DslEntityBuilder("Domain")
				.addField(MAX_LENGTH, Integer, false)
				.addField(TYPE, String, false)
				.addField(UNIT, String, false)
				.addField(INDEX_TYPE, String, false)
				.addField(STORE_TYPE, String, false)
				.addField("formatter", FORMATTER_ENTITY.getLink(), true)
				.addField("dataType", KernelGrammar.getDataTypeEntity().getLink(), true)
				.addFields("constraint", CONSTRAINT_ENTITY.getLink(), false)
				.build();

		DT_FIELD_ENTITY = new DslEntityBuilder("Field")
				.addField(LABEL, String, true)
				.addField(NOT_NULL, Boolean, true)
				.addField("domain", DOMAIN_ENTITY.getLink(), true)
				.addField(PERSISTENT, Boolean, false)
				.build();

		FT_COMPUTED_FIELD_ENTITY = new DslEntityBuilder("ComputedField")
				.addField(LABEL, String, true)
				.addField("domain", DOMAIN_ENTITY.getLink(), true)
				.addField(EXPRESSION, String, true)
				.build();

		DT_DEFINITION_ENTITY = new DslEntityBuilder("DtDefinition")
				.addField(DISPLAY_FIELD, String, false)
				.addField(SORT_FIELD, String, false)
				.addFields(FIELD, DT_FIELD_ENTITY, false)
				.addFields(COMPUTED, FT_COMPUTED_FIELD_ENTITY, false)
				.addField(ID, DT_FIELD_ENTITY, false)
				.addField(PERSISTENT, Boolean, false)
				.addField(DYNAMIC, Boolean, false)
				.addField(STEREOTYPE, String, false)
				.addField(DATA_SPACE, String, false)
				.build();

		ASSOCIATION_ENTITY = new DslEntityBuilder("Association")
				.addField(FK_FIELD_NAME, String, false)
				.addField(MULTIPLICITY_A, String, true)
				.addField(NAVIGABILITY_A, Boolean, true)
				.addField(ROLE_A, String, true)
				.addField(LABEL_A, String, true)
				.addField(MULTIPLICITY_B, String, true)
				.addField(NAVIGABILITY_B, Boolean, true)
				.addField(ROLE_B, String, true)
				.addField(LABEL_B, String, true)
				.addField("dtDefinitionA", DT_DEFINITION_ENTITY.getLink(), true)
				.addField("dtDefinitionB", DT_DEFINITION_ENTITY.getLink(), true)
				.build();

		ASSOCIATION_NN_ENTITY = new DslEntityBuilder("AssociationNN")
				.addField(TABLE_NAME, String, true)
				.addField(NAVIGABILITY_A, Boolean, true)
				.addField(ROLE_A, String, true)
				.addField(LABEL_A, String, true)
				.addField(NAVIGABILITY_B, Boolean, true)
				.addField(ROLE_B, String, true)
				.addField(LABEL_B, String, true)
				.addField("dtDefinitionA", DT_DEFINITION_ENTITY.getLink(), true)
				.addField("dtDefinitionB", DT_DEFINITION_ENTITY.getLink(), true)
				.build();

	}

	@Override
	public List<DslEntity> getEntities() {
		return new ListBuilder<DslEntity>()
				.add(PROPERTY_ENTITY)
				.add(CONSTRAINT_ENTITY)
				.add(FORMATTER_ENTITY)
				//---
				.add(DOMAIN_ENTITY)
				.add(DT_FIELD_ENTITY)
				.add(FT_COMPUTED_FIELD_ENTITY)
				.add(DT_DEFINITION_ENTITY)
				.add(ASSOCIATION_ENTITY)
				.add(ASSOCIATION_NN_ENTITY)
				.unmodifiable()
				.build();
	}
}
