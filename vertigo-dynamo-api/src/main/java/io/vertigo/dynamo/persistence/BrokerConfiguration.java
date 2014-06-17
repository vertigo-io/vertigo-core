package io.vertigo.dynamo.persistence;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;

/**
 * Configuration du composant de persistance.
 * 
 * @author pchretien
 */
public interface BrokerConfiguration {

	/**
	 * Enregistre si un DT peut être mis en cache et la façon de charger les données.
	 * @param dtDefinition Définition de DT
	 * @param timeToLiveInSeconds Durée de vie du cache
	 * @param isReloadedByList Si ce type d'objet doit être chargé de façon ensembliste ou non
	 */
	void registerCacheable(final DtDefinition dtDefinition, final long timeToLiveInSeconds, final boolean isReloadedByList);

	/**
	 * Enregistre un Store spécifique pour une dtDefinition donnée.
	 * @param dtDefinition Définition de DT
	 * @param specificStore Store spécifique
	 */
	void register(final DtDefinition dtDefinition, final StorePlugin specificStore);
}
