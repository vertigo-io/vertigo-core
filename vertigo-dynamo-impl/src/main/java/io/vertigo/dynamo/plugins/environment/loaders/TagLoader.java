package io.vertigo.dynamo.plugins.environment.loaders;

import java.util.List;

public interface TagLoader {

	/**
	 * Récupération des classes déclarées dans l'OOM.
	 * @return Liste des classes
	 */
	List<TagClass> getTagClasses();

	/**
	 * Récupération des associations déclarées dans l'OOM.
	 * @return Liste des associations
	 */
	List<TagAssociation> getTagAssociations();

}