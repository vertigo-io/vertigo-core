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
package io.vertigo.quarto.plugins.publisher.environment.registries;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;

/**
 * Grammaire de publisher.
 * 
 * @author npiedeloup
 */
final class PublisherGrammar extends GrammarProvider {
	/**
	 * Clé des FIELD_DEFINITION de type PK utilisés dans les DT_DEFINITION.
	 */
	static final String STRING_FIELD = "stringField";
	static final String BOOLEAN_FIELD = "booleanField";
	static final String IMAGE_FIELD = "imageField";
	static final String DATA_FIELD = "dataField";
	static final String LIST_FIELD = "listField";

	private static final String PUB_DEFINITION_META_DEFINITION = "PublisherDefinition";
	private static final String NODE_DEFINITION_META_DEFINITION = "PublisherNode";
	private static final String NODE_FIELD_META_DEFINITION = "DataField";
	private static final String NODE_DATA_FIELD_META_DEFINITION = "NodeField";
	//public static final String NODE_TYPE_META_DEFINITION = "type";

	/**Définition d'un DT.*/
	private final Entity publisherDefinition;
	/**Définition d'un domain.*/
	private final Entity nodeDefinition;

	/**Définition des champs.*/
	private final Entity fieldDefinition;
	/**Définition des champs typés.*/
	private final Entity dataFieldDefinition;

	/**
	 * Initialisation des métadonnées permettant de décrire le métamodèle de Dynamo.
	 */
	PublisherGrammar() {
		//--
		fieldDefinition = new EntityBuilder(NODE_FIELD_META_DEFINITION).build();
		//--
		//On a une relation circulaire
		//On conserve donc une référence sur le builder
		final EntityBuilder builder = new EntityBuilder(NODE_DEFINITION_META_DEFINITION);
		dataFieldDefinition = createDataFieldDefinitionEntity(builder.build());
		nodeDefinition = createNodeDefinitionEntity(builder, dataFieldDefinition, fieldDefinition);
		//--
		publisherDefinition = createPublisherDefinitionEntity(nodeDefinition);
		//---------------------------------------------------------------------
		getGrammar().registerEntity(publisherDefinition);
		getGrammar().registerEntity(nodeDefinition);
		getGrammar().registerEntity(fieldDefinition);
		getGrammar().registerEntity(dataFieldDefinition);
	}

	private static Entity createDataFieldDefinitionEntity(final Entity nodeDefinition) {
		return new EntityBuilder(NODE_DATA_FIELD_META_DEFINITION)//
		.withAttribute("type", nodeDefinition, false, true)//
		.build();
	}

	private static Entity createNodeDefinitionEntity(final EntityBuilder builder, final Entity dataFieldDefinition, final Entity fieldDefinition) {
		return builder//
				.withAttribute(STRING_FIELD, fieldDefinition, true, false) //Multiple, facultative
				.withAttribute(BOOLEAN_FIELD, fieldDefinition, true, false) //Multiple, facultative
				.withAttribute(IMAGE_FIELD, fieldDefinition, true, false) //Multiple, facultative
				.withAttribute(DATA_FIELD, dataFieldDefinition, true, false) //Multiple, facultative
				.withAttribute(LIST_FIELD, dataFieldDefinition, true, false) //Multiple, facultative
				.build();
	}

	private static Entity createPublisherDefinitionEntity(final Entity nodeDefinition) {
		return new EntityBuilder(PUB_DEFINITION_META_DEFINITION)//
		.withAttribute("root", nodeDefinition, false, true)//
		.build();
	}

	/**
	 * @return Définition d'une edition.
	 */
	Entity getPublisherDefinition() {
		return publisherDefinition;
	}

	/**
	 * @return Définition d'un PublisherNode
	 */
	Entity getPublisherNodeDefiniton() {
		return nodeDefinition;
	}

	/**
	 * @return Définition des champs d'un PublisherNode
	 */
	public Entity getFieldDefinition() {
		return fieldDefinition;
	}

	/**
	 * @return Définition des champs data d'un PublisherNode
	 */
	public Entity getDataFieldDefinition() {
		return dataFieldDefinition;
	}
}
