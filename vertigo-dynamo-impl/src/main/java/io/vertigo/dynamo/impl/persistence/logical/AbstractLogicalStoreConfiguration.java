package io.vertigo.dynamo.impl.persistence.logical;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration logique des stores physiques.
 * @author pchretien, npiedeloup
 * @version $Id: AbstractLogicalStoreConfiguration.java,v 1.5 2013/10/22 12:26:32 pchretien Exp $
 */
abstract class AbstractLogicalStoreConfiguration<D extends Definition, S> {
	/** Store physique par défaut. */
	private S defaultStore;

	/** Map des stores utilisés spécifiquement pour certains DT */
	private final Map<D, S> storeMap = new HashMap<>();

	/**
	 * Fournit un store adpaté au type de l'objet.
	 * @param definition Définition 
	 * @return Store utilisé pour cette definition
	 */
	protected final S getPhysicalStore(final D definition) {
		Assertion.checkNotNull(definition);
		//---------------------------------------------------------------------
		//On regarde si il existe un store enregistré spécifiquement pour cette Definition
		S physicalStore = storeMap.get(definition);

		physicalStore = physicalStore == null ? defaultStore : physicalStore;
		Assertion.checkNotNull(physicalStore, "Aucun store trouvé pour la définition '{0}'", definition.getName());
		return physicalStore;
	}

	/**
	 * Enregistre un Store spécifique pour une dtDefinition donnée.
	 * @param definition Définition
	 * @param specificStore Store spécifique
	 */
	public final void register(final D definition, final S specificStore) {
		//check();
		Assertion.checkNotNull(definition);
		Assertion.checkNotNull(specificStore);
		Assertion.checkArgument(!storeMap.containsKey(definition), "Un store spécifique est déjà enregistré pour cette definition ''{0}'')", storeMap.get(definition));
		//----------------------------------------------------------------------
		storeMap.put(definition, specificStore);
	}

	public final void registerDefaultPhysicalStore(final S tmpDefaultStore) {
		Assertion.checkNotNull(tmpDefaultStore);
		Assertion.checkState(defaultStore == null, "defaultStore deja initialisé");
		//---------------------------------------------------------------------
		defaultStore = tmpDefaultStore;
	}
}
