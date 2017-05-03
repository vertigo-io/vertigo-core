package io.vertigo.dynamo.domain.model;

import java.io.Serializable;

import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.store.datastore.DataStore;
import io.vertigo.lang.Assertion;

/**
 * This class is a way to access an entity defined by a relationship.
 * It's a kind of box (aka optional) that offers a small list of methods.
 *
 * @author pchretien
 *
 * @param <E> the type of entity
 */
public final class VAccessor<E extends Entity> implements Serializable {
	private static final long serialVersionUID = 1L;

	private static enum State {
		LOADED, NOT_LOADED
	}

	private State status = State.NOT_LOADED;
	private final DefinitionReference<DtDefinition> targetDtDefinitionRef;
	private final String role;
	private URI<E> targetURI;
	private E value;

	/**
	 * Constructor.
	 * @param clazz the entity class
	 */
	public VAccessor(final Class<E> clazz, final String role) {
		this(DtObjectUtil.findDtDefinition(clazz), role);
	}

	/**
	 * Constructor.
	 * @param targetDtDefinition the entity definition
	 */
	public VAccessor(final DtDefinition targetDtDefinition, final String role) {
		Assertion.checkNotNull(targetDtDefinition);
		Assertion.checkArgNotEmpty(role);
		//---
		this.targetDtDefinitionRef = new DefinitionReference(targetDtDefinition);
		this.role = role;
	}

	private static DataStore getDataStore() {
		return io.vertigo.app.Home.getApp().getComponentSpace().resolve(io.vertigo.dynamo.store.StoreManager.class)
				.getDataStore();
	}

	/**
	 * @return the entity
	 */
	public E get() {
		load();
		return value;
	}

	/**
	 * @return the entity uri
	 */
	public URI<E> getURI() {
		return targetURI;
	}

	/**
	 * @return the entity id
	 */
	public Serializable getId() {
		return targetURI == null ? null : targetURI.getId();
	}

	/**
	 * Loads the value if needed.
	 */
	private void load() {
		if (status == State.NOT_LOADED) {
			value = targetURI == null ? null : getDataStore().readOne(targetURI);
			status = State.LOADED;
		}
	}

	/**
	 * Sets the entity
	 * @param entity the entity
	 */
	public void set(final E entity) {
		Assertion.checkNotNull(entity);
		//---
		value = entity; //maybe null
		targetURI = entity == null ? null : entity.getURI();
		status = State.LOADED;
	}

	/**
	 * Sets the entity id
	 * @param id the entity id
	 */
	public void setId(final Serializable id) {
		//id final may be null
		//---
		targetURI = id == null ? null : new URI(targetDtDefinitionRef.get(), id);
		//we have to reset the value and the state
		value = null;
		status = State.NOT_LOADED;
	}

	/**
	 * Sets the entity uri
	 * @param uri the entity uri
	 */
	public void setUri(final URI<E> uri) {
		Assertion.checkNotNull(uri);
		//---
		targetURI = uri; //maybe null
		//we have to reset the value and the state
		value = null;
		status = State.NOT_LOADED;
	}

	public String getRole() {
		return role;
	}

	public boolean isLoaded() {
		return status == State.LOADED;
	}
}
