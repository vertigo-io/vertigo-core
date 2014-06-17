package io.vertigo.dynamo.impl.environment.kernel.model;

import io.vertigo.dynamo.impl.environment.kernel.meta.EntityProperty;
import io.vertigo.kernel.lang.Builder;

import java.util.List;

/**
 * Interface de création des définitions.
 * @author  pchretien
 */
public interface DynamicDefinitionBuilder extends Builder<DynamicDefinition> {
	DynamicDefinitionBuilder withPackageName(String packageName);

	DynamicDefinitionBuilder withBody(DynamicDefinition dynamicDefinition);

	/**
	 * @param property Propriété
	 * @param value Valeur de la propriété
	 */
	DynamicDefinitionBuilder withPropertyValue(final EntityProperty property, final Object value);

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Nom du champ
	 * @param definitionKey Clé de la définition
	 */
	DynamicDefinitionBuilder withDefinition(final String fieldName, final DynamicDefinitionKey definitionKey);

	/**
	 * Ajoute une liste de définitions au champ défini par fieldName.
	 * La définition n'est connue que par sa référence, son nom.
	 * @param fieldName Nom du champ
	 * @param definitionKeyList  Liste des clés de définition
	 */
	DynamicDefinitionBuilder withDefinitions(final String fieldName, final List<DynamicDefinitionKey> definitionKeys);

	/**
	 * Ajoute une définition au champ défini par fieldName.
	 * @param fieldName Nom du champ
	 * @param definition Définition
	 */
	DynamicDefinitionBuilder withChildDefinition(final String fieldName, final DynamicDefinition definition);
}
