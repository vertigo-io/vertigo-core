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

import io.vertigo.dynamo.impl.environment.KernelGrammar;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.Grammar;
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
		CONSTAINT_ENTITY = new EntityBuilder("Constraint")//
		.withProperty(KspProperty.CLASS_NAME, true)//
		.withProperty(KspProperty.ARGS, false)//
		.withProperty(KspProperty.MSG, false)//
		.build();
		FORMATTER_ENTITY = new EntityBuilder("Formatter")//
		.withProperty(KspProperty.CLASS_NAME, true)//
		.withProperty(KspProperty.ARGS, false)//
		.build();
		PROPERTY_ENTITY = new EntityBuilder("Property").build();

		DOMAIN_ENTITY = new EntityBuilder("Domain")//
		.withProperty(KspProperty.MAX_LENGTH, false)//
		.withProperty(KspProperty.TYPE, false)//
		.withProperty(KspProperty.UNIT, false)//
		.withProperty(KspProperty.INDEX_TYPE, false)//
		.withProperty(KspProperty.STORE_TYPE, false)//
		.withAttribute("formatter", FORMATTER_ENTITY, false, true)//
		.withAttribute("dataType", KernelGrammar.getDataTypeEntity(), false, true)//
		.withAttribute("constraint", CONSTAINT_ENTITY, true, false)//
		.build();

		DT_FIELD_ENTITY = new EntityBuilder(DT_FIELD_META_DEFINITION)//
		.withProperty(KspProperty.LABEL, true)//
		.withProperty(KspProperty.NOT_NULL, true)//
		.withAttribute("domain", DOMAIN_ENTITY, false, true)//
		.withProperty(KspProperty.PERSISTENT, false)//
		.build();

		FT_COMPUTED_FIELD_ENTITY = new EntityBuilder(DT_COMPUTED_FIELD_META_DEFINITION)//
		.withProperty(KspProperty.LABEL, true)//
		.withAttribute("domain", DOMAIN_ENTITY, false, true)//
		.withProperty(KspProperty.EXPRESSION, true)//
		.build();

		DT_DEFINITION_ENTITY = new EntityBuilder(DT_DEFINITION_META_DEFINITION)//
		.withProperty(KspProperty.DISPLAY_FIELD, false)//
		.withProperty(KspProperty.SORT_FIELD, false)//
		.withAttribute(FIELD, DT_FIELD_ENTITY, true, false)//Multiple, facultative
		.withAttribute(COMPUTED, FT_COMPUTED_FIELD_ENTITY, true, false) //Multiple, facultative
		.withAttribute(PRIMARY_KEY, DT_FIELD_ENTITY, false, false) //Simple, facultative
		.withProperty(KspProperty.PERSISTENT, false)//
		.withProperty(KspProperty.DYNAMIC, false)//
		//DT_DEFINITION.addMetaDefinitionReference("extends", DT_DEFINITION, true, false);
		.build();

		ASSOCIATION_ENTITY = new EntityBuilder(ASSOCIATION_META_DEFINITION)//
		.withProperty(KspProperty.FK_FIELD_NAME, false)//
		.withProperty(KspProperty.MULTIPLICITY_A, true)//
		.withProperty(KspProperty.NAVIGABILITY_A, true)//
		.withProperty(KspProperty.ROLE_A, true)//
		.withProperty(KspProperty.LABEL_A, true)//
		.withProperty(KspProperty.MULTIPLICITY_B, true)//
		.withProperty(KspProperty.NAVIGABILITY_B, true)//
		.withProperty(KspProperty.ROLE_B, true)//
		.withProperty(KspProperty.LABEL_B, true)//
		.withAttribute("dtDefinitionA", DT_DEFINITION_ENTITY, false, true)//
		.withAttribute("dtDefinitionB", DT_DEFINITION_ENTITY, false, true)//
		.build();

		ASSOCIATION_NN_ENTITY = new EntityBuilder(ASSOCIATION_NN_META_DEFINITION)//
		.withProperty(KspProperty.TABLE_NAME, true)//
		.withProperty(KspProperty.NAVIGABILITY_A, true)//
		.withProperty(KspProperty.ROLE_A, true)//
		.withProperty(KspProperty.LABEL_A, true)//
		.withProperty(KspProperty.NAVIGABILITY_B, true)//
		.withProperty(KspProperty.ROLE_B, true)//
		.withProperty(KspProperty.LABEL_B, true)//
		.withAttribute("dtDefinitionA", DT_DEFINITION_ENTITY, false, true)//
		.withAttribute("dtDefinitionB", DT_DEFINITION_ENTITY, false, true)//s
		.build();

		GRAMMAR = new Grammar(//
				PROPERTY_ENTITY, //
				CONSTAINT_ENTITY, //
				FORMATTER_ENTITY, //
				//---
				DOMAIN_ENTITY,//
				DT_FIELD_ENTITY,//
				FT_COMPUTED_FIELD_ENTITY,//
				DT_DEFINITION_ENTITY,//
				ASSOCIATION_ENTITY,//
				ASSOCIATION_NN_ENTITY//
				);
	}
}
