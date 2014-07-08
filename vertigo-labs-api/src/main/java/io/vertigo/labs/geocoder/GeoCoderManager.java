package io.vertigo.labs.geocoder;

import io.vertigo.kernel.component.Manager;

/**
 * API de Geocoding d'adresses postales ou de POI.
 * 
 * @author spoitrenaud, pchretien
 */
public interface GeoCoderManager extends Manager {
	/**
	 * Geocoding d'une adresse.
	 * 
	 * @param address Chaine de caract�res repr�sentant une adresse.
	 * @return Liste des emplacements (latitude ; longitude) correspondant � l'adresse recherch�e.
	 */
	GeoLocation findLocation(String address);

	/**
	 * Calcul de distance entre deux points
	 *  
	 * @param geoLocation1 Premier point
	 * @param geoLocation2 Second point
	 * @return Distance exprim�es en km.
	 */
	double distanceKm(final GeoLocation geoLocation1, final GeoLocation geoLocation2);
}
