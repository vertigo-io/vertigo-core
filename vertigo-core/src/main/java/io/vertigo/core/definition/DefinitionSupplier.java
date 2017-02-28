package io.vertigo.core.definition;

/**
 * Functional Interface for providing Definitions.
 * @author mlaroche
 *
 */
@FunctionalInterface
public interface DefinitionSupplier {

	/**
	 * Provide a definition with a definitionSpace a in parameter for composite definitions
	 * @param definitionSpace the actual definitionSpace
	 * @return a new definition
	 */
	Definition get(DefinitionSpace definitionSpace);

}
