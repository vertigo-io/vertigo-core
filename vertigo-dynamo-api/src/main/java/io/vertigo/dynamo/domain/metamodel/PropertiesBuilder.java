package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Builder;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestion de la flexibilité structurelle du modèle.
 * Permet d'ajouter des propriétés sur les concepts structurels.
 *
 * @author pchretien
 */
public final class PropertiesBuilder implements Builder<Properties> {
	private final Map<Property<?>, Object> properties = new HashMap<>();

	/**
	 * Ajout d'une propriété typée.
	 * @param property propriété
	 * @param value Valeur de la propriété
	 */
	public <T> void withValue(final Property<T> property, final T value) {
		Assertion.checkNotNull(property);
		Assertion.checkArgument(!properties.containsKey(property), "Propriété {0} déjà déclarée : ", property);
		//On vérifie que la valeur est du bon type
		property.getType().cast(value);
		//----------------------------------------------------------------------
		properties.put(property, value);
	}

	@Override
	public Properties build() {
		return new Properties(properties);
	}
}
