package io.vertigo.core.dsl.entity;

import io.vertigo.lang.Assertion;

public final class EntityLink implements EntityType {
	private final Entity entity;

	public EntityLink(final Entity entity) {
		Assertion.checkNotNull(entity);
		Assertion.checkState(!entity.isPrimitive(), "A primitive entity such as {0} can't be linked", entity);
		//-----
		this.entity = entity;
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public boolean isPrimitive() {
		return false;
	}
}
