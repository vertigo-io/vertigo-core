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
import io.vertigo.core.impl.environment.KernelGrammar;
import io.vertigo.core.impl.environment.kernel.meta.Entity;
import io.vertigo.core.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.core.impl.environment.kernel.meta.EntityPropertyType;
import io.vertigo.core.impl.environment.kernel.meta.Grammar;

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
	public static final Grammar GRAMMAR;

	static {
		CONSTAINT_ENTITY = new EntityBuilder("Constraint")
				.addProperty(CLASS_NAME, EntityPropertyType.String, true)
				.addProperty(ARGS, EntityPropertyType.String, false)
				.addProperty(MSG, EntityPropertyType.String, false)
				.build();
		FORMATTER_ENTITY = new EntityBuilder("Formatter")
				.addProperty(CLASS_NAME, EntityPropertyType.String, true)
				.addProperty(ARGS, EntityPropertyType.String, false)
				.build();
		PROPERTY_ENTITY = new EntityBuilder("Property").build();

		DOMAIN_ENTITY = new EntityBuilder("Domain")
				.addProperty(MAX_LENGTH, EntityPropertyType.Integer, false)
				.addProperty(TYPE, EntityPropertyType.String, false)
				.addProperty(UNIT, EntityPropertyType.String, false)
				.addProperty(INDEX_TYPE, EntityPropertyType.String, false)
				.addProperty(STORE_TYPE, EntityPropertyType.String, false)
				.addAttribute("formatter", FORMATTER_ENTITY, true)
				.addAttribute("dataType", KernelGrammar.getDataTypeEntity(), true)
				.addAttributes("constraint", CONSTAINT_ENTITY, false)
				.build();

		DT_FIELD_ENTITY = new EntityBuilder(DT_FIELD_META_DEFINITION)
				.addProperty(LABEL, EntityPropertyType.String, true)
				.addProperty(NOT_NULL, EntityPropertyType.Boolean, true)
				.addAttribute("domain", DOMAIN_ENTITY, true)
				.addProperty(PERSISTENT, EntityPropertyType.Boolean, false)
				.build();

		FT_COMPUTED_FIELD_ENTITY = new EntityBuilder(DT_COMPUTED_FIELD_META_DEFINITION)
				.addProperty(LABEL, EntityPropertyType.String, true)
				.addAttribute("domain", DOMAIN_ENTITY, true)
				.addProperty(EXPRESSION, EntityPropertyType.String, true)
				.build();

		DT_DEFINITION_ENTITY = new EntityBuilder(DT_DEFINITION_META_DEFINITION)
				.addProperty(DISPLAY_FIELD, EntityPropertyType.String, false)
				.addProperty(SORT_FIELD, EntityPropertyType.String, false)
				.addAttributes(FIELD, DT_FIELD_ENTITY, false)// facultative
				.addAttributes(COMPUTED, FT_COMPUTED_FIELD_ENTITY, false) //facultative
				.addAttribute(PRIMARY_KEY, DT_FIELD_ENTITY, false) // facultative
				.addProperty(PERSISTENT, EntityPropertyType.Boolean, false)
				.addProperty(DYNAMIC, EntityPropertyType.Boolean, false)
				.addProperty(STEREOTYPE, EntityPropertyType.String, false)
				//DT_DEFINITION.addMetaDefinitionReference("extends", DT_DEFINITION, true, false);
				.build();

		ASSOCIATION_ENTITY = new EntityBuilder(ASSOCIATION_META_DEFINITION)
				.addProperty(FK_FIELD_NAME, EntityPropertyType.String, false)
				.addProperty(MULTIPLICITY_A, EntityPropertyType.String, true)
				.addProperty(NAVIGABILITY_A, EntityPropertyType.Boolean, true)
				.addProperty(ROLE_A, EntityPropertyType.String, true)
				.addProperty(LABEL_A, EntityPropertyType.String, true)
				.addProperty(MULTIPLICITY_B, EntityPropertyType.String, true)
				.addProperty(NAVIGABILITY_B, EntityPropertyType.Boolean, true)
				.addProperty(ROLE_B, EntityPropertyType.String, true)
				.addProperty(LABEL_B, EntityPropertyType.String, true)
				.addAttribute("dtDefinitionA", DT_DEFINITION_ENTITY, true)
				.addAttribute("dtDefinitionB", DT_DEFINITION_ENTITY, true)
				.build();

		ASSOCIATION_NN_ENTITY = new EntityBuilder(ASSOCIATION_NN_META_DEFINITION)
				.addProperty(TABLE_NAME, EntityPropertyType.String, true)
				.addProperty(NAVIGABILITY_A, EntityPropertyType.Boolean, true)
				.addProperty(ROLE_A, EntityPropertyType.String, true)
				.addProperty(LABEL_A, EntityPropertyType.String, true)
				.addProperty(NAVIGABILITY_B, EntityPropertyType.Boolean, true)
				.addProperty(ROLE_B, EntityPropertyType.String, true)
				.addProperty(LABEL_B, EntityPropertyType.String, true)
				.addAttribute("dtDefinitionA", DT_DEFINITION_ENTITY, true)
				.addAttribute("dtDefinitionB", DT_DEFINITION_ENTITY, true)
				.build();

		GRAMMAR = new Grammar(
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
