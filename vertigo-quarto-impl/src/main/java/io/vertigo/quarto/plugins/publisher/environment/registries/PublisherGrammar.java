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

import io.vertigo.core.definition.dsl.entity.Entity;
import io.vertigo.core.definition.dsl.entity.EntityBuilder;
import io.vertigo.core.definition.dsl.entity.EntityGrammar;

/**
 * Grammaire de publisher.
 *
 * @author npiedeloup
 */
final class PublisherGrammar {
	/**
	 * Clé des FIELD_DEFINITION de type PK utilisés dans les DT_DEFINITION.
	 */
	public static final String STRING_FIELD = "stringField";
	public static final String BOOLEAN_FIELD = "booleanField";
	public static final String IMAGE_FIELD = "imageField";
	public static final String DATA_FIELD = "dataField";
	public static final String LIST_FIELD = "listField";

	private static final String PUB_DEFINITION_META_DEFINITION = "PublisherDefinition";
	private static final String NODE_DEFINITION_META_DEFINITION = "PublisherNode";
	private static final String NODE_FIELD_META_DEFINITION = "DataField";
	private static final String NODE_DATA_FIELD_META_DEFINITION = "NodeField";
	//public static final String NODE_TYPE_META_DEFINITION = "type";

	/**Définition d'un DT.*/
	static final Entity publisherDefinition;
	/**Définition d'un domain.*/
	static final Entity publisherNodeDefinition;

	/**Définition des champs.*/
	private static final Entity publisherFieldDefinition;
	/**Définition des champs typés.*/
	private static final Entity publisherDataFieldDefinition;

	/** Publisher Grammar instance. */
	public static final EntityGrammar GRAMMAR;
	/**
	 * Initialisation des métadonnées permettant de décrire le métamodèle de Dynamo.
	 */
	static {
		//On a une relation circulaire
		//On conserve donc une référence sur le builder
		final EntityBuilder builder = new EntityBuilder(NODE_DEFINITION_META_DEFINITION);
		publisherFieldDefinition = new EntityBuilder(NODE_FIELD_META_DEFINITION).build();

		publisherDataFieldDefinition = new EntityBuilder(NODE_DATA_FIELD_META_DEFINITION)//
				.addField("type", builder.build().getLink(), true)
				.build();

		publisherNodeDefinition = builder//
				.addFields(STRING_FIELD, publisherFieldDefinition, false) // facultative
				.addFields(BOOLEAN_FIELD, publisherFieldDefinition, false) // facultative
				.addFields(IMAGE_FIELD, publisherFieldDefinition, false) //facultative
				.addFields(DATA_FIELD, publisherDataFieldDefinition, false) //facultative
				.addFields(LIST_FIELD, publisherDataFieldDefinition, false) //facultative
				.build();

		//--
		publisherDefinition = new EntityBuilder(PUB_DEFINITION_META_DEFINITION)
				.addField("root", publisherNodeDefinition.getLink(), true)
				.build();
		//-----
		GRAMMAR = new EntityGrammar(publisherDefinition,
				publisherNodeDefinition,
				publisherFieldDefinition,
				publisherDataFieldDefinition);
	}

}
