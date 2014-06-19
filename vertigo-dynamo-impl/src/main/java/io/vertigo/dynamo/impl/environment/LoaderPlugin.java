package io.vertigo.dynamo.impl.environment;

import io.vertigo.kernel.component.Plugin;

/**
 * Plugin de chargement de l'environnement.
 * @author pchretien
 */
public interface LoaderPlugin extends Plugin, Loader {
	/**
	 * Type parsed by loader. 
	 * Examples : oom, kpr, eaxmi...
	 */
	String getType();
}
