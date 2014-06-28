package io.vertigo.quarto.plugins.publisher.environment.registries;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.publisher.metamodel.PublisherDataDefinition;
import io.vertigo.quarto.publisher.metamodel.PublisherNodeDefinition;
import io.vertigo.quarto.publisher.metamodel.PublisherNodeDefinitionBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

/**
 * DynamicRegistry de PublisherManager.
 * @author npiedeloup
 * @version $Id: PublisherDynamicRegistryPlugin.java,v 1.7 2014/02/27 10:32:26 pchretien Exp $
 */
public final class PublisherDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin<PublisherGrammar> {
	private final Map<String, PublisherNodeDefinitionBuilder> publisherDefinitionMap = new HashMap<>();
	private final Set<String> unusedNodeSet = new HashSet<>();

	/**
	 * Constructeur.
	 */
	@Inject
	public PublisherDynamicRegistryPlugin() {
		super(new PublisherGrammar());
		Home.getDefinitionSpace().register(PublisherDataDefinition.class);
	}

	/** {@inheritDoc} */
	public void onDefinition(final DynamicDefinition xdefinition) {
		final Entity metaDefinition = xdefinition.getEntity();

		if (metaDefinition.equals(getGrammarProvider().getPublisherDefinition())) {
			final PublisherDataDefinition definition = createPublisherDataDefinition(xdefinition);
			Home.getDefinitionSpace().put(definition, PublisherDataDefinition.class);
		} else if (metaDefinition.equals(getGrammarProvider().getPublisherNodeDefiniton())) {
			createPublisherNodeDefinition(xdefinition);
		} else {
			throw new IllegalArgumentException("Type de d�finition non g�r�e: " + xdefinition.getDefinitionKey().getName());
		}
	}

	private PublisherDataDefinition createPublisherDataDefinition(final DynamicDefinition xpublisherDefinition) {
		final String definitionName = xpublisherDefinition.getDefinitionKey().getName();
		final String publisherNodeRootName = xpublisherDefinition.getDefinitionKey("root").getName();

		final PublisherNodeDefinition rootDefinition = getNodeDefinitionBuilder(publisherNodeRootName, "root", definitionName).build();
		return new PublisherDataDefinition(definitionName, rootDefinition);
	}

	private void createPublisherNodeDefinition(final DynamicDefinition xpublisherNodeDefinition) {
		final PublisherNodeDefinitionBuilder publisherDataNodeDefinitionBuilder = new PublisherNodeDefinitionBuilder();
		final String publisherDataNodeName = xpublisherNodeDefinition.getDefinitionKey().getName();

		//D�claration des champs string
		final List<DynamicDefinition> stringFieldList = xpublisherNodeDefinition.getChildDefinitions(PublisherGrammar.STRING_FIELD);
		for (final DynamicDefinition field : stringFieldList) {
			final String fieldName = field.getDefinitionKey().getName();
			publisherDataNodeDefinitionBuilder.withStringField(fieldName);
		}

		//D�claration des champs boolean
		final List<DynamicDefinition> booleanFieldList = xpublisherNodeDefinition.getChildDefinitions(PublisherGrammar.BOOLEAN_FIELD);
		for (final DynamicDefinition field : booleanFieldList) {
			publisherDataNodeDefinitionBuilder.withBooleanField(field.getDefinitionKey().getName());
		}

		//D�claration des champs images
		final List<DynamicDefinition> imageFieldList = xpublisherNodeDefinition.getChildDefinitions(PublisherGrammar.IMAGE_FIELD);
		for (final DynamicDefinition field : imageFieldList) {
			publisherDataNodeDefinitionBuilder.withImageField(field.getDefinitionKey().getName());
		}

		//D�claration des champs data
		final List<DynamicDefinition> dataFieldList = xpublisherNodeDefinition.getChildDefinitions(PublisherGrammar.DATA_FIELD);
		for (final DynamicDefinition field : dataFieldList) {
			final String fieldName = field.getDefinitionKey().getName();
			final String refNodeName = field.getDefinitionKey("type").getName();
			final PublisherNodeDefinitionBuilder publisherNode = getNodeDefinitionBuilder(refNodeName, fieldName, publisherDataNodeName);
			publisherDataNodeDefinitionBuilder.withNodeField(fieldName, publisherNode.build());
		}

		//D�claration des champs list
		final List<DynamicDefinition> listFieldList = xpublisherNodeDefinition.getChildDefinitions(PublisherGrammar.LIST_FIELD);
		for (final DynamicDefinition field : listFieldList) {
			final String fieldName = field.getDefinitionKey().getName();
			final String refNodeName = field.getDefinitionKey("type").getName();
			final PublisherNodeDefinitionBuilder publisherNode = getNodeDefinitionBuilder(refNodeName, fieldName, publisherDataNodeName);
			publisherDataNodeDefinitionBuilder.withListField(fieldName, publisherNode.build());
		}

		//		System.out.println("Add " + publisherDataNodeName);
		publisherDefinitionMap.put(publisherDataNodeName, publisherDataNodeDefinitionBuilder);
		unusedNodeSet.add(publisherDataNodeName);
	}

	private PublisherNodeDefinitionBuilder getNodeDefinitionBuilder(final String name, final String fieldName, final String parentNodeName) {
		final PublisherNodeDefinitionBuilder publisherNodeDefinitionBuilder = publisherDefinitionMap.get(name);
		Assertion.checkNotNull(publisherNodeDefinitionBuilder, "Le PublisherNode {0} est introuvable pour le field {1} de {2}.", name, fieldName, parentNodeName);
		unusedNodeSet.remove(name);
		//		System.out.println("ref " + name + "  in " + parentNodeName + "." + fieldName);

		return publisherNodeDefinitionBuilder;
	}
}
