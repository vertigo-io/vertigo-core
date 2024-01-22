package io.vertigo.core.node.definition;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a list of definitions through an enum.
 *
 * @author skerdudou
 */
public interface SimpleEnumDefinitionProvider<D extends Definition> extends DefinitionProvider {

	/**
	 * Return a list of definitions with a set of already known definitions
	 *
	 * @param definitionSpace the actual definitionSpace
	 * @return the list of new definition to register
	 */
	@Override
	default List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		final var enumValues = getEnumClass().getEnumConstants();

		return Arrays.stream(enumValues)
				.map(v -> (DefinitionSupplier) v::buildDefinition)
				.toList();
	}

	Class<? extends EnumDefinition<D, ? extends Enum<?>>> getEnumClass();

	static interface EnumDefinition<D, T extends Enum<T>> {

		D buildDefinition(DefinitionSpace definitionSpace);

		D get();

	}
}
