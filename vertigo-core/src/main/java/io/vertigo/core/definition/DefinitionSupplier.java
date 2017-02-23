package io.vertigo.core.definition;

@FunctionalInterface
public interface DefinitionSupplier {

	Definition get(DefinitionSpace definitionSpace);

}
