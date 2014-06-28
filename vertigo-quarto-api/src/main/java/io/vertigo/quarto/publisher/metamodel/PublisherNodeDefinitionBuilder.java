package io.vertigo.quarto.publisher.metamodel;

import io.vertigo.kernel.lang.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder de la d�finition d'un mod�le de noeud d'�dition.
 * Un noeud d'edition compose l'arbre des des donn�es d'une �dition.
 *
 * @author npiedeloup, pchretien
 * @version $Id: PublisherNodeDefinitionBuilder.java,v 1.3 2014/02/27 10:32:26 pchretien Exp $
 */
public final class PublisherNodeDefinitionBuilder implements Builder<PublisherNodeDefinition> {
	private final List<PublisherField> publisherFields = new ArrayList<>();

	/**
	 * Ajoute un champ bool�en.
	 * @param fieldName Nom du champ
	 */
	public PublisherNodeDefinitionBuilder withBooleanField(final String fieldName) {
		return registerField(fieldName, PublisherFieldType.Boolean, null);
	}

	/**
	 * Ajoute un champ String.
	 * @param fieldName Nom du champ
	 */
	public PublisherNodeDefinitionBuilder withStringField(final String fieldName) {
		return registerField(fieldName, PublisherFieldType.String, null);
	}

	/**
	 * Ajoute un champ Image.
	 * @param fieldName Nom du champ
	 */
	public PublisherNodeDefinitionBuilder withImageField(final String fieldName) {
		return registerField(fieldName, PublisherFieldType.Image, null);
	}

	/**
	 * Ajoute un champ Data (autre noeud).
	 * @param fieldName Nom du champ
	 * @param nodeDefinition D�finition du noeud
	 */
	public PublisherNodeDefinitionBuilder withNodeField(final String fieldName, final PublisherNodeDefinition nodeDefinition) {
		return registerField(fieldName, PublisherFieldType.Node, nodeDefinition);
	}

	/**
	 * Ajoute un champ List (liste compos�e de noeud).
	 * @param fieldName Nom du champ
	 * @param nodeDefinition D�finition des �l�ments de la liste
	 */
	public PublisherNodeDefinitionBuilder withListField(final String fieldName, final PublisherNodeDefinition nodeDefinition) {
		return registerField(fieldName, PublisherFieldType.List, nodeDefinition);
	}

	private PublisherNodeDefinitionBuilder registerField(final String fieldName, final PublisherFieldType fieldType, final PublisherNodeDefinition nodeDefinition) {
		publisherFields.add(new PublisherField(fieldName, fieldType, nodeDefinition));
		return this;
	}

	/**
	 * 
	 * @return PublisherDataNodeDefinition
	 */
	public PublisherNodeDefinition build() {
		return new PublisherNodeDefinition(publisherFields);
	}
}
