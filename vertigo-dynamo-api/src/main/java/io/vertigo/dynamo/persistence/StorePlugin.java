package io.vertigo.dynamo.persistence;

import io.vertigo.kernel.component.Plugin;

/**
 * Plugin permettant de gérer les accès physiques à un quelconque système de stockage.
 * SQL ou non SQL.
 *
 * @author  pchretien
 * @version $Id: StorePlugin.java,v 1.2 2013/10/22 12:37:38 pchretien Exp $
 */
public interface StorePlugin extends Plugin, Store {
	//
}
