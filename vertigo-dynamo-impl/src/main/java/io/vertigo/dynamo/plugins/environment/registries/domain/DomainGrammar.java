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
import static io.vertigo.dynamo.plugins.environment.KspProperty.FRAGMENT_OF;
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
	/**Définition d'un domain.*/
	public static final DslEntity DOMAIN_ENTITY;

	/**Field*/
	public static final DslEntity DT_FIELD_ENTITY;

	public static final DslEntity DT_DEFINITION_ENTITY;
	/**Définition d'une association simple.*/
	public static final DslEntity ASSOCIATION_ENTITY;
	/**Définition d'une association NN.*/
	public static final DslEntity ASSOCIATION_NN_ENTITY;

	/**
	 * Fragments
	 */
	public static final DslEntity FRAGMENT_ENTITY;

	static {
		CONSTRAINT_ENTITY = new DslEntityBuilder("Constraint")
				.addRequiredField(CLASS_NAME, String)
				.addOptionalField(ARGS, String)
				.addOptionalField(MSG, String)
				.build();
		FORMATTER_ENTITY = new DslEntityBuilder("Formatter")
				.addRequiredField(CLASS_NAME, String)
				.addOptionalField(ARGS, String)
				.build();

		DOMAIN_ENTITY = new DslEntityBuilder("Domain")
				.addOptionalField(MAX_LENGTH, Integer)
				.addOptionalField(TYPE, String)
				.addOptionalField(UNIT, String)
				.addOptionalField(INDEX_TYPE, String)
				.addOptionalField(STORE_TYPE, String)
				.addRequiredField("formatter", FORMATTER_ENTITY.getLink())
				.addRequiredField("dataType", KernelGrammar.getDataTypeEntity().getLink())
				.addManyFields("constraint", CONSTRAINT_ENTITY.getLink())
				.build();

		DT_FIELD_ENTITY = new DslEntityBuilder("Field")
				.addRequiredField(LABEL, String)
				.addRequiredField(NOT_NULL, Boolean)
				.addRequiredField("domain", DOMAIN_ENTITY.getLink())
				.addOptionalField(PERSISTENT, Boolean)
				.build();

		final DslEntity computedFieldEntity = new DslEntityBuilder("ComputedField")
				.addRequiredField(LABEL, String)
				.addRequiredField("domain", DOMAIN_ENTITY.getLink())
				.addRequiredField(EXPRESSION, String)
				.build();

		DT_DEFINITION_ENTITY = new DslEntityBuilder("DtDefinition")
				.addOptionalField(DISPLAY_FIELD, String)
				.addOptionalField(SORT_FIELD, String)
				.addManyFields(FIELD, DT_FIELD_ENTITY)
				.addManyFields(COMPUTED, computedFieldEntity)
				.addOptionalField(ID, DT_FIELD_ENTITY)
				.addOptionalField(PERSISTENT, Boolean)
				.addOptionalField(DYNAMIC, Boolean)
				.addOptionalField(FRAGMENT_OF, String)
				.addOptionalField(STEREOTYPE, String)
				.addOptionalField(DATA_SPACE, String)
				.build();

		final DslEntity fieldAliasEntity = new DslEntityBuilder("fieldAlias")
				.addOptionalField(LABEL, String)
				.addOptionalField(NOT_NULL, Boolean)
				.build();

		FRAGMENT_ENTITY = new DslEntityBuilder("Fragment")
				.addRequiredField("from", DT_DEFINITION_ENTITY.getLink())
				.addManyFields("alias", fieldAliasEntity) //on peut ajouter des champs
				.addOptionalField(DISPLAY_FIELD, String)
				.addOptionalField(SORT_FIELD, String)
				.addManyFields(FIELD, DT_FIELD_ENTITY) //on peut ajouter des champs
				.addManyFields(COMPUTED, computedFieldEntity) //et des computed
				.build();

		ASSOCIATION_ENTITY = new DslEntityBuilder("Association")
				.addOptionalField(FK_FIELD_NAME, String)
				.addRequiredField(MULTIPLICITY_A, String)
				.addRequiredField(NAVIGABILITY_A, Boolean)
				.addRequiredField(ROLE_A, String)
				.addRequiredField(LABEL_A, String)
				.addRequiredField(MULTIPLICITY_B, String)
				.addRequiredField(NAVIGABILITY_B, Boolean)
				.addRequiredField(ROLE_B, String)
				.addRequiredField(LABEL_B, String)
				.addRequiredField("dtDefinitionA", DT_DEFINITION_ENTITY.getLink())
				.addRequiredField("dtDefinitionB", DT_DEFINITION_ENTITY.getLink())
				.build();

		ASSOCIATION_NN_ENTITY = new DslEntityBuilder("AssociationNN")
				.addRequiredField(TABLE_NAME, String)
				.addRequiredField(NAVIGABILITY_A, Boolean)
				.addRequiredField(ROLE_A, String)
				.addRequiredField(LABEL_A, String)
				.addRequiredField(NAVIGABILITY_B, Boolean)
				.addRequiredField(ROLE_B, String)
				.addRequiredField(LABEL_B, String)
				.addRequiredField("dtDefinitionA", DT_DEFINITION_ENTITY.getLink())
				.addRequiredField("dtDefinitionB", DT_DEFINITION_ENTITY.getLink())
				.build();

	}

	@Override
	public List<DslEntity> getEntities() {
		return new ListBuilder<DslEntity>()
				.add(CONSTRAINT_ENTITY)
				.add(FORMATTER_ENTITY)
				//---
				.add(DOMAIN_ENTITY)
				.add(FRAGMENT_ENTITY)
				.add(DT_DEFINITION_ENTITY)
				.add(ASSOCIATION_ENTITY)
				.add(ASSOCIATION_NN_ENTITY)
				.unmodifiable()
				.build();
	}
}
