package io.vertigo.labs.impl.geocoder;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.labs.geocoder.GeoCoderManager;
import io.vertigo.labs.geocoder.GeoLocation;

import javax.inject.Inject;

/**
 * @author spoitrenaud
 *
 */
public final class GeoCoderManagerImpl implements GeoCoderManager {
	private final GeoCoderPlugin geoCoderPlugin;

	/**
	 * Constructeur.
	 * @param geoCoderPlugin Plugin de Geocoding
	 */
	@Inject
	public GeoCoderManagerImpl(final GeoCoderPlugin geoCoderPlugin) {
		Assertion.checkNotNull(geoCoderPlugin);
		//---------------------------------------------------------------------
		this.geoCoderPlugin = geoCoderPlugin;

	}

	/** {@inheritDoc} */
	public GeoLocation findLocation(final String address) {
		return geoCoderPlugin.findLocation(address);
	}

	/** {@inheritDoc} */
	public double distanceKm(final GeoLocation geoLocation1, final GeoLocation geoLocation2) {
		Assertion.checkArgument(!geoLocation1.isUndefined(), "le premier point n'est pas d�fini");
		Assertion.checkArgument(!geoLocation2.isUndefined(), "le second point n'est pas d�fini");
		//-------------------------------------------------------------------------
		int R = 6371; // km
		final double theta = Math.toRadians(geoLocation2.getLongitude() - geoLocation1.getLongitude());
		double lat1 = Math.toRadians(geoLocation1.getLatitude());
		double lat2 = Math.toRadians(geoLocation2.getLatitude());
		return Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(theta)) * R;

	}

}
