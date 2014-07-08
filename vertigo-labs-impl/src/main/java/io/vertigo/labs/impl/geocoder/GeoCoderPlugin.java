package io.vertigo.labs.impl.geocoder;

import io.vertigo.kernel.component.Plugin;
import io.vertigo.labs.geocoder.GeoLocation;

/**
 * @author spoitrenaud
 *
 */
public interface GeoCoderPlugin extends Plugin {
	/**
	 * Geocoding d'une adresse.
	 * 
	 * @param address Chaine de caract�res repr�sentant une adresse.
	 * @return Liste des emplacements (latitude ; longitude) correspondant � l'adresse recherch�e.
	 */
	GeoLocation findLocation(String address);
}
