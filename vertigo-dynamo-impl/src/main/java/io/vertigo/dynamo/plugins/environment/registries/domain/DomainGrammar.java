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

import io.vertigo.core.impl.environment.KernelGrammar;
import io.vertigo.core.impl.environment.kernel.meta.Entity;
import io.vertigo.core.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.core.impl.environment.kernel.meta.Grammar;
import io.vertigo.dynamo.plugins.environment.KspProperty;

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

	public static final Grammar GRAMMAR;

	static {
		CONSTAINT_ENTITY = new EntityBuilder("Constraint")
				.addProperty(KspProperty.CLASS_NAME, true)
				.addProperty(KspProperty.ARGS, false)
				.addProperty(KspProperty.MSG, false)
				.build();
		FORMATTER_ENTITY = new EntityBuilder("Formatter")
				.addProperty(KspProperty.CLASS_NAME, true)
				.addProperty(KspProperty.ARGS, false)
				.build();
		PROPERTY_ENTITY = new EntityBuilder("Property").build();

		DOMAIN_ENTITY = new EntityBuilder("Domain")
				.addProperty(KspProperty.MAX_LENGTH, false)
				.addProperty(KspProperty.TYPE, false)
				.addProperty(KspProperty.UNIT, false)
				.addProperty(KspProperty.INDEX_TYPE, false)
				.addProperty(KspProperty.STORE_TYPE, false)
				.addAttribute("formatter", FORMATTER_ENTITY, true)
				.addAttribute("dataType", KernelGrammar.getDataTypeEntity(), true)
				.addAttributes("constraint", CONSTAINT_ENTITY, false)
				.build();

		DT_FIELD_ENTITY = new EntityBuilder(DT_FIELD_META_DEFINITION)
				.addProperty(KspProperty.LABEL, true)
				.addProperty(KspProperty.NOT_NULL, true)
				.addAttribute("domain", DOMAIN_ENTITY, true)
				.addProperty(KspProperty.PERSISTENT, false)
				.build();

		FT_COMPUTED_FIELD_ENTITY = new EntityBuilder(DT_COMPUTED_FIELD_META_DEFINITION)
				.addProperty(KspProperty.LABEL, true)
				.addAttribute("domain", DOMAIN_ENTITY, true)
				.addProperty(KspProperty.EXPRESSION, true)
				.build();

		DT_DEFINITION_ENTITY = new EntityBuilder(DT_DEFINITION_META_DEFINITION)
				.addProperty(KspProperty.DISPLAY_FIELD, false)
				.addProperty(KspProperty.SORT_FIELD, false)
				.addAttributes(FIELD, DT_FIELD_ENTITY, false)// facultative
				.addAttributes(COMPUTED, FT_COMPUTED_FIELD_ENTITY, false) //facultative
				.addAttribute(PRIMARY_KEY, DT_FIELD_ENTITY, false) // facultative
				.addProperty(KspProperty.PERSISTENT, false)
				.addProperty(KspProperty.DYNAMIC, false)
				.addProperty(KspProperty.STEREOTYPE, false)
				//DT_DEFINITION.addMetaDefinitionReference("extends", DT_DEFINITION, true, false);
				.build();

		ASSOCIATION_ENTITY = new EntityBuilder(ASSOCIATION_META_DEFINITION)
				.addProperty(KspProperty.FK_FIELD_NAME, false)
				.addProperty(KspProperty.MULTIPLICITY_A, true)
				.addProperty(KspProperty.NAVIGABILITY_A, true)
				.addProperty(KspProperty.ROLE_A, true)
				.addProperty(KspProperty.LABEL_A, true)
				.addProperty(KspProperty.MULTIPLICITY_B, true)
				.addProperty(KspProperty.NAVIGABILITY_B, true)
				.addProperty(KspProperty.ROLE_B, true)
				.addProperty(KspProperty.LABEL_B, true)
				.addAttribute("dtDefinitionA", DT_DEFINITION_ENTITY, true)
				.addAttribute("dtDefinitionB", DT_DEFINITION_ENTITY, true)
				.build();

		ASSOCIATION_NN_ENTITY = new EntityBuilder(ASSOCIATION_NN_META_DEFINITION)
				.addProperty(KspProperty.TABLE_NAME, true)
				.addProperty(KspProperty.NAVIGABILITY_A, true)
				.addProperty(KspProperty.ROLE_A, true)
				.addProperty(KspProperty.LABEL_A, true)
				.addProperty(KspProperty.NAVIGABILITY_B, true)
				.addProperty(KspProperty.ROLE_B, true)
				.addProperty(KspProperty.LABEL_B, true)
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
}
