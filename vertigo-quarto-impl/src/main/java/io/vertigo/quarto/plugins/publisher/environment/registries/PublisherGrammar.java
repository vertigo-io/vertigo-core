package io.vertigo.quarto.plugins.publisher.environment.registries;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;

/**
 * Grammaire de publisher.
 * 
 * @author npiedeloup
 * @version $Id: PublisherGrammar.java,v 1.3 2014/02/03 17:29:01 pchretien Exp $
 */
final class PublisherGrammar extends GrammarProvider {
	/**
	 * Cl� des FIELD_DEFINITION de type PK utilis�s dans les DT_DEFINITION. 
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

	/**D�finition d'un DT.*/
	private final Entity publisherDefinition;
	/**D�finition d'un domain.*/
	private final Entity nodeDefinition;

	/**D�finition des champs.*/
	private final Entity fieldDefinition;
	/**D�finition des champs typ�s.*/
	private final Entity dataFieldDefinition;

	/**
	 * Initialisation des m�tadonn�es permettant de d�crire le m�tamod�le de Dynamo.
	 */
	PublisherGrammar() {
		//--
		fieldDefinition = new EntityBuilder(NODE_FIELD_META_DEFINITION).build();
		//--
		//On a une relation circulaire 
		//On conserve donc une r�f�rence sur le builder
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
	 * @return D�finition d'une edition.
	 */
	Entity getPublisherDefinition() {
		return publisherDefinition;
	}

	/**
	 * @return D�finition d'un PublisherNode
	 */
	Entity getPublisherNodeDefiniton() {
		return nodeDefinition;
	}

	/**
	 * @return D�finition des champs d'un PublisherNode
	 */
	public Entity getFieldDefinition() {
		return fieldDefinition;
	}

	/**
	 * @return D�finition des champs data d'un PublisherNode
	 */
	public Entity getDataFieldDefinition() {
		return dataFieldDefinition;
	}
}
