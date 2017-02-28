package io.vertigo.core.definition;

import java.util.Collection;

/**
 * Espace de définitions (non threadSafe).
 * @author mlaroche
 *
 */
public interface DefinitionSpace {

	/**
	 * Returns true if this container contains the specified definition
	 * @param name the name of the expected definition
	 * @return true if the definition is already registered.
	 */
	boolean contains(String name);

	/**
	 * Resolve a definition from its name and class.
	 * @param name the name of the expected definition
	 * @param clazz Type of the definition
	 * @return the definition
	 */
	<D extends Definition> D resolve(String name, Class<D> clazz);

	/**
	 * @return Liste de tous les types de définition gérés.
	 */
	Collection<Class<? extends Definition>> getAllTypes();

	/**
	 * @return Collection de tous les objets enregistrés pour un type donné.
	 * @param clazz type de l'object
	 * @param <C> Type de l'objet
	 */
	<C extends Definition> Collection<C> getAll(Class<C> clazz);
}
