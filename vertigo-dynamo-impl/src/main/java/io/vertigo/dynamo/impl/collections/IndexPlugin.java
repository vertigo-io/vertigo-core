package io.vertigo.dynamo.impl.collections;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.component.Plugin;

import java.util.Collection;

/**
 * Plugin de construction et d'interrogation de l'index d'une DtList.
 * @author npiedeloup
 * @version $Id: IndexPlugin.java,v 1.3 2014/01/20 17:45:43 pchretien Exp $
 */
public interface IndexPlugin extends Plugin {
	/**
	 * Retourne une liste filtrée en fonction de la saisie utilisateur.
	 * @param <D> Type d'objet
	 * @param keywords Liste de Mot-clé recherchés séparés par espace(préfix d'un mot)
	 * @param searchedFields Liste des champs sur lesquel porte la recherche  (non null)
	 * @param maxRows Nombre maximum de lignes retournées
	 * @param boostedField Champ boosté (nullable : aucun)
	 * @return Liste résultat 
	 */
	<D extends DtObject> DtList<D> getCollection(final String keywords, final Collection<DtField> searchedFields, final int maxRows, final DtField boostedField, final DtList<D> dtc);
}
