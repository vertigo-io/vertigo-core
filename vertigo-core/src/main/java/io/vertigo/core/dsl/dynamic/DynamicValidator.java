package io.vertigo.core.dsl.dynamic;

import io.vertigo.core.dsl.entity.Entity;
import io.vertigo.lang.Assertion;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Validate a definition considering its own entity.
 * 
 * @author pchretien
 *
 */
final class DynamicValidator {
	static void check(final DynamicDefinition definition) {
		Assertion.checkNotNull(definition);
		//-----
		final Entity myEntity = definition.getEntity();
		// 1.On vérifie la définition par rapport à la métadéfinition
		// 1.1 on vérifie les propriétés.
		final Set<String> propertyNames = definition.getPropertyNames();
		final Set<String> entityPropertyNames = myEntity.getPropertyNames();
		// 1.1.1 on vérifie que toutes les propriétés sont déclarées sur le
		// métamodèle
		checkProperties(definition, propertyNames, entityPropertyNames);

		// 1.1.2 on vérifie les propriétés obligatoires
		checkMandatoryProperties(definition, myEntity, propertyNames, entityPropertyNames);

		// 1.1.3 on vérifie les types des propriétés déclarées
		for (final String propertyName : propertyNames) {
			myEntity.getPrimitiveType(propertyName).checkValue(definition.getPropertyValue(propertyName));
		}

		// 1.2 on vérifie les définitions composites (sous définitions).
		for (final DynamicDefinition child : definition.getAllChildDefinitions()) {
			check(child);
		}

		// 1.3 on vérifie les définitions références.
		// TODO vérifier les définitions références
	}

	private static void checkProperties(
			final DynamicDefinition definition,
			final Set<String> propertyNames,
			final Set<String> entityPropertyNames) {
		// Vérification que toutes les propriétés sont déclarées sur le
		// métamodèle
		final Set<String> undeclaredPropertyNames = new HashSet<>();
		for (final String propertyName : propertyNames) {
			if (!entityPropertyNames.contains(propertyName)) {
				// Si la propriété n'est pas déclarée alors erreur
				undeclaredPropertyNames.add(propertyName);
			}
		}
		if (!undeclaredPropertyNames.isEmpty()) {
			throw new IllegalStateException("Sur l'objet '" + definition.getName() + "' Il existe des propriétés non déclarées " + undeclaredPropertyNames);
		}
	}

	private static void checkMandatoryProperties(
			final DynamicDefinition dynamicDefinition,
			final Entity myEntity,
			final Set<String> propertyNames,
			final Set<String> entityPropertyNames) {
		// Vérification des propriétés obligatoires
		final Set<String> unusedMandatoryPropertySet = new HashSet<>();
		for (final String propertyName : entityPropertyNames) {
			if (myEntity.isRequired(propertyName) && (!propertyNames.contains(propertyName) || dynamicDefinition.getPropertyValue(propertyName) == null)) {
				// Si la propriété obligatoire n'est pas renseignée alors erreur
				// Ou si la propriété obligatoire est renseignée mais qu'elle
				// est nulle alors erreur !
				unusedMandatoryPropertySet.add(propertyName);
			}
		}
		if (!unusedMandatoryPropertySet.isEmpty()) {
			throw new IllegalStateException(dynamicDefinition.getName() + " Il existe des propriétés obligatoires non renseignées " + unusedMandatoryPropertySet);
		}
	}
}
