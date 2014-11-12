package io.vertigo.dynamo.plugins.environment.loaders.xml;

import java.util.List;

public interface XmlLoader {

	/**
	 * Récupération des classes déclarées dans l'OOM.
	 * @return Liste des classes
	 */
	List<XmlClass> getClasses();

	/**
	 * Récupération des associations déclarées dans l'OOM.
	 * @return Liste des associations
	 */
	List<XmlAssociation> getAssociations();

}