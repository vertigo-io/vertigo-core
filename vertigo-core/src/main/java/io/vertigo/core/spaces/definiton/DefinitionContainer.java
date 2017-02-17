package io.vertigo.core.spaces.definiton;

import java.util.Set;

/**
 * The Container interface defines a universal container for the definitions.
 * Each definition is identified by a name.
 *
 * @author pchretien
 */
public interface DefinitionContainer {

	/**
	 * Returns true if this container contains the specified definition
	 * @param name the name of the expected definition
	 * @return true if the definition is already registered.
	 */
	boolean contains(final String name);

	/**
	 * Resolve a definition from its name and class.
	 * @param name the name of the expected definition
	 * @param definitionClass Type of the definition
	 * @return the definition
	 */
	<D extends Definition> D resolve(final String name, final Class<D> definitionClass);

	/**
	 * Returns the list of the names  of the definitions managed in this container.
	 * @return list of names
	 */
	Set<String> keySet();
}
