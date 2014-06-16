package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.domain.metamodel.KDataType;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;

final class KernelLoaderPlugin implements LoaderPlugin {
	private final KernelGrammar kernelGrammar;

	/**
	 * Constructeur.
	 */
	KernelLoaderPlugin() {
		kernelGrammar = KernelGrammar.INSTANCE;

	}

	/** {@inheritDoc} */
	public void load(final DynamicDefinitionRepository dynamicModelrepository) {
		//--Enregistrement des types primitifs
		final Entity dataTypeEntity = kernelGrammar.getDataTypeEntity();
		for (final KDataType type : KDataType.values()) {
			final DynamicDefinition definition = dynamicModelrepository.createDynamicDefinitionBuilder(type.name(), dataTypeEntity, null).build();
			dynamicModelrepository.addDefinition(definition);
		}
	}
}
