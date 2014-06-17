package io.vertigo.dynamo.impl.environment.kernel.meta;

import io.vertigo.kernel.lang.Assertion;

/**
 * Attribut d'une entité.
 * 
 * @author pchretien
 */
public final class Attribute {
	private final String name;
	private final boolean multiple;
	private final boolean notNull;
	private final Entity entity;

	/**
	 * Constructeur.
	 * @param name Nom
	 * @param entity Entité / Méta-définition parente (composition ou référence)
	 * @param multiple Si multiple
	 * @param notNull Si not null
	 */
	Attribute(final String name, final Entity entity, final boolean multiple, final boolean notNull) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(entity);
		//----------------------------------------------------------------------
		this.name = name;
		this.multiple = multiple;
		this.notNull = notNull;
		this.entity = entity;
	}

	/**
	 * @return Nom
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Si multiple
	 */
	public boolean isMultiple() {
		return multiple;
	}

	/**
	 * @return Si not null
	 */
	public boolean isNotNull() {
		return notNull;
	}

	/**
	 * @return Entité référencée. (composition ou référence)
	 */
	public Entity getEntity() {
		return entity;
	}
}
