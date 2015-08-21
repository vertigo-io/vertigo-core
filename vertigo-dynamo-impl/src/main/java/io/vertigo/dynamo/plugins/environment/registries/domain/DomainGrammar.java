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
package io.vertigo.dynamo.plugins.environment.registries.domain;

import static io.vertigo.core.dsl.entity.EntityPropertyType.Boolean;
import static io.vertigo.core.dsl.entity.EntityPropertyType.Integer;
import static io.vertigo.core.dsl.entity.EntityPropertyType.String;
import static io.vertigo.dynamo.plugins.environment.KspProperty.ARGS;
import static io.vertigo.dynamo.plugins.environment.KspProperty.CLASS_NAME;
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
import io.vertigo.core.dsl.entity.Entity;
import io.vertigo.core.dsl.entity.EntityBuilder;
import io.vertigo.core.dsl.entity.EntityGrammar;
import io.vertigo.core.impl.environment.KernelGrammar;

/**
 * @author pchretien
 */
public final class DomainGrammar {
	/**
	 * Clé des FIELD_DEFINITION de type PK utilisés dans les DT_DEFINITION.
	 */
	public static final String PRIMARY_KEY = "key";
	/**
	 * Clé des FIELD_DEFINITION de type FIELD utilisés dans les DT_DEFINITION.
	 */
	public static final String FIELD = "field";
	/**
	 * Clé des FIELD_DEFINITION de type COMPUTED utilisés dans les DT_DEFINITION.
	 */
	public static final String COMPUTED = "computed";

	/** Mot-clé des MetaDefinitions de Fields. */
	static final String DT_FIELD_META_DEFINITION = "Field";
	/** Mot-clé des MetaDefinitions de Fields. */
	private static final String DT_COMPUTED_FIELD_META_DEFINITION = "ComputedField";
	/** Mot-clé des MetaDefinitions de AssociationNN. */
	static final String ASSOCIATION_NN_META_DEFINITION = "AssociationNN";
	/** Mot-clé des MetaDefinitions de Association. */
	private static final String ASSOCIATION_META_DEFINITION = "Association";
	/** Mot-clé des MetaDefinitions de DtDefinition. */
	private static final String DT_DEFINITION_META_DEFINITION = "DtDefinition";

	/**Définition d'une constraint.*/
	static final Entity CONSTAINT_ENTITY;
	/**Définition d'un formatter.*/
	static final Entity FORMATTER_ENTITY;
	/**Définition d'une propriété.*/
	private static final Entity PROPERTY_ENTITY;
	/**Définition d'un domain.*/
	public static final Entity DOMAIN_ENTITY;

	/**Définition d'un champ de DT.*/
	public static final Entity DT_FIELD_ENTITY;
	/**Définition d'un champ computed de DT.*/
	private static final Entity FT_COMPUTED_FIELD_ENTITY;
	/**Définition d'un DT.*/
	public static final Entity DT_DEFINITION_ENTITY;
	/**Définition d'une association simple.*/
	public static final Entity ASSOCIATION_ENTITY;
	/**Définition d'une association NN.*/
	public static final Entity ASSOCIATION_NN_ENTITY;

	/** Domain Grammar instance. */
	public static final EntityGrammar GRAMMAR;

	static {
		CONSTAINT_ENTITY = new EntityBuilder("Constraint")
				.addField(CLASS_NAME, String, true)
				.addField(ARGS, String, false)
				.addField(MSG, String, false)
				.build();
		FORMATTER_ENTITY = new EntityBuilder("Formatter")
				.addField(CLASS_NAME, String, true)
				.addField(ARGS, String, false)
				.build();
		PROPERTY_ENTITY = new EntityBuilder("Property").build();

		DOMAIN_ENTITY = new EntityBuilder("Domain")
				.addField(MAX_LENGTH, Integer, false)
				.addField(TYPE, String, false)
				.addField(UNIT, String, false)
				.addField(INDEX_TYPE, String, false)
				.addField(STORE_TYPE, String, false)
				.addField("formatter", FORMATTER_ENTITY, true)
				.addField("dataType", KernelGrammar.getDataTypeEntity(), true)
				.addFields("constraint", CONSTAINT_ENTITY, false)
				.build();

		DT_FIELD_ENTITY = new EntityBuilder(DT_FIELD_META_DEFINITION)
				.addField(LABEL, String, true)
				.addField(NOT_NULL, Boolean, true)
				.addField("domain", DOMAIN_ENTITY, true)
				.addField(PERSISTENT, Boolean, false)
				.build();

		FT_COMPUTED_FIELD_ENTITY = new EntityBuilder(DT_COMPUTED_FIELD_META_DEFINITION)
				.addField(LABEL, String, true)
				.addField("domain", DOMAIN_ENTITY, true)
				.addField(EXPRESSION, String, true)
				.build();

		DT_DEFINITION_ENTITY = new EntityBuilder(DT_DEFINITION_META_DEFINITION)
				.addField(DISPLAY_FIELD, String, false)
				.addField(SORT_FIELD, String, false)
				.addFields(FIELD, DT_FIELD_ENTITY, false)// facultative
				.addFields(COMPUTED, FT_COMPUTED_FIELD_ENTITY, false) //facultative
				.addField(PRIMARY_KEY, DT_FIELD_ENTITY, false) // facultative
				.addField(PERSISTENT, Boolean, false)
				.addField(DYNAMIC, Boolean, false)
				.addField(STEREOTYPE, String, false)
				//DT_DEFINITION.addMetaDefinitionReference("extends", DT_DEFINITION, true, false);
				.build();

		ASSOCIATION_ENTITY = new EntityBuilder(ASSOCIATION_META_DEFINITION)
				.addField(FK_FIELD_NAME, String, false)
				.addField(MULTIPLICITY_A, String, true)
				.addField(NAVIGABILITY_A, Boolean, true)
				.addField(ROLE_A, String, true)
				.addField(LABEL_A, String, true)
				.addField(MULTIPLICITY_B, String, true)
				.addField(NAVIGABILITY_B, Boolean, true)
				.addField(ROLE_B, String, true)
				.addField(LABEL_B, String, true)
				.addField("dtDefinitionA", DT_DEFINITION_ENTITY, true)
				.addField("dtDefinitionB", DT_DEFINITION_ENTITY, true)
				.build();

		ASSOCIATION_NN_ENTITY = new EntityBuilder(ASSOCIATION_NN_META_DEFINITION)
				.addField(TABLE_NAME, String, true)
				.addField(NAVIGABILITY_A, Boolean, true)
				.addField(ROLE_A, String, true)
				.addField(LABEL_A, String, true)
				.addField(NAVIGABILITY_B, Boolean, true)
				.addField(ROLE_B, String, true)
				.addField(LABEL_B, String, true)
				.addField("dtDefinitionA", DT_DEFINITION_ENTITY, true)
				.addField("dtDefinitionB", DT_DEFINITION_ENTITY, true)
				.build();

		GRAMMAR = new EntityGrammar(
				PROPERTY_ENTITY,
				CONSTAINT_ENTITY,
				FORMATTER_ENTITY,
				//---
				DOMAIN_ENTITY,
				DT_FIELD_ENTITY,
				FT_COMPUTED_FIELD_ENTITY,
				DT_DEFINITION_ENTITY,
				ASSOCIATION_ENTITY,
				ASSOCIATION_NN_ENTITY
				);
	}

	private DomainGrammar() {
		//private
	}
}
