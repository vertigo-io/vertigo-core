package io.vertigo.dynamo.persistence;

import io.vertigo.kernel.component.Plugin;

/**
 * Plugin permettant de gérer les accès physiques à un quelconque système de stockage.
 * SQL ou non SQL.
 *
 * @author  pchretien
 */
public interface StorePlugin extends Plugin, Store {
	//
}
