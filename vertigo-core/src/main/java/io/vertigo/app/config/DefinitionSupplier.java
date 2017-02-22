package io.vertigo.app.config;

import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;

@FunctionalInterface
public interface DefinitionSupplier {

	Definition get(DefinitionSpace definitionSpace);

}
