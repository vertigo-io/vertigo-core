package io.vertigo.dynamo.impl.environment.kernel.meta;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builder des entities.
 * 
 * @author pchretien
 */
public final class EntityBuilder implements Builder<Entity> {
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
	public EntityBuilder(final String name) {
		Assertion.checkNotNull(name);
		//----------------------------------------------------------------------
		this.name = name;
		attributes = new HashSet<>();
		properties = new HashMap<>();

	}

	/**
	 * Ajout d'un attribut.
	 * @param fieldName Nom
	 * @param entity Entité référencée
	 * @param multiple Si il y a plusieurs entités référencées
	 * @param notNull Si l'attribut est obligatoire
	 */
	public EntityBuilder withAttribute(final String fieldName, final Entity entity, final boolean multiple, final boolean notNull) {
		Assertion.checkNotNull(fieldName);
		Assertion.checkNotNull(entity);
		//On vérifie que le nom du champ n'est pas déjà utilisé.
		//----------------------------------------------------------------------
		final Attribute metaFieldDefinition = new Attribute(fieldName, entity, multiple, notNull);
		//----------------------------------------------------------------------
		attributes.add(metaFieldDefinition);
		return this;
	}

	/**
	 * Ajout d'une propriété.
	 * @param property Propriété
	 * @param notNull Si la propriété est obligatoire
	 */
	public EntityBuilder withProperty(final EntityProperty property, final boolean notNull) {
		Assertion.checkNotNull(property);
		Assertion.checkArgument(!properties.containsKey(property), "la propriete {0} est deja declaree pour {1}", property, this);
		//----------------------------------------------------------------------
		//Une propriété est unique pour une définition donnée.
		//Il n'y a jamais de multiplicité
		properties.put(property, notNull);
		return this;
	}

	/** {@inheritDoc} */
	public Entity build() {
		return new Entity(name, attributes, properties);
	}
}
