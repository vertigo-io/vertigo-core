package io.vertigo.dynamo.plugins.environment.registries.file;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;
import io.vertigo.dynamo.plugins.environment.KspProperty;

/**
 * @author npiedeloup
 */
final class FileGrammar extends GrammarProvider {

	/**Définition de tache.*/
	final Entity fileInfoDefinition;

	/**
	 * Constructeur.
	 * Initialisation des métadonnées permettant de décrire le métamodèle de Dynamo.
	 */
	FileGrammar() {
		fileInfoDefinition = createFileInfoDefinitionEntity();
		getGrammar().registerEntity(fileInfoDefinition);
	}

	private static Entity createFileInfoDefinitionEntity() {
		return new EntityBuilder("FileInfo")//
				.withProperty(KspProperty.STORE_NAME, true)//
				.withProperty(KspProperty.ROOT, true)//
				.build();
	}
}
