package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.Function;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIForMasterData;
import io.vertigo.dynamo.domain.model.DtObject;

import java.io.Serializable;

/**
 * Configuration des données de référence.
 * @author  pchretien
 * @version $Id: MasterDataConfiguration.java,v 1.3 2014/01/20 17:45:43 pchretien Exp $
 */
public interface MasterDataConfiguration {
	/**
	 * Enregistre la stratégie d'accès à une liste de référence.
	 * La liste est un filtrage simple sur la liste racine.
	 * @param uri URI
	 * @param fieldName Nom du champ de sélection 
	 * @param value  Valeur de sélection
	 */
	void register(final DtListURIForMasterData uri, final String fieldName, final Serializable value);

	/**
	 * Enregistre la stratégie d'accès à une liste de référence.
	 * La liste est un filtrage double sur la liste racine.
	 * @param uri URI
	 * @param fieldName1 Nom du premier champ de sélection 
	 * @param value1  Valeur du premier champ de sélection
	 * @param fieldName2 Nom du second champ de sélection 
	 * @param value2  Valeur du second champ de sélection
	 */
	void register(final DtListURIForMasterData uri, final String fieldName1, final Serializable value1, final String fieldName2, final Serializable value2);

	/**
	 * Enregistre la stratégie d'accès à une liste de référence.
	 * La liste de référence est La liste racine.
	 * @param uri URI
	 */
	void register(final DtListURIForMasterData uri);

	/**
	 * Indique s'il existe une MasterDataList pour ce type d'objet.
	 * @param dtDefinition  Définition de DT
	 * @return True, s'il existe une MasterDataList
	 */
	boolean containsMasterData(final DtDefinition dtDefinition);

	/**
	 * Renvoi l'URI à partir d'une définition.
	 * @param dtDefinition DId de la Définition de DT
	 * @return URI de retour (notNUll)
	 */
	DtListURIForMasterData getDtListURIForMasterData(final DtDefinition dtDefinition);

	/**
	 * @param <D> Type d'objet
	 * @param uri URI de la liste
	 * @return Fonction à appliquer sur la liste (par rapport à la liste complète).
	 */
	<D extends DtObject> Function<DtList<D>, DtList<D>> getFilter(final DtListURIForMasterData uri);
}
