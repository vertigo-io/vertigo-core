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
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;
import io.vertigo.dynamo.plugins.environment.KspProperty;

/**
 * @author pchretien
 */
public final class DomainGrammar extends GrammarProvider {
	public static final DomainGrammar INSTANCE = new DomainGrammar();

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
	private static final String DT_FIELD_META_DEFINITION = "Field";
	/** Mot-clé des MetaDefinitions de Fields. */
	private static final String DT_COMPUTED_FIELD_META_DEFINITION = "ComputedField";
	/** Mot-clé des MetaDefinitions de AssociationNN. */
	private static final String ASSOCIATION_NN_META_DEFINITION = "AssociationNN";
	/** Mot-clé des MetaDefinitions de Association. */
	private static final String ASSOCIATION_META_DEFINITION = "Association";
	/** Mot-clé des MetaDefinitions de DtDefinition. */
	private static final String DT_DEFINITION_META_DEFINITION = "DtDefinition";

	/**Définition d'une constraint.*/
	private final Entity constraintEntity;
	/**Définition d'un formatter.*/
	private final Entity formatterEntity;
	/**Définition d'un DT.*/
	private final Entity dtDefinitionEntity;
	/**Définition d'un domain.*/
	private final Entity domainEntity;
	/**Définition d'une association simple.*/
	private final Entity associationEntity;
	/**Définition d'une association NN.*/
	private final Entity associationNNEntity;

	/**Définition d'une propriété.*/
	private final Entity propertyEntity;
	//	//Fausses définitions ????????????????
	/**Définition d'un champ de DT.*/
	private final Entity dtFieldEntity;
	/**Définition d'un champ computed de DT.*/
	private final Entity dtComputedFieldEntity;

	/**
	 * Initialisation des métadonnées permettant de décrire le métamodèle de Dynamo.
	 */
	private DomainGrammar() {
		propertyEntity = createPropertyEntity();
		constraintEntity = createConstraintEntity();
		formatterEntity = createFormatterEntity();
		//---
		domainEntity = createDomainEntity(formatterEntity, constraintEntity);
		//---
		dtFieldEntity = createDtFieldEntity(domainEntity);
		dtComputedFieldEntity = createDtComputedFieldEntity(domainEntity);
		//---
		dtDefinitionEntity = createDtDefinitionEntity(dtFieldEntity, dtComputedFieldEntity);
		associationEntity = createAssociationEntity(dtDefinitionEntity);
		associationNNEntity = createAssociationNNEntity(dtDefinitionEntity);
		//---------------------------------------------------------------------
		getGrammar().registerEntity(constraintEntity);
		getGrammar().registerEntity(formatterEntity);
		getGrammar().registerEntity(domainEntity);
		getGrammar().registerEntity(dtDefinitionEntity);
		getGrammar().registerEntity(dtFieldEntity);
		getGrammar().registerEntity(dtComputedFieldEntity);
		getGrammar().registerEntity(associationEntity);
		getGrammar().registerEntity(associationNNEntity);
		getGrammar().registerEntity(propertyEntity);
	}

	private static Entity createDtFieldEntity(final Entity domainEntity) {
		return new EntityBuilder(DT_FIELD_META_DEFINITION)//
				.withProperty(KspProperty.LABEL, true)//
				.withProperty(KspProperty.NOT_NULL, true)//
				.withAttribute("domain", domainEntity, false, true)//
				.withProperty(KspProperty.PERSISTENT, false)//
				.build();
	}

	private static Entity createDtDefinitionEntity(final Entity dtFieldEntity, final Entity dtComputedFieldEntity) {
		return new EntityBuilder(DT_DEFINITION_META_DEFINITION)//
				.withProperty(KspProperty.DISPLAY_FIELD, false)//
				.withProperty(KspProperty.SORT_FIELD, false)//
				.withAttribute(FIELD, dtFieldEntity, true, false)//Multiple, facultative
				.withAttribute(COMPUTED, dtComputedFieldEntity, true, false) //Multiple, facultative
				.withAttribute(PRIMARY_KEY, dtFieldEntity, false, false) //Simple, facultative
				.withProperty(KspProperty.PERSISTENT, false)//
				.withProperty(KspProperty.DYNAMIC, false)//
				//DT_DEFINITION.addMetaDefinitionReference("extends", DT_DEFINITION, true, false);
				.build();
	}

	private static Entity createPropertyEntity() {
		final EntityBuilder builder = new EntityBuilder("Property");
		return builder.build();
	}

	private static Entity createFormatterEntity() {
		return new EntityBuilder("Formatter")//
				.withProperty(KspProperty.CLASS_NAME, true)//
				.withProperty(KspProperty.ARGS, false)//
				.build();
	}

	private static Entity createConstraintEntity() {
		return new EntityBuilder("Constraint")//
				.withProperty(KspProperty.CLASS_NAME, true)//
				.withProperty(KspProperty.ARGS, false)//
				.withProperty(KspProperty.MSG, false)//
				.build();
	}

	private static Entity createDomainEntity(final Entity formatterEntity, final Entity constraintEntity) {
		return new EntityBuilder("Domain")//
				.withProperty(KspProperty.MAX_LENGTH, false)//
				.withProperty(KspProperty.TYPE, false)//
				.withProperty(KspProperty.UNIT, false)//
				.withProperty(KspProperty.INDEX_TYPE, false)//
				.withProperty(KspProperty.STORE_TYPE, false)//
				.withAttribute("formatter", formatterEntity, false, true)//
				.withAttribute("dataType", KernelGrammar.INSTANCE.getDataTypeEntity(), false, true)//
				.withAttribute("constraint", constraintEntity, true, false)//
				.build();
	}

	private static Entity createDtComputedFieldEntity(final Entity domainEntity) {
		return new EntityBuilder(DT_COMPUTED_FIELD_META_DEFINITION)//
				.withProperty(KspProperty.LABEL, true)//
				.withAttribute("domain", domainEntity, false, true)//
				.withProperty(KspProperty.EXPRESSION, true)//
				.build();
	}

	private static Entity createAssociationEntity(final Entity dtDefinitionEntity) {
		return new EntityBuilder(ASSOCIATION_META_DEFINITION)//
				.withProperty(KspProperty.FK_FIELD_NAME, false)//
				.withProperty(KspProperty.MULTIPLICITY_A, true)//
				.withProperty(KspProperty.NAVIGABILITY_A, true)//
				.withProperty(KspProperty.ROLE_A, true)//
				.withProperty(KspProperty.LABEL_A, true)//
				.withProperty(KspProperty.MULTIPLICITY_B, true)//
				.withProperty(KspProperty.NAVIGABILITY_B, true)//
				.withProperty(KspProperty.ROLE_B, true)//
				.withProperty(KspProperty.LABEL_B, true)//
				.withAttribute("dtDefinitionA", dtDefinitionEntity, false, true)//
				.withAttribute("dtDefinitionB", dtDefinitionEntity, false, true)//
				.build();
	}

	private static Entity createAssociationNNEntity(final Entity dtDefinitionEntity) {
		return new EntityBuilder(ASSOCIATION_NN_META_DEFINITION)//
				.withProperty(KspProperty.TABLE_NAME, true)//
				.withProperty(KspProperty.NAVIGABILITY_A, true)//
				.withProperty(KspProperty.ROLE_A, true)//
				.withProperty(KspProperty.LABEL_A, true)//
				.withProperty(KspProperty.NAVIGABILITY_B, true)//
				.withProperty(KspProperty.ROLE_B, true)//
				.withProperty(KspProperty.LABEL_B, true)//
				.withAttribute("dtDefinitionA", dtDefinitionEntity, false, true)//
				.withAttribute("dtDefinitionB", dtDefinitionEntity, false, true)//s
				.build();
	}

	/**
	 * @return Définition d'une constraint.
	 */
	Entity getConstraintEntity() {
		return constraintEntity;
	}

	/**
	 * @return Définition d'un formatter.
	 */
	Entity getFormatterEntity() {
		return formatterEntity;
	}

	/**
	 * @return Définition d'un DT.
	 */
	public Entity getDtDefinitionEntity() {
		return dtDefinitionEntity;
	}

	/**
	 * @return Définition d'un domain.
	 */
	public Entity getDomainEntity() {
		return domainEntity;
	}

	/**
	 * @return Définition d'une association simple.
	 */
	public Entity getAssociationEntity() {
		return associationEntity;
	}

	/**
	 * @return Définition d'une association NN.
	 */
	public Entity getAssociationNNEntity() {
		return associationNNEntity;
	}

	/**
	 * @return Définition d'un champ de DT.
	 */
	public Entity getDtFieldEntity() {
		return dtFieldEntity;
	}
}
