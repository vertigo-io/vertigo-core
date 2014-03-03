package io.vertigo.dynamo.plugins.environment.registries.file;

import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.kernel.Home;

/**
 * @author pchretien
 * @version $Id: FileDynamicRegistryPlugin.java,v 1.2 2013/10/22 12:34:28 pchretien Exp $
 */
public final class FileDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin<FileGrammar> {

	/**
	 * Constructeur.
	 */
	public FileDynamicRegistryPlugin() {
		super(new FileGrammar());
		Home.getDefinitionSpace().register(FileInfoDefinition.class);
	}

	/** {@inheritDoc} */
	public void onDefinition(final DynamicDefinition xdefinition) {
		if (getGrammarProvider().fileInfoDefinition.equals(xdefinition.getEntity())) {
			//Seuls les taches sont gérées.
			final FileInfoDefinition definition = createFileDefinition(xdefinition);
			Home.getDefinitionSpace().put(definition, FileInfoDefinition.class);
		}
	}

	private static FileInfoDefinition createFileDefinition(final DynamicDefinition xFileDefinition) {
		final String fileDefinitionName = xFileDefinition.getDefinitionKey().getName();
		final String fileStorePluginName = getPropertyValueAsString(xFileDefinition, KspProperty.STORE_NAME);
		final String root = getPropertyValueAsString(xFileDefinition, KspProperty.ROOT);

		return new FileInfoDefinition(fileDefinitionName, root, fileStorePluginName);
	}

}
