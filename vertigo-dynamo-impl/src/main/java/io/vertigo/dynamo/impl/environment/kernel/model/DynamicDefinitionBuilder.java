package io.vertigo.dynamo.impl.environment.kernel.model;

import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.kernel.lang.Builder;

import java.util.List;

/**
 * Interface de création des définitions.
 * @author  pchretien
 * @version $Id: DynamicDefinitionBuilder.java,v 1.5 2013/10/22 12:30:28 pchretien Exp $
 */
public interface DynamicDefinitionBuilder extends Builder<DynamicDefinition> {
	DynamicDefinitionBuilder withPackageName(String packageName);

	DynamicDefinitionBuilder withBody(DynamicDefinition dynamicDefinition);

	/**
	 * @param property Propriété
	 * @param value Valeur de la propriété
	 */
	DynamicDefinitionBuilder putPropertyValue(final EntityProperty property, final Object value);

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Nom du champ
	 * @param definitionKey Clé de la définition
	 */
	DynamicDefinitionBuilder addDefinition(final String fieldName, final DynamicDefinitionKey definitionKey);

	/**
	 * Ajoute une liste de définitions au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Nom du champ
	 * @param definitionKeyList  Liste des clés de définition
	 */
	void addDefinitionList(final String fieldName, final List<DynamicDefinitionKey> definitionKeyList);

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * @param fieldName Nom du champ
	 * @param definition Définition
	 */
	void addChildDefinition(final String fieldName, final DynamicDefinition definition);
}
