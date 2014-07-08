package io.vertigo.labs.geocoder.google;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.labs.geocoder.GeoCoderManager;
import io.vertigo.labs.geocoder.GeoLocation;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author spoitrenaud
 */
public class GoogleGeoCoderManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private GeoCoderManager geoCoderManager;

	/**
	 * Test de g�olocalisation d'une cha�ne null.
	 */
	@Test(expected = NullPointerException.class)
	public final void testNull() {
		// On v�rifie que la g�olocalisation d'une addresse n'existant pas retourne une liste vide
		GeoLocation geoLocation = geoCoderManager.findLocation(null);
		Assert.assertTrue(geoLocation.isUndefined());
	}

	/**
	 * Test de g�olocalisation d'une cha�ne vide.
	 */
	public final void testEmpty() {
		// On v�rifie que la g�olocalisation d'une addresse n'existant pas
		// retourne une liste vide
		GeoLocation geoLocation = geoCoderManager.findLocation("");
		Assert.assertTrue(geoLocation.isUndefined());
	}

	/**
	 * Test de geolocalisation d'une adresse retournant un seul r�sultat.
	 * Test avec un accent.
	 */
	@Test
	public final void testEtain() {
		// G�olocalisation
		String address = "étain,55400,Meuse,Lorraine,FRANCE";
		final GeoLocation geoLocation = geoCoderManager.findLocation(address);
		AssertNear(geoLocation, 49.213506, 5.63623222988, 2);
		Assert.assertEquals("étain", geoLocation.getLocality().toLowerCase());
		Assert.assertEquals("55", geoLocation.getLevel2());
		Assert.assertEquals("LORRAINE", geoLocation.getLevel1().toUpperCase());
		Assert.assertEquals("FR", geoLocation.getCountryCode());
	}

	/**
	 * Test de geolocalisation d'une adresse retournant un seul r�sultat.
	 */
	@Test
	public final void testOneResult() {
		// G�olocalisation
		final GeoLocation geoLocation = geoCoderManager.findLocation("4, rue du vieux lavoir, 91190, Saint-Aubin");
		AssertNear(geoLocation, 48.713709, 2.138841, 0.1);
	}

	/**
	 * Test de g�olocalisation d'une adresse retournant un seul r�sultat.
	 */
	@Test
	public final void testOneResult2() {
		final GeoLocation geoLocation = geoCoderManager.findLocation("4 rue du VIEux lavoir, 91190 Saint-aubin, france");
		AssertNear(geoLocation, 48.713709, 2.138841, 0.1);
		Assert.assertEquals("91", geoLocation.getLevel2());
	}

	/**
	 * Test de calcul de distance.
	 */
	@Test
	public final void testDistance() {
		final GeoLocation paris = new GeoLocation(48.8667, 2.3333);
		final GeoLocation roma = new GeoLocation(41.9000, 12.4833);

		double distance = geoCoderManager.distanceKm(paris, roma);
		Assert.assertTrue(Math.abs(distance - 1105.76) < 1);
	}

	//-------------------------------------------------------------------------
	//--------------------------Static-----------------------------------------
	//-------------------------------------------------------------------------

	/**
	 * Test de g�olocalisation d'une adresse retournant plusieurs r�sultats.
	 */
	@Test
	public final void testManyResults() {
		// G�olocalisation
		final GeoLocation coordinates = geoCoderManager.findLocation("brussels");
		AssertNear(coordinates, 50.84807, 4.349427, 2);
	}

	private void AssertNear(final GeoLocation geoLocation, final double latitude, final double longitude, final double distanceMaxKm) {
		Assert.assertTrue(!geoLocation.isUndefined());
		Assert.assertTrue(near(geoLocation, latitude, longitude, distanceMaxKm));
	}

	/**
	 * M�thode permettant de v�rifier qu'un r�sultat se situe bien dans un perim�tre donn�.
	 *  
	 * @param geoCoord le r�sultat � v�rifier
	 * @param lat la latitude du centre du p�rim�tre
	 * @param lon la longitude du centre du p�rim�tre
	 * @param distanceMax le rayon du cercle en km
	 * @return True si le point recherch� est dans le p�rim�tre consid�r�
	 */
	private boolean near(final GeoLocation geoLocation, final double latitude, final double longitude, final double distanceMax) {
		GeoLocation geoLocation2 = new GeoLocation(latitude, longitude);
		return geoCoderManager.distanceKm(geoLocation, geoLocation2) < distanceMax;
	}
}
