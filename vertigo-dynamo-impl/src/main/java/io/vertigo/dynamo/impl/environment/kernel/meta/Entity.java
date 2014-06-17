package io.vertigo.dynamo.impl.environment.kernel.meta;

import io.vertigo.kernel.lang.Assertion;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Une entité permet de décrire un modèle, une classe.
 * - Elle est définie par son nom.
 * - Elle possède une liste de propriétés (Chacune étant obligatoire / facultative)
 * - Elle est composée d'une liste d'attibuts. 
 *
 * Une entité permet, ainsi, d'adopter des comportement dynamique, de fabriquer des grammaires.
 * Si l'ensemble des définitions permet de construire le modèle, l'ensemble des entités permet de décrire le métamodèle.
 *
 * @author pchretien
 */
public final class Entity {
	/**
	 * Nom de la metadefinition (Type de la définition).
	 */
	private final String name;

	/**
	 * Liste de TOUTES les définitions (composites et références) acceptées.
	 */
	private final Set<Attribute> attributes;
	/**
	 * Map permettant de savoir si une propriété est obligatoire, facultative (Property, Boolean)
	 * Set des propriétés autorisées pour la définition
	 * est représenté par la liste des clés de la Map.
	 */
	private final Map<EntityProperty, Boolean> properties;

	/**
	 * Constructeur de la MetaDefinition
	 * Une instance de MetaDefinition correspond à une classe -ou une interface- de Definition
	 * (Exemple : Classe Service).
	 * @param name Classe représentant l'instance métaDéfinition
	 */
	Entity(final String name, final Set<Attribute> attributes, final Map<EntityProperty, Boolean> properties) {
		Assertion.checkNotNull(name);
		Assertion.checkNotNull(attributes);
		Assertion.checkNotNull(properties);
		//----------------------------------------------------------------------
		this.name = name;
		this.attributes = Collections.unmodifiableSet(attributes);
		this.properties = Collections.unmodifiableMap(properties);
	}

	/**
	 * @return Nom de l'entité (Type de la définition).
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Ensemble de toutes les propriétés gérées (obligatoires ou non).
	 */
	public Set<EntityProperty> getProperties() {
		return properties.keySet();
	}

	/**
	 * @param property Propriété 
	 * @return Si la propriété mentionnée est nulle
	 */
	public boolean isNotNull(final EntityProperty property) {
		Assertion.checkNotNull(property);
		Assertion.checkArgument(properties.containsKey(property), "la propriete {0} n'est pas declaree pour {1}", property, this);
		//----------------------------------------------------------------------
		return properties.get(property);
	}

	/**
	 * @return Set des attributs de l'entité
	 */
	public Set<Attribute> getAttributes() {
		return attributes;
	}
}
